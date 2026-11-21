.PHONY: setup up upd stop restart build down downv ps clj/test clj/check clj/check-lint clj/check-format clj/fix clj/install clj/outdated db/migrate db/reset db/login db/repl clojure/login clojure/repl

## コンテナ操作系
# セットアップ
setup:
	docker compose build
	docker compose up -d --force-recreate --wait
	docker compose exec api lein run -m api.migrate
	docker compose stop
# コンテナの起動
up:
	docker compose up --force-recreate
# コンテナの起動（バックグランド）
upd:
	docker compose up -d --force-recreate --wait
# コンテナの停止
stop:
	docker compose stop
# コンテナの再起動（停止+起動）
restart:
	docker compose restart
# コンテナのビルド
build:
	docker compose build
# コンテナの削除
down:
	docker compose down --remove-orphans
# コンテナの削除（ボリューム含む）
downv:
	docker compose down -v --remove-orphans
# コンテナの状態確認
ps:
	docker compose ps -a


## Leiningen操作系
# テスト
clj/test:
	docker compose exec api lein test
# コードチェック
clj/check: clj/check-lint clj/check-format
# コードチェック（リンター）
clj/check-lint:
	docker compose exec api lein clj-kondo --lint src test
# コードチェック（フォーマッタ）
clj/check-format:
	docker compose exec api lein cljfmt check
# コード修正（フォーマッタ）
clj/fix:
	docker compose exec api lein cljfmt fix
# ライブラリのインストール
clj/install:
	docker compose exec api lein deps
# 新しいバージョンがあるライブラリの確認
clj/outdated:
	docker compose exec api lein ancient :all


## DB操作系
# DBのマイグレーション（全テーブルを削除して、作り直す）
db/migrate:
	docker compose exec api lein run -m api.migrate
# DBのリセット（マイグレーションと同じ）
db/reset: db/migrate
# dbコンテナのシェル
db/login:
	docker compose exec db bash
# dbコンテナのmysqlのrepl
db/repl:
	docker compose exec db mysql demo


## コンソール系
# apiコンテナのシェル
clojure/login:
	docker compose exec api bash
# apiコンテナのclojureのrepl
clojure/repl:
	docker compose exec api lein repl
