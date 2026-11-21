(ns api.migrate
  (:require [api.db :as db]
            [clojure.string :as str]
            [clojure.tools.logging :as log]))

;; テーブルを作るSQLの一覧
;; テーブルを足したら、ここに追加する。
;; 例: ["tasks" "CREATE TABLE tasks (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(1024) NOT NULL)"]
(def tables
  [["tasks" (str "CREATE TABLE tasks ("
                 "id INT AUTO_INCREMENT PRIMARY KEY, "
                 "title VARCHAR(1024) NOT NULL, "
                 "due_date DATE, "
                 "is_done BOOLEAN NOT NULL DEFAULT FALSE)")]])

;; DBにあるテーブルの名前を取得する関数
(defn- existing-table-names []
  (mapv :table_name
        (db/execute! [(str "SELECT table_name AS table_name FROM information_schema.tables "
                           "WHERE table_schema = DATABASE()")])))

;; まだ存在しないテーブルを作成する関数
;; すでにあるテーブルとデータは、そのまま残る。
(defn create-tables []
  (let [existing (set (existing-table-names))]
    (doseq [[table-name create-sql] tables]
      (if (contains? existing table-name)
        (log/info "Skipped the table (already exists)" table-name)
        (do
          (db/execute! [create-sql])
          (log/info "Created the table" table-name))))))

;; DBをリセットする関数
;; DBにある全テーブルを削除し、再度作成する。データは消える。
(defn reset-database []
  (let [table-names (existing-table-names)]
    (when (seq table-names)
      ;; まとめて1つの文で削除する。こうすると、テーブル同士が参照し合っていても、削除できる。
      (db/execute! [(str "DROP TABLE " (str/join ", " (map #(str "`" % "`") table-names)))])
      (log/info "Dropped the tables" (str/join ", " table-names))))
  (create-tables))

;; メイン関数
;; 引数に reset を渡すと、全テーブルを削除してから作り直す。
(defn -main [& args]
  (db/execute-one! ["SELECT 1"])
  (log/info "Connected to the database" (:dbname db/db-spec))
  (if (= "reset" (first args))
    (do
      (reset-database)
      (log/info "Reset the database"))
    (do
      (create-tables)
      (log/info "Created the tables"))))
