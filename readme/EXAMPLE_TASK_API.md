# Example: task-api（タスクを管理する API の見本）

`example/task-api` ブランチの説明です。このブランチは、`main`（最小の形）に、タスクを管理する API を足したものです。機能を足すときの、書き方の手本として使います。

リポジトリ全体の説明は [README](../README.md)、各ファイルの役割は [Architecture](./03_ARCHITECTURE.md) を参照してください。

## 足した API

| メソッドとパス | 内容 | 成功したときの状態コード |
| --- | --- | --- |
| `GET /api/tasks` | タスクの一覧を、ID の小さい順に返します。`?is_done=true` のように付けると、完了か未完了かで絞り込みます | 200 |
| `POST /api/tasks` | タスクを作ります。`title` と `due_date` を渡します | 201 |
| `GET /api/tasks/:id` | タスクを1件返します | 200 |
| `PUT /api/tasks/:id` | タスクの `title` と `due_date` を更新します。渡さなかった `due_date` は、空になります | 200 |
| `DELETE /api/tasks/:id` | タスクを削除します | 204（本文なし） |
| `PUT /api/tasks/:id/done` | タスクを完了にします | 200 |
| `DELETE /api/tasks/:id/done` | タスクを未完了に戻します | 200 |

リクエストの本文は、JSON で渡します。`content-type: application/json` を付けてください。

タスクは、次の項目を持ちます。

| 項目 | 型 | 内容 | 渡すとき |
| --- | --- | --- | --- |
| `title` | 文字列 | タスク名。空白だけではなく、1024文字以下 | 必須 |
| `due_date` | 日付（`2024-09-01` の形） | 期日。年は、1000 から 9999 まで | なくてもよい |
| `id` | 整数 | 自動で振られる番号 | 渡せません |
| `is_done` | 真偽値 | 完了したかどうか。作った時点では `false` | 渡せません（`/done` の API で変えます） |

うまくいかなかったときは、次の状態コードと、`{"detail": "<内容>"}` の形の本文を返します。

| 場面 | 状態コード | `detail` |
| --- | --- | --- |
| 指定した `id` のタスクがない | 404 | `Task not found` |
| `id` が数字でない | 404 | `Not Found` |
| 本文が JSON のオブジェクトでない（本文がない、`content-type` を付けていない、配列を渡した） | 422 | `Invalid task` |
| `title` がない、空、1025文字以上、文字列でない | 422 | `Invalid task` |
| `due_date` が、存在しない日付や、形の違う文字列である | 422 | `Invalid task` |
| `is_done` が、`true` と `false` 以外である | 422 | `Invalid is_done` |
| 本文を、JSON として読めない | 400 | `Malformed JSON` |

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

$ curl -X POST http://localhost:3000/api/tasks -H 'content-type: application/json' -d '{"title": ""}'
{"detail":"Invalid task"}
```

## `main` に足したファイル

| ファイル | 内容 |
| --- | --- |
| `src/api/task/core.clj` | タスクの API のルーティング |
| `src/api/task/handler.clj` | リクエストの受け取り、値の検査、レスポンスの組み立て |
| `src/api/task/repository.clj` | `tasks` テーブルの読み書き |
| `test/api/task/core_test.clj` | タスクの API のテスト |
| `test/api/db_test_helper.clj` | テスト用のデータベースの用意 |
| `test/api/db_test.clj` | トランザクション（`api.db/with-transaction`）のテスト |

## `main` から書き換えたファイル

| ファイル | 書き換えた内容 |
| --- | --- |
| `src/api/core.clj` | タスクのルーティングを、`app-routes` に足しました |
| `src/api/migrate.clj` | `tasks` テーブルを作る SQL を、`tables` に足しました |
| `scripts/smoke.sh` | 起動した API を呼ぶ確認に、タスクの API を足しました |

## 書き方の要点

- **3つのファイルに分ける**：ルーティング（`core.clj`）、HTTP に関わる処理（`handler.clj`）、SQL（`repository.clj`）を分けています。`handler.clj` に SQL はなく、`repository.clj` に状態コードはありません。
- **値の検査は、ハンドラで、データベースを呼ぶ前に行う**：本文が JSON のオブジェクトであること、`title` の型と長さ、`due_date` の形と範囲を確かめ、正しくなければ 422 を返します。検査がないと、長すぎる値は、データベースのエラーになり、500 が返ります。
- **本文がマップであることを、最初に確かめる**：`content-type` を付け忘れたリクエストでは、本文がマップになりません。確かめずにキーを取り出すと、すべて `nil` になり、空のタスクを作ったり、既存のタスクを空で上書きしたりします。
- **更新と削除は、結果の行の数で、対象があったかを判断する**：先に SELECT で存在を調べてから更新すると、そのあいだに別のリクエストが削除した場合に、正しく扱えません。`repository.clj` は、UPDATE や DELETE が対象にした行の数を見て、0 なら「なかった」と返します。
- **何度呼んでも、結果が同じになるようにする**：完了の操作は、「呼ぶたびに切り替える」のではなく、`PUT` で完了、`DELETE` で未完了、と分けています。通信の再送で2回届いても、結果が変わりません。
- **`id` は、ルーティングで数字に限る**：`"/tasks/:id{[0-9]+}"` と書くと、数字でない `id` は、ルーティングに一致しなくなります。
- **データベースの値を、レスポンス用にそろえる**：MySQL から返る日付と真偽値を、`repository.clj` の `row->task` で、JSON にできる形に直しています。

## テストの仕組み

タスクの API のテストは、実際の MySQL を使います。

- **開発用とは別のデータベースを使います。** テストの最初に、開発用のデータベースの名前に `_test` を付けたデータベース（`demo_test`）を作り、接続先をそちらに差し替えます。開発用の `demo` のデータは、消えません。
- **テストごとに、空の状態から始まります。** テストのたびに、全テーブルを作り直します。
- **`db` のコンテナが動いている必要があります。** `make upd` で起動してから、`make clj/test` を実行してください。

仕組みは、`test/api/db_test_helper.clj` の `with-test-database` にあります。データベースを使うテストのファイルでは、次の1行で、この仕組みを有効にします。

```clojure
(use-fixtures :each with-test-database)
```

テストは、サーバーを起動せずに、関数を直接呼びます。起動したサーバーを実際に呼ぶ確認は、[scripts/smoke.sh](../scripts/smoke.sh) で行っています。

```zsh
$ make upd
$ bash scripts/smoke.sh
```
