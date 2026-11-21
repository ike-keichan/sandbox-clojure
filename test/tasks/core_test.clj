(ns tasks.core-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]
            [utils.mock-request.core :refer [mock-request]]
            [api.tasks.core :refer [task-routes]]))

(deftest test-get-tasks
  (testing "GET /api/tasks"
    (let [response (task-routes (mock-request :get "/tasks"))
          body (json/parse-string (slurp (:body response)) true)]
      (is (= 200 (:status response)))
      (is (vector? body)))))

(deftest test-add-task
  (testing "POST /api/tasks"
    (let [response (task-routes (mock-request :post "/tasks" :body "title=New Task"))
          body (json/parse-string (slurp (:body response)) true)]
      (is (= 200 (:status response)))
      (is (vector? body)))))

(deftest test-get-task-by-id
  (testing "GET /api/tasks/:id"
    (let [create-response (task-routes (mock-request :post "/tasks" :body "title=New Task"))
          task-id (get-in (json/parse-string (slurp (:body create-response)) true) [0 :id])
          response (task-routes (mock-request :get (str "/tasks/" task-id)))
          body (json/parse-string (slurp (:body response)) true)]
      (is (= 200 (:status response)))
      (is (= task-id (:id body))))))

(deftest test-update-task
  (testing "PUT /api/tasks/:id"
    (let [create-response (task-routes (mock-request :post "/tasks" :body "title=Old Task"))
          task-id (get-in (json/parse-string (slurp (:body create-response)) true) [0 :id])
          response (task-routes (mock-request :put (str "/tasks/" task-id) :body "title=Updated Task&dueDate=2024-09-30"))
          body (json/parse-string (slurp (:body response)) true)]
      (is (= 200 (:status response)))
      (is (= "Updated Task" (:title body))))))

(deftest test-delete-task
  (testing "DELETE /api/tasks/:id"
    (let [create-response (task-routes (mock-request :post "/tasks" :body "title=To be deleted"))
          task-id (get-in (json/parse-string (slurp (:body create-response)) true) [0 :id])
          response (task-routes (mock-request :delete (str "/tasks/" task-id)))]
      (is (= 204 (:status response))))))
