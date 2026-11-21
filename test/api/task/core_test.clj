(ns api.task.core-test
  (:require [api.core :refer [app]]
            [api.db-test-helper :refer [with-test-database]]
            [api.task.handler :refer [title-max-length]]
            [api.test-helper :refer [parse-body]]
            [clojure.string :as str]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [ring.mock.request :as mock]))

(use-fixtures :each with-test-database)

;; JSONの本文つきのリクエストを送る関数
(defn- request-json [method path body]
  (app (-> (mock/request method path)
           (mock/json-body body))))

;; テスト用に、タスクを1件作って、レスポンスの中身を返す関数
(defn- create-task [task]
  (let [response (request-json :post "/api/tasks" (merge {:title "テストタスク"} task))]
    (is (= 201 (:status response)))
    (parse-body response)))

(deftest test-create
  (testing "タスクを作ると、作ったタスクを201で返す"
    (let [response (request-json :post "/api/tasks" {:title "テストタスク" :due_date "2024-09-01"})]
      (is (= 201 (:status response)))
      (is (= {:id 1 :title "テストタスク" :due_date "2024-09-01" :is_done false}
             (parse-body response)))))

  (testing "期日は、なくてもよい"
    (is (nil? (:due_date (create-task {})))))

  (testing "タスク名は、上限ちょうどの長さまで使える"
    (let [title (str/join (repeat title-max-length "あ"))]
      (is (= title (:title (create-task {:title title})))))))

(deftest test-create-with-invalid-input
  (testing "値が正しくなければ、422を返し、タスクを作らない"
    (doseq [body [{}
                  {:title ""}
                  {:title "   "}
                  {:title nil}
                  {:title 123}
                  {:title (str/join (repeat (inc title-max-length) "あ"))}
                  {:title "テストタスク" :due_date "2024-12-32"}
                  {:title "テストタスク" :due_date "2024/12/01"}
                  {:title "テストタスク" :due_date "2024-1201"}
                  {:title "テストタスク" :due_date "+12345-01-01"}
                  {:title "テストタスク" :due_date 20241201}
                  [1 2]
                  "テストタスク"]]
      (let [response (request-json :post "/api/tasks" body)]
        (is (= 422 (:status response)) (pr-str body))
        (is (= {:detail "Invalid task"} (parse-body response)) (pr-str body))))
    (is (= [] (parse-body (app (mock/request :get "/api/tasks"))))))

  (testing "本文がない、またはJSONでなければ、422を返す"
    (is (= 422 (:status (app (mock/request :post "/api/tasks")))))
    (is (= 422 (:status (app (-> (mock/request :post "/api/tasks")
                                 (mock/body "title=テストタスク"))))))))

(deftest test-get
  (testing "タスクを1件取得できる"
    (let [task (create-task {})
          response (app (mock/request :get (str "/api/tasks/" (:id task))))]
      (is (= 200 (:status response)))
      (is (= task (parse-body response))))))

(deftest test-list
  (testing "一覧を、IDの小さい順に取得できる"
    (let [first-task (create-task {:title "1つ目"})
          second-task (create-task {:title "2つ目"})
          response (app (mock/request :get "/api/tasks"))]
      (is (= 200 (:status response)))
      (is (= [first-task second-task] (parse-body response)))))

  (testing "完了状態で絞り込める"
    (let [done-task (parse-body (app (mock/request :put "/api/tasks/1/done")))]
      (is (= [done-task] (parse-body (app (mock/request :get "/api/tasks?is_done=true")))))
      (is (= ["2つ目"] (map :title (parse-body (app (mock/request :get "/api/tasks?is_done=false"))))))))

  (testing "is_done が true と false 以外なら、422を返す"
    (doseq [query ["is_done=abc" "is_done=1" "is_done=" "is_done=true&is_done=false"]]
      (let [response (app (mock/request :get (str "/api/tasks?" query)))]
        (is (= 422 (:status response)) query)
        (is (= {:detail "Invalid is_done"} (parse-body response)) query)))))

(deftest test-update
  (testing "タスクを更新できる。渡さなかった期日は、空になる"
    (let [task (create-task {:due_date "2024-09-01"})
          response (request-json :put (str "/api/tasks/" (:id task)) {:title "更新したタスク"})]
      (is (= 200 (:status response)))
      (is (= (assoc task :title "更新したタスク" :due_date nil) (parse-body response)))))

  (testing "値が正しくなければ、422を返し、タスクを変えない"
    (let [before (parse-body (app (mock/request :get "/api/tasks/1")))]
      (is (= 422 (:status (request-json :put "/api/tasks/1" {:title ""}))))
      (is (= 422 (:status (app (mock/request :put "/api/tasks/1")))))
      (is (= before (parse-body (app (mock/request :get "/api/tasks/1"))))))))

(deftest test-delete
  (testing "タスクを削除すると、本文なしの204を返す"
    (let [task (create-task {})
          response (app (mock/request :delete (str "/api/tasks/" (:id task))))]
      (is (= 204 (:status response)))
      (is (nil? (:body response)))
      (is (= 404 (:status (app (mock/request :get (str "/api/tasks/" (:id task))))))))))

(deftest test-done
  (testing "完了にして、未完了に戻せる。同じ操作を2回呼んでも、結果は変わらない"
    (let [task (create-task {})
          path (str "/api/tasks/" (:id task) "/done")]
      (let [response (app (mock/request :put path))]
        (is (= 200 (:status response)))
        (is (= (assoc task :is_done true) (parse-body response))))
      (is (true? (:is_done (parse-body (app (mock/request :put path))))))
      (let [response (app (mock/request :delete path))]
        (is (= 200 (:status response)))
        (is (= task (parse-body response))))
      (is (false? (:is_done (parse-body (app (mock/request :delete path)))))))))

(deftest test-not-found
  (testing "存在しないタスクは、404を返す"
    (doseq [request [(mock/request :get "/api/tasks/999")
                     (-> (mock/request :put "/api/tasks/999") (mock/json-body {:title "テストタスク"}))
                     (mock/request :delete "/api/tasks/999")
                     (mock/request :put "/api/tasks/999/done")
                     (mock/request :delete "/api/tasks/999/done")
                     ;; IDが大きすぎて数値にできない場合
                     (mock/request :get "/api/tasks/99999999999999999999999")]]
      (let [response (app request)]
        (is (= 404 (:status response)) (:uri request))
        (is (= {:detail "Task not found"} (parse-body response)) (:uri request)))))

  (testing "IDが数字でなければ、ルーティングに一致せず、404を返す"
    (let [response (app (mock/request :get "/api/tasks/abc"))]
      (is (= 404 (:status response)))
      (is (= {:detail "Not Found"} (parse-body response)))))

  (testing "値が正しくないときは、タスクが存在しなくても、422を先に返す"
    (is (= 422 (:status (request-json :put "/api/tasks/999" {:title ""}))))))
