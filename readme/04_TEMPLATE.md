# Template（ひな形として使う）

このリポジトリをもとに、新しいプロジェクトを始める手順です。

- [1. 自分のリポジトリを作る](#1-自分のリポジトリを作る)
- [2. 手元に持ってきて、動くことを確かめる](#2-手元に持ってきて動くことを確かめる)
- [3. 名前を変える](#3-名前を変える)
- [4. 機能を足す](#4-機能を足す)
- [5. 不要なものを消す](#5-不要なものを消す)

---

## 1. 自分のリポジトリを作る

1. GitHub で、[このリポジトリのページ](https://github.com/ike-keichan/sandbox-clojure)を開きます。
2. 右上の「Use this template」を押し、「Create a new repository」を選びます。
3. リポジトリの名前と、公開範囲（Public か Private）を決めます。
4. どのブランチから始めるかで、次のどちらかを選びます。

   | 始めたい内容 | 「Include all branches」 | 作ったあとに使うブランチ |
   | --- | --- | --- |
   | 最小の形（API は `GET /api/hello` だけ） | チェックしない | `main` |
   | タスクを管理する API の見本つき | チェックする | `example/task-api` |

   迷ったら、最小の形で始めてください。見本は、このリポジトリの `example/task-api` ブランチを、あとからいつでも見られます。

5. 「Create repository」を押します。

「Use this template」で作ったリポジトリは、このリポジトリの履歴を引き継ぎません。最初のコミットが1つだけある状態から始まります。

---

## 2. 手元に持ってきて、動くことを確かめる

名前を変える前に、まず、そのままで動くことを確かめます。

[Getting Started](./01_GETTING_STARTED.md) の手順を、「2. リポジトリを手元に持ってくる」から進めてください。`git clone` の URL は、いま作った自分のリポジトリのものを使います。

`curl http://localhost:3000/api/hello` で応答が返ったら、次へ進みます。

---

## 3. 名前を変える

`sandbox-clojure` や `demo` という名前を、自分のプロジェクトの名前に変えます。

| ファイル | 変える場所 | いまの値 |
| --- | --- | --- |
| `project.clj` | `defproject` の直後の名前 | `sandbox-clojure` |
| `project.clj` | `:description` | `Clojure による Web API のひな形` |
| `docker-compose.yml` | `MYSQL_DATABASE`（`db` の `environment`） | `demo` |
| `docker-compose.yml` | `DB_NAME`（`api` の `environment`） | `demo` |
| `Makefile` | `db/repl` の最後の `demo` | `demo` |
| `README.md`、`readme/` | 題名、説明、URL | |

データベースの名前を変えた場合は、古い名前のデータベースが残っているので、環境を作り直します。

```zsh
$ make downv
$ make setup
$ make up
```

コンテナの名前（`sandbox-clojure-api-1` など）は、フォルダの名前から自動で付きます。手で変える必要はありません。

名前空間の先頭の `api`（`api.core` など）は、そのままでも動きます。変える場合は、`src/api/` と `test/api/` のフォルダ名、各ファイルの `ns`、`project.clj` の `:main` を、すべてそろえてください。

---

## 4. 機能を足す

`task`（タスク）という機能を足す場合を例に、書く順番を説明します。完成した形は、[`example/task-api` ブランチ](https://github.com/ike-keichan/sandbox-clojure/tree/example/task-api)にあります。各ファイルの役割は、[Architecture](./03_ARCHITECTURE.md) を参照してください。

| 順番 | 書くファイル | 書くこと |
| --- | --- | --- |
| 1 | `src/api/migrate.clj` | テーブルを作る SQL を、`tables` に足す（ファイルの中に、書き方をコメントで示しています） |
| 2 | `src/api/task/repository.clj` | データベースの読み書き |
| 3 | `src/api/task/handler.clj` | リクエストの受け取りと、レスポンスの組み立て |
| 4 | `src/api/task/core.clj` | パスと、呼ぶハンドラ |
| 5 | `src/api/core.clj` | 4 で作ったルーティングを、`app-routes` に足す |
| 6 | `test/api/task/core_test.clj` | テスト |
| 7 | `scripts/smoke.sh` | 起動した API を呼んで確かめる行 |

書き終えたら、テーブルを作って、確かめます。

```zsh
$ make db/migrate
$ make clj/fix
$ make clj/check
$ make clj/test
```

`curl` で、足した API を呼んで確かめます。

新しいファイルを作るときは、名前空間とファイルの場所をそろえてください（`api.task.core` なら `src/api/task/core.clj`）。そろっていないと、読み込めずにエラーになります。

---

## 5. 不要なものを消す

自分の機能ができたら、見本の `hello` は消してかまいません。

| 消すもの | 場所 |
| --- | --- |
| `hello` のプログラム | `src/api/hello/` |
| `hello` の登録 | `src/api/core.clj` の、`hello-routes` を読み込む行と、`app-routes` の中の行 |
| `hello` のテスト | `test/api/hello/` |
| コンテナの起動の確認 | `docker-compose.yml` の `api` の `healthcheck` は、ポートを見ているだけなので、そのままで動きます |
| `hello` を使っているテスト | `test/api/core_test.clj` の `test-malformed-json` と `test-unhandled-exception`（自分の API を使う形に書き換える） |
| 起動の確認の呼び出し | `scripts/smoke.sh` の `/hello` を呼ぶ行（自分の API のパスに変える） |

`readme/` の文書も、自分のプロジェクトに合わせて書き換えてください。
