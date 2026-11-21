(ns api.db-test
  (:require [api.db :as db]
            [api.db-test-helper :refer [with-test-database]]
            [clojure.test :refer [deftest is testing use-fixtures]]))

(use-fixtures :each with-test-database)

;; tasks テーブルの行の数を数える関数
(defn- count-tasks []
  (:total (db/execute-one! ["SELECT COUNT(*) AS total FROM tasks"])))

(deftest test-with-transaction
  (testing "最後まで実行できたら、変更が残る"
    (db/with-transaction
      (db/execute-one! ["INSERT INTO tasks (title) VALUES (?)" "1つ目"])
      (db/execute-one! ["INSERT INTO tasks (title) VALUES (?)" "2つ目"]))
    (is (= 2 (count-tasks))))

  (testing "途中で例外が起きたら、それまでの変更が取り消される"
    (is (thrown? Exception
                 (db/with-transaction
                   (db/execute-one! ["INSERT INTO tasks (title) VALUES (?)" "3つ目"])
                   (throw (ex-info "failed" {})))))
    (is (= 2 (count-tasks)))))
