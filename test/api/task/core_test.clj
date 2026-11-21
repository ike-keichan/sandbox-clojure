(ns api.task.core-test
  (:require [api.core :refer [app]]
            [api.test-helper :refer [parse-body with-test-database]]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mock]))

(use-fixtures :each with-test-database)

;; JSONの本文つきのリクエストを送る関数
(defn- request-json [method path body]
  (app (-> (mock/request method path)
           (mock/json-body body))))

(deftest test-create-and-read
  (testing "タスクを作って、一覧と1件の取得で読める"
    (let [response (request-json :post "/api/tasks" {:title "テストタスク" :due_date "2024-09-01"})]
      (is (= 201 (:status response)))
      (is (= {:id 1 :title "テストタスク" :due_date "2024-09-01" :is_done false}
             (parse-body response))))
    (let [response (app (mock/request :get "/api/tasks"))]
      (is (= 200 (:status response)))
      (is (= [{:id 1 :title "テストタスク" :due_date "2024-09-01" :is_done false}]
             (parse-body response))))
    (let [response (app (mock/request :get "/api/tasks/1"))]
      (is (= 200 (:status response)))
      (is (= "テストタスク" (:title (parse-body response)))))))

(deftest test-done-flag
  (testing "完了と未完了を切り替えて、一覧を絞り込める"
    (request-json :post "/api/tasks" {:title "テストタスク2"})
    (let [response (app (mock/request :put "/api/tasks/1/done"))]
      (is (= 200 (:status response)))
      (is (true? (:is_done (parse-body response)))))
    (is (= 1 (count (parse-body (app (mock/request :get "/api/tasks?is_done=true"))))))
    (is (= 0 (count (parse-body (app (mock/request :get "/api/tasks?is_done=false"))))))
    (let [response (app (mock/request :put "/api/tasks/1/done"))]
      (is (false? (:is_done (parse-body response)))))))

(deftest test-due-date
  (testing "期日の形が正しくなければ、422を返す"
    (doseq [[due-date expected] [["2024-12-01" 201]
                                 ["2024-12-32" 422]
                                 ["2024/12/01" 422]
                                 ["2024-1201" 422]]]
      (let [response (request-json :post "/api/tasks" {:title "テストタスク" :due_date due-date})]
        (is (= expected (:status response)) due-date)))))

(deftest test-update-and-delete
  (testing "タスクを更新して、削除できる"
    (request-json :post "/api/tasks" {:title "テストタスク3"})
    (let [response (request-json :put "/api/tasks/1" {:title "更新したタスク" :due_date "2024-12-01"})]
      (is (= 200 (:status response)))
      (is (= {:id 1 :title "更新したタスク" :due_date "2024-12-01" :is_done false}
             (parse-body response))))
    (is (= 204 (:status (app (mock/request :delete "/api/tasks/1")))))
    (is (= 404 (:status (app (mock/request :get "/api/tasks/1")))))))

(deftest test-not-found
  (testing "存在しないタスクは、404を返す"
    (is (= 404 (:status (app (mock/request :get "/api/tasks/999")))))
    (is (= 404 (:status (request-json :put "/api/tasks/999" {:title "x"}))))
    (is (= 404 (:status (app (mock/request :delete "/api/tasks/999")))))
    (is (= 404 (:status (app (mock/request :put "/api/tasks/999/done")))))
    (is (= {:detail "Task not found"} (parse-body (app (mock/request :get "/api/tasks/999")))))))
