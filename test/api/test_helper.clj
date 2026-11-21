(ns api.test-helper
  (:require [api.db :as db]
            [api.migrate :as migrate]
            [cheshire.core :as json]
            [next.jdbc :as jdbc]))

;; テスト用のDBの名前
;; 開発用のDB（demo）のデータを消さないように、別のDBを使う。
(def ^:private test-db-name "demo_test")

;; テスト用のDBを用意して、テストを実行する関数
;; テストごとに、全テーブルを作り直して、空の状態から始める。
(defn with-test-database [test-fn]
  (jdbc/execute! (jdbc/get-datasource (assoc db/db-spec :dbname "mysql"))
                 [(str "CREATE DATABASE IF NOT EXISTS " test-db-name)])
  (binding [db/*datasource* (jdbc/get-datasource (assoc db/db-spec :dbname test-db-name))]
    (migrate/reset-database)
    (test-fn)))

;; レスポンスのJSONを、キーワードのマップに変える関数
(defn parse-body [response]
  (json/parse-string (:body response) true))
