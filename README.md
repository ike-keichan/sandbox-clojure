# sandbox-clojure

Clojure（Ring、Compojure）による Web API の技術検証と、プロジェクトのひな形を兼ねたリポジトリ。

## 概要

| 項目 | 内容 |
| --- | --- |
| 題材 | Clojure による Web API の実装 |
| 時期 | 2024年9月 |
| 言語 | Clojure |

Clojure で、タスクを管理する Web API を作り、構成を検証したものである。HTTP の処理に Ring、ルーティングに Compojure、データベースに MySQL を使い、Docker Compose で動かす。[sandbox-fastapi](https://github.com/ike-keichan/sandbox-fastapi) で作った API と同じ題材を、Clojure で書いている。

用意している API は、次のとおりである。

| メソッドとパス | 内容 |
| --- | --- |
| `GET /api/hello` | `hello world!` というメッセージを返す |
| `GET /api/tasks` | タスクの一覧を返す。`isDone` を付けると、完了か未完了かで絞り込む |
| `POST /api/tasks` | タスクを作る。`title` を渡す |
| `GET /api/tasks/:id` | タスクを1件返す |
| `PUT /api/tasks/:id/done` | タスクの完了と未完了を切り替える |

## ディレクトリ構成

| ディレクトリ・ファイル | 内容 |
| --- | --- |
| `src/api/core.clj` | 入口。全体のルーティングと、サーバーの起動 |
| `src/api/db.clj` | データベースへの接続の設定と、`tasks` テーブルの作成 |
| `src/api/hello/` | hello API のルーティング（`core.clj`）と処理（`handler.clj`） |
| `src/api/tasks/` | tasks API のルーティング（`core.clj`）と処理（`handler.clj`） |
| `src/utils/` | 応答の JSON のキーを、キャメルケースに変える関数 |
| `test/` | hello API と tasks API のテストと、テスト用のリクエストを作る関数 |
| `project.clj` | Leiningen の設定。依存するライブラリと、そのバージョンの記録 |
| `Dockerfile`、`docker-compose.yml` | Clojure のコンテナと、MySQL のコンテナの定義 |
| `Makefile` | よく使うコマンドをまとめたもの |

## 実行環境

当時使っていた環境のうち、実行に必要なソフトウェアを挙げる。バージョンは、リポジトリに記録が残っている値である。

| ソフトウェア | バージョン | 用途 | 補足 |
| --- | --- | --- | --- |
| macOS | 14 Sonoma | OS | |
| Docker Desktop | 不明 | コンテナの実行 | バージョンの記録がない |
| OpenJDK | 17 | Clojure の実行 | `Dockerfile` に記録。コンテナの中で使う |
| Leiningen | 不明 | ビルドと実行 | `Dockerfile` のイメージ（`clojure:openjdk-17-lein`）に付属するもの |
| Clojure | 1.10.3 | 言語 | `project.clj` に記録 |
| Ring | 1.9.2 | HTTP の処理 | `project.clj` に記録 |
| Compojure | 1.6.2 | ルーティング | `project.clj` に記録 |
| MySQL | 8.0 | データベース | `docker-compose.yml` に記録 |

Java、Leiningen、Clojure、MySQL は、コンテナの中で動く。手元にインストールする必要はない。

## セットアップ

次の手順で、実行に必要なソフトウェアを用意する。当時より新しいバージョンでの動作は確認していないので、動かない場合は「実行環境」の表に近いバージョンを使う。

1. [Docker Desktop](https://www.docker.com/products/docker-desktop/) をインストールして、起動する。
2. コンテナをビルドする。

   ```sh
   make build
   ```

## 実行方法

コンテナを起動する。

```sh
make up
```

`http://localhost:3000/api/hello` を開くと、メッセージが返る。終了するには、ターミナルで Ctrl + C を押す。

主なコマンドは、次のとおりである。すべて `Makefile` に書いてある。

| コマンド | 内容 |
| --- | --- |
| `make up` | コンテナを起動する |
| `make upd` | コンテナを、バックグラウンドで起動する |
| `make stop` | コンテナを止める |
| `make down` | コンテナを削除する |
| `make downv` | コンテナを、データベースの中身ごと削除する |
| `make test` | テストを実行する（コンテナの起動中に使う） |
| `make clojure/repl` | Clojure の REPL を開く |
| `make db/repl` | MySQL に接続する |

## 備考

- tasks API は作りかけである。タスクの更新と削除は、ルーティングをコメントにしてあり、処理も書いていない。テストには、その2つを対象にしたものも含まれている。
- `src/api/tasks/handler.clj` などに、確認用の表示（`println`）が残っている。
- データベースは、パスワードなしの root で接続する設定である。手元で試すためのもので、公開する環境では使えない。
