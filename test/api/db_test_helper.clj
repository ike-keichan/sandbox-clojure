(ns api.db-test-helper
  (:require [api.db :as db]
            [api.migrate :as migrate]
            [next.jdbc :as jdbc]))

;; テスト用のDBの名前
;; 開発用のDB（例: demo）のデータを消さないように、名前の最後に _test を付けた、別のDBを使う。
(def ^:private test-db-name (str (:dbname db/db-spec) "_test"))

;; テスト用のDBを用意して、テストを実行する関数
;; テストごとに、全テーブルを作り直して、空の状態から始める。
;; DBそのものを作る処理は、接続先を切り替える前に行う必要があるので、ここだけは next.jdbc を直接使っている。
(defn with-test-database [test-fn]
  (jdbc/execute! (jdbc/get-datasource (assoc db/db-spec :dbname "mysql"))
                 [(str "CREATE DATABASE IF NOT EXISTS `" test-db-name "`")])
  (binding [db/*datasource* (jdbc/get-datasource (assoc db/db-spec :dbname test-db-name))]
    (migrate/reset-database)
    (test-fn)))
