## コンテナ操作系
# コンテナの起動
up:
	docker compose up --force-recreate
# コンテナの起動（バックグランド）
upd:
	docker compose up -d --force-recreate
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
# パッケージインストール
install:
	docker compose exec clojure lein deps
# リンター
check:
	docker compose exec clojure lein check
# テスト
test:
	docker compose exec clojure lein test


## repl系
# clojureコンテナのシェル
clojure/login:
	docker compose exec clojure bash
# clojureコンテナのclojureのrepl
clojure/repl:
	docker compose exec clojure lein repl
# dbコンテナのシェル
db/login:
	docker compose exec db bash
# dbコンテナのmysqlのrepl
db/repl:
	docker compose exec db mysql demo
