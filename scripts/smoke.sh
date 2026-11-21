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
