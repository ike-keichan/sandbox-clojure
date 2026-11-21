#!/usr/bin/env bash
# 起動したAPIを、実際に呼んで確かめるスクリプト。自動テスト（GitHub Actions）から実行する。
# テスト（make clj/test）は、サーバーを起動せずに関数を直接呼ぶ。
# サーバーが実際に起動して応答することは、ここで確かめる。
# 機能を足したら、その機能のAPIを呼ぶ行を足す。
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:3000/api}"

# 状態コードを確かめる関数。期待した値でなければ、失敗で終わる。
# 使い方: expect_status <期待する状態コード> <curl の引数...>
expect_status() {
  local expected="$1"
  shift
  local actual
  actual="$(curl --silent --output /dev/null --write-out '%{http_code}' "$@")"
  if [ "${actual}" != "${expected}" ]; then
    echo "expected ${expected} but got ${actual}: $*" >&2
    exit 1
  fi
  echo "${actual} $*"
}

# 起動が終わるまで、少し待ちながら呼ぶ。
curl --fail --silent --show-error --retry 10 --retry-connrefused --retry-delay 2 "${BASE_URL}/hello"
echo

expect_status 404 "${BASE_URL}/unknown"
expect_status 400 -X POST "${BASE_URL}/hello" -H 'content-type: application/json' -d '{"message": '

# タスクのAPI
JSON='content-type: application/json'

# タスクを1件作って、振られたIDを取り出す。
CREATED="$(curl --fail --silent --show-error -X POST "${BASE_URL}/tasks" -H "${JSON}" -d '{"title": "日本語のタスク", "due_date": "2024-09-01"}')"
echo "${CREATED}"
TASK_ID="$(echo "${CREATED}" | sed -E 's/.*"id":([0-9]+).*/\1/')"

expect_status 200 "${BASE_URL}/tasks"
expect_status 200 "${BASE_URL}/tasks?is_done=false"
expect_status 200 "${BASE_URL}/tasks/${TASK_ID}"
expect_status 200 -X PUT "${BASE_URL}/tasks/${TASK_ID}" -H "${JSON}" -d '{"title": "更新したタスク"}'
expect_status 200 -X PUT "${BASE_URL}/tasks/${TASK_ID}/done"
expect_status 200 -X DELETE "${BASE_URL}/tasks/${TASK_ID}/done"
expect_status 204 -X DELETE "${BASE_URL}/tasks/${TASK_ID}"
expect_status 404 "${BASE_URL}/tasks/${TASK_ID}"

# 値が正しくないときは、DBのエラー（500）ではなく、入力の誤り（422）になる。
LONG_TITLE="$(printf 'a%.0s' $(seq 1 1025))"
expect_status 422 -X POST "${BASE_URL}/tasks" -H "${JSON}" -d "{\"title\": \"${LONG_TITLE}\"}"
expect_status 422 -X POST "${BASE_URL}/tasks"
expect_status 422 "${BASE_URL}/tasks?is_done=abc"
