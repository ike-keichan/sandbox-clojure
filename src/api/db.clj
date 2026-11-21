(ns api.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as result-set]))

;; 環境変数を読む関数。設定されていなければ、既定の値を返す。
(defn- env [name default]
  (or (System/getenv name) default))

;; DBの接続先。環境変数で上書きできる（docker-compose.yml を参照）。
(def db-spec {:dbtype "mysql"
              :host (env "DB_HOST" "db")
              :port (parse-long (env "DB_PORT" "3306"))
              :dbname (env "DB_NAME" "demo")
              :user (env "DB_USER" "root")
              :password (env "DB_PASSWORD" "")})

;; DB接続用のデータソース
;; テストでは、別のデータソースに差し替える。
(def ^:dynamic *datasource* (jdbc/get-datasource db-spec))

;; 結果の取り出し方。列の名前を、そのままキーワードにしたマップで受け取る（例: :due_date）。
(def ^:private options {:builder-fn result-set/as-unqualified-lower-maps})

;; SQLを実行して、結果をすべて返す関数
(defn execute! [sql-params]
  (jdbc/execute! *datasource* sql-params options))

;; SQLを実行して、結果の1件目だけを返す関数
(defn execute-one! [sql-params]
  (jdbc/execute-one! *datasource* sql-params options))
