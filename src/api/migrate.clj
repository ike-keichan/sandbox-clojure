(ns api.migrate
  (:require [api.db :as db]
            [clojure.tools.logging :as log]))

;; テーブルを作るSQLの一覧
;; テーブルを足したら、ここに追加する。
;; 例: ["tasks" "CREATE TABLE tasks (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(1024))"]
(def tables [])

;; DBをリセットする関数
;; 現在のDBの全テーブルを削除し、再度作成する。
(defn reset-database []
  (doseq [[name _] (reverse tables)]
    (db/execute! [(str "DROP TABLE IF EXISTS " name)])
    (log/info "Dropped the table" name))
  (doseq [[name create-sql] tables]
    (db/execute! [create-sql])
    (log/info "Created the table" name)))

;; メイン関数
(defn -main [& _]
  (db/execute-one! ["SELECT 1"])
  (log/info "Connected to the database" (:dbname db/db-spec))
  (reset-database)
  (log/info "Finished the migration"))
