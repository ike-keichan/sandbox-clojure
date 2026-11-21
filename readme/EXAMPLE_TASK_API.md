# Example: task-api（タスクを管理する API の見本）

`example/task-api` ブランチの説明です。このブランチは、`main`（最小の形）に、タスクを管理する API を足したものです。機能を足すときの、書き方の手本として使います。

リポジトリ全体の説明は [README](../README.md)、各ファイルの役割は [Architecture](./03_ARCHITECTURE.md) を参照してください。

## 足した API

| メソッドとパス | 内容 | 成功したときの状態コード |
| --- | --- | --- |
| `GET /api/tasks` | タスクの一覧を返します。`?is_done=true` のように付けると、完了か未完了かで絞り込みます | 200 |
| `POST /api/tasks` | タスクを作ります。`title` と `due_date` を渡します | 201 |
| `GET /api/tasks/:id` | タスクを1件返します | 200 |
| `PUT /api/tasks/:id` | タスクの `title` と `due_date` を更新します | 200 |
| `DELETE /api/tasks/:id` | タスクを削除します | 204（本文なし） |
| `PUT /api/tasks/:id/done` | タスクの完了と未完了を切り替えます | 200 |

タスクは、次の項目を持ちます。

| 項目 | 型 | 内容 |
| --- | --- | --- |
| `id` | 整数 | 自動で振られる番号 |
| `title` | 文字列 | タスク名 |
| `due_date` | 日付（`2024-09-01` の形） | 期日。なくてもよい |
| `is_done` | 真偽値 | 完了したかどうか。作った時点では `false` |

うまくいかなかったときは、次の状態コードと、`{"detail": "<内容>"}` の形の本文を返します。

| 場面 | 状態コード |
| --- | --- |
| 指定した `id` のタスクがない | 404 |
| `id` が数字でない | 404 |
| `due_date` が、存在しない日付や、形の違う文字列である | 422 |

## 試す

環境構築は、[Getting Started](./01_GETTING_STARTED.md) の手順で行います。`git clone` のあとに、このブランチに切り替えてください。

```zsh
$ git switch example/task-api
$ make setup
$ make upd
```

ターミナルから、API を呼びます。

```zsh
$ curl -X POST http://localhost:3000/api/tasks -H 'content-type: application/json' -d '{"title": "ラジオ体操", "due_date": "2024-09-01"}'
{"id":1,"title":"ラジオ体操","due_date":"2024-09-01","is_done":false}

$ curl -X PUT http://localhost:3000/api/tasks/1/done
{"id":1,"title":"ラジオ体操","due_date":"2024-09-01","is_done":true}

$ curl 'http://localhost:3000/api/tasks?is_done=true'
[{"id":1,"title":"ラジオ体操","due_date":"2024-09-01","is_done":true}]

$ curl http://localhost:3000/api/tasks/999
{"detail":"Task not found"}
```

## `main` に足したファイル

| ファイル | 内容 |
| --- | --- |
| `src/api/task/core.clj` | タスクの API のルーティング |
| `src/api/task/handler.clj` | リクエストの受け取り、値の検査、レスポンスの組み立て |
| `src/api/task/repository.clj` | `tasks` テーブルの読み書き |
| `test/api/task/core_test.clj` | タスクの API のテスト |
| `test/api/test_helper.clj` | テスト用のデータベースの用意と、レスポンスの読み取り |

## `main` から書き換えたファイル

| ファイル | 書き換えた内容 |
| --- | --- |
| `src/api/core.clj` | タスクのルーティングを、`app-routes` に足しました |
| `src/api/migrate.clj` | `tasks` テーブルを作る SQL を、`tables` に足しました |

## 書き方の要点

- **3つのファイルに分ける**：ルーティング（`core.clj`）、HTTP に関わる処理（`handler.clj`）、SQL（`repository.clj`）を分けています。`handler.clj` に SQL はなく、`repository.clj` に状態コードはありません。
- **値の検査は、ハンドラで行う**：`due_date` を日付に変えられなければ、データベースを呼ぶ前に 422 を返します。
- **ないときは 404**：1件を対象にする API は、最初にタスクを取得して、なければ 404 を返します。
- **`id` は、ルーティングで数字に限る**：`"/tasks/:id{[0-9]+}"` と書くと、数字でない `id` は、ルーティングに一致しなくなります。
- **データベースの値を、レスポンス用にそろえる**：MySQL から返る日付と真偽値を、`repository.clj` の `row->task` で、JSON にできる形に直しています。

## テストの仕組み

タスクの API のテストは、実際の MySQL を使います。

- **開発用とは別のデータベースを使います。** テストの最初に、`demo_test` というデータベースを作り、接続先をそちらに差し替えます。開発用の `demo` のデータは、消えません。
- **テストごとに、空の状態から始まります。** テストのたびに、全テーブルを作り直します。
- **`db` のコンテナが動いている必要があります。** `make upd` で起動してから、`make clj/test` を実行してください。

仕組みは、`test/api/test_helper.clj` の `with-test-database` にあります。データベースを使うテストのファイルでは、次の1行で、この仕組みを有効にします。

```clojure
(use-fixtures :each with-test-database)
```
