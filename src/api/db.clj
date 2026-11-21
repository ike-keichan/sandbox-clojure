(ns api.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as result-set]))

;; 環境変数を読む関数。設定されていなければ、既定の値を返す。
(defn- env [variable-name default]
  (or (System/getenv variable-name) default))

;; DBの接続先。環境変数で上書きできる（docker-compose.yml を参照）。
(def db-spec {:dbtype "mysql"
              :host (env "DB_HOST" "db")
              :port (parse-long (env "DB_PORT" "3306"))
              :dbname (env "DB_NAME" "demo")
              :user (env "DB_USER" "root")
              :password (env "DB_PASSWORD" "")})

;; DB接続用のデータソース
;; テストでは、別のデータソースに差し替える。
;; 接続をためておく仕組み（コネクションプール）は、使っていない。SQLを実行するたびに、DBにつなぎ直す。
;; ひな形としては十分だが、利用が増えたら、HikariCP などのコネクションプールを足す。
(def ^:dynamic *datasource* (jdbc/get-datasource db-spec))

;; 結果の取り出し方。列の名前を、そのままキーワードにしたマップで受け取る（例: :due_date）。
(def ^:private options {:builder-fn result-set/as-unqualified-lower-maps})

;; SQLを実行して、結果をすべて返す関数
(defn execute! [sql-params]
  (jdbc/execute! *datasource* sql-params options))

;; SQLを実行して、結果の1件目だけを返す関数
;; INSERTで振られたIDを受け取るときは、追加のオプションに {:return-keys true} を渡す。
;; UPDATEとDELETEは、対象になった行の数を、{:next.jdbc/update-count 1} の形で返す。
(defn execute-one!
  ([sql-params]
   (execute-one! sql-params {}))
  ([sql-params extra-options]
   (jdbc/execute-one! *datasource* sql-params (merge options extra-options))))

;; UPDATEやDELETEの結果から、対象になった行の数を取り出す関数
(defn update-count [result]
  (:next.jdbc/update-count result))

;; 複数のSQLを、ひとまとまり（トランザクション）として実行するマクロ
;; 途中で例外が起きたら、それまでの変更は、すべて取り消される。
;; 例: (db/with-transaction
;;       (db/execute-one! ["UPDATE ..."])
;;       (db/execute-one! ["INSERT ..."]))
(defmacro with-transaction [& body]
  `(jdbc/with-transaction [transaction# *datasource*]
     (binding [*datasource* transaction#]
       ~@body)))
