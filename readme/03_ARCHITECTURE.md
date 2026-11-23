# Architecture（構成）

このリポジトリが、どういう部品でできていて、どこに何を書くのかの説明です。

- [1. コンテナの構成](#1-コンテナの構成)
- [2. リクエストの流れ](#2-リクエストの流れ)
- [3. プログラムの層](#3-プログラムの層)
- [4. ディレクトリ構成](#4-ディレクトリ構成)
- [5. テストの仕組み](#5-テストの仕組み)
- [6. 設定](#6-設定)
- [7. 注意点](#7-注意点)

---

## 1. コンテナの構成

Docker Compose で、2つのコンテナを動かします。定義は、[docker-compose.yml](../docker-compose.yml) にあります。

```mermaid
flowchart LR
    browser["Web ブラウザ<br>curl など"]
    subgraph compose["Docker Compose"]
        api["api<br>Java + Clojure + Jetty"]
        db[("db<br>MySQL")]
    end
    browser -- "localhost:3000" --> api
    api -- "db:3306" --> db
```

| コンテナ | 中身 | 手元からつなぐ先 |
| --- | --- | --- |
| `api` | Java、Clojure、Leiningen（Clojure のビルドの道具）。[Dockerfile](../Dockerfile) から作ります | `http://localhost:3000` |
| `db` | MySQL。公式のイメージを、そのまま使います | `localhost:33306` |

知っておくとよい点は、次の5つです。

- **ファイルを保存すると、変更が読み込まれます。** `api` のコンテナは、リポジトリのフォルダをそのまま読み込んでいます。`src/` の中のファイルを保存すると、次に API を呼んだときに、変わったファイルが読み込み直されます。この動きは、環境変数 `APP_ENV` が `development` のときだけ有効です。
- **ライブラリは、Docker のボリュームに入ります。** コンテナのイメージには入っていません。コンテナを作り直しても、ダウンロードし直さずに済みます。`project.clj` を変えたときは、`make clj/install` でダウンロードします。
- **コンテナの中のプログラムは、手元のパソコンと同じユーザーで動きます。** コンテナが作るファイル（`target` フォルダなど）が、手元で消せないファイルにならないようにするためです。ユーザーの番号は、`Makefile` が調べて、コンテナに渡しています。そのため、コンテナを起動するコマンドは、`docker compose` を直接使わずに、`make` から実行してください。
- **データベースの中身は、コンテナを止めても残ります。** これも、Docker のボリュームに保存しているためです。消すときは、`make downv` を使います。
- **`api` は、`db` の準備が終わるのを待ってから起動します。** `docker-compose.yml` の `healthcheck` と `depends_on` で決めています。
- **API とデータベースには、手元のパソコンからしかつなげません。** `docker-compose.yml` の `ports` に、`127.0.0.1` を付けているためです。外すと、同じネットワークのほかの機器から、パスワードなしでデータベースに入れてしまいます。

---

## 2. リクエストの流れ

HTTP の処理には、Ring というライブラリを使っています。Ring では、リクエストもレスポンスも、Clojure のマップで表します。

```mermaid
flowchart TD
    request["リクエスト"] --> middleware
    middleware["ミドルウェア<br>JSON とマップの変換"] --> routes
    routes["ルーティング<br>パスから、呼ぶ関数を決める"] --> handler
    handler["ハンドラ<br>処理して、レスポンスのマップを返す"] --> response["レスポンス"]
```

| 部品 | 役割 | 使うライブラリ | 書く場所 |
| --- | --- | --- | --- |
| ミドルウェア | リクエストの JSON を Clojure のマップに変え、レスポンスのマップを JSON に変えます。想定していない例外を受け止めて、決まった形の 500 にする役目もあります | ring-json | `src/api/core.clj` の `app` |
| ルーティング | パスとメソッドから、呼ぶ関数を決めます | Compojure | `src/api/core.clj` と、各機能の `core.clj` |
| ハンドラ | 処理を行い、レスポンスのマップを返します | Ring | 各機能の `handler.clj` |

ハンドラが返すのは、次のようなマップです。JSON への変換は、ミドルウェアが行います。

```clojure
{:status 200
 :body {:message "hello world!"}}
```

### エラーの返し方

うまくいかなかったときは、どの場合も、同じ形の JSON を返します。

```json
{"detail": "Not Found"}
```

| 場面 | 状態コード | 返しているところ |
| --- | --- | --- |
| パスが存在しない | 404 | `src/api/core.clj` の `app-routes` |
| リクエストの本文を、JSON として読めない | 400 | `src/api/core.clj` の `app`（`wrap-json-body` の設定） |
| 想定していない例外が起きた | 500 | `src/api/core.clj` の `wrap-exception` |
| 値が正しくない、対象が見つからない | 422、404 | 各機能の `handler.clj` |

想定していない例外をそのままにすると、サーバーが、内部の情報（ファイル名や SQL）を含んだ HTML の画面を返してしまいます。`wrap-exception` は、内容を記録に出したうえで、利用者には `Internal Server Error` とだけ返します。

エラーのレスポンスは、`api.response/error-response` で作ります。

---

## 3. プログラムの層

機能ごとに、ディレクトリを分けます。1つの機能は、役割の違うファイルに分けて書きます。

| ファイル | 役割 |
| --- | --- |
| `core.clj` | その機能のルーティング。パスとメソッドを決め、ハンドラを呼びます |
| `handler.clj` | ハンドラ。リクエストから値を取り出し、処理して、レスポンスを返します。「見つからなければ 404」のような、HTTP に関わる判断は、ここに書きます |
| `repository.clj` | データベースの読み書き。SQL は、ここにだけ書きます |

```
src/api/
├── core.clj               ← 全体のルーティングと、サーバーの起動
├── db.clj                 ← データベースへの接続
├── migrate.clj            ← テーブルの作成
├── response.clj           ← エラーのレスポンスを作る関数
├── hello/
│   ├── core.clj
│   └── handler.clj
└── task/                  ← 足す機能
    ├── core.clj
    ├── handler.clj
    └── repository.clj
```

`main` ブランチには、`hello/` だけが入っています。`hello` はデータベースを使わないので、`repository.clj` はありません。3つのファイルがそろった例は、`example/task-api` ブランチにあります。

機能を足したら、`src/api/core.clj` の `app-routes` に、その機能のルーティングを追加します。

---

## 4. ディレクトリ構成

| ディレクトリ・ファイル | 内容 |
| --- | --- |
| `src/api/core.clj` | 入口です。全体のルーティング、ミドルウェアの適用、サーバーの起動を行います |
| `src/api/db.clj` | データベースへの接続です。接続先の組み立てと、SQL を実行する関数があります |
| `src/api/migrate.clj` | テーブルを作るスクリプトです。`make db/migrate` と `make db/reset` で実行されます |
| `src/api/response.clj` | エラーのレスポンスを作る関数です。エラーの本文を、同じ形にそろえるために使います |
| `scripts/smoke.sh` | 起動した API を、実際に呼んで確かめるスクリプト。自動テストから実行されます |
| `src/api/<機能名>/` | 機能ごとのプログラム |
| `test/` | テスト。`src/` と同じ構成で、ファイル名の最後に `_test` を付けます |
| `project.clj` | Leiningen の設定です。依存するライブラリと、そのバージョンを書きます |
| `Dockerfile` | `api` のコンテナの作り方 |
| `docker-compose.yml` | `api` と `db` のコンテナの定義 |
| `Makefile` | よく使うコマンドをまとめたもの |
| `.github/workflows/test.yml` | 自動テストの設定 |
| `.github/dependabot.yml` | Dependabot（依存の更新の通知）の設定 |
| `readme/` | このリポジトリの文書 |

Clojure では、名前空間（`ns`）とファイルの場所が対応します。名前空間の `-` は、ファイル名では `_` になります。

| 名前空間 | ファイル |
| --- | --- |
| `api.hello.core` | `src/api/hello/core.clj` |
| `api.hello.core-test` | `test/api/hello/core_test.clj` |

---

## 5. テストの仕組み

テストは、`test/` にあります。Clojure に標準で付いている `clojure.test` を使います。

- **API を呼ぶ形で書きます。** ハンドラの関数を直接呼ばずに、テスト用のリクエスト（ring-mock）を `app` に渡して、レスポンスを確かめます。サーバーは起動しません。
- **ミドルウェアも通ります。** `app` を呼ぶので、JSON への変換も含めて確かめられます。

```clojure
(deftest test-hello
  (testing "GET /api/hello"
    (let [response (app (mock/request :get "/api/hello"))]
      (is (= 200 (:status response)))
      (is (= {:message "hello world!"} (parse-body response))))))
```

データベースを使う機能のテストの書き方は、`example/task-api` ブランチにあります。

テストは、サーバーを起動せずに、`app` を直接呼びます。サーバーが実際に起動して応答することは、[scripts/smoke.sh](../scripts/smoke.sh) で別に確かめています。このスクリプトは、起動した API を `curl` で呼ぶもので、自動テスト（GitHub Actions）から実行されます。機能を足したら、その機能の API を呼ぶ行を足してください。

---

## 6. 設定

環境変数で変えられます。`docker-compose.yml` の `api` の `environment` に書きます。

| 環境変数 | 既定の値 | 内容 |
| --- | --- | --- |
| `APP_ENV` | （なし） | `development` にすると、保存したファイルを読み込み直します |
| `TZ` | （なし） | タイムゾーン。`docker-compose.yml` では、`api` と `db` の両方を `Asia/Tokyo` にしています。そろえないと、日時を扱うときに、時刻がずれます |
| `PORT` | `3000` | API のポート番号 |
| `DB_HOST` | `db` | データベースのホスト名 |
| `DB_PORT` | `3306` | データベースのポート番号 |
| `DB_NAME` | `demo` | データベースの名前 |
| `DB_USER` | `root` | データベースのユーザー名 |
| `DB_PASSWORD` | （空） | データベースのパスワード |

読み込んでいるのは、[src/api/core.clj](../src/api/core.clj) と [src/api/db.clj](../src/api/db.clj) です。

---

## 7. 注意点

- **データベースは、パスワードなしの root でつなぐ設定です。** 手元で試すためのものです。公開する環境では、ユーザーとパスワードを設定してください。
- **`make db/migrate` は、まだないテーブルを作るだけの簡単なものです。** すでにあるテーブルの形は変えられません。データを残したままテーブルの形を変えたい場合は、Migratus などのマイグレーションの道具を足してください。
- **データベースへの接続は、SQL を実行するたびに作り直しています。** 接続をためておく仕組み（コネクションプール）は、使っていません。利用が増えたら、HikariCP などを足してください。
- **macOS の Docker Desktop で使う前提です。** コンテナの中のプログラムは、root で動きます。Linux では、コンテナが作ったファイル（`target/` など）が root の持ち物になり、手元で消すのに `sudo` が要ります。
- **手元から MySQL につなぐときのポートは、`33306` です。** コンテナの中では `3306` です。手元の MySQL と重ならないように、ずらしています。
- **起動や、コマンドの実行に時間がかかります。** Clojure は、実行のたびに Java を起動するためです。開発中は、コンテナを起動したままにして、保存したファイルの読み込み直しを使うと、待たずに済みます。
- **公開する環境で動かす形（jar ファイルの作成など）は、用意していません。** 手元で開発するところまでが、このひな形の範囲です。
