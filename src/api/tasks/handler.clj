(ns api.tasks.handler
  (:require [clojure.java.jdbc :as jdbc]
            [cheshire.core :as json]
            [ring.util.response :refer [response status]]
            [ring.middleware.params :refer [wrap-params]]
            [api.db :refer [db-spec]]
            [clojure.string :as str]
            [utils.convert-json-keys-to-camel-case.core :refer [convert-json-keys]])
  (:import [java.time.format DateTimeFormatter]
           [java.time LocalDate]))

(defn- format-date [date]
  (when date
    (.format date (DateTimeFormatter/ofPattern "yyyy-MM-dd"))))

(defn- prepare-for-json [data]
  (clojure.walk/postwalk
    (fn [x]
      (cond
        (instance? LocalDate x) (format-date x)
        :else x))
    data))

(defn- wrap-response [body status]
  (println "Debug - wrap-response input:" (pr-str body))
  (println "Debug - wrap-response input type:" (type body))
  (let [converted-body (cond
                         (map? body) (convert-json-keys body)
                         (and (sequential? body) (not-empty body)) (mapv convert-json-keys body)
                         (empty? body) []
                         :else body)]
    (println "Debug - After convert-json-keys:" (pr-str converted-body))
    (let [prepared-body (prepare-for-json converted-body)]
      (println "Debug - After prepare-for-json:" (pr-str prepared-body))
      (let [json-string (json/generate-string prepared-body)]
        (println "Debug - Final JSON string:" json-string)
        (-> (response json-string)
            (status status)
            (update :headers assoc "Content-Type" "application/json"))))))

;; タスク一覧の取得
(defn get-tasks-handler [is-done]
  (let [parsed-is-done (when (not (str/blank? is-done)) (Boolean/parseBoolean is-done))
        query (if (nil? parsed-is-done)
                ["SELECT * FROM tasks"]
                ["SELECT * FROM tasks WHERE isDone = ?" parsed-is-done])
        _ (println "Debug - SQL query:" (pr-str query))
        tasks (jdbc/query db-spec query)]
    (println "Debug - Tasks from database:" (pr-str tasks))
    (println "Debug - Tasks type:" (type tasks))
    (if (empty? tasks)
      (do
        (println "Debug - No tasks found")
        (wrap-response [] 200))
      (do
        (doseq [task tasks]
          (println "Debug - Task:" (pr-str task))
          (println "Debug - Task type:" (type task)))
        (wrap-response tasks 200)))))

;; タスクの新規作成
(defn create-task-handler [request]
  (println "Debug - create-task-handler input:" (pr-str request))
  (let [params (:form-params request)
        _ (println "Debug - Form params:" (pr-str params))
        title (get params "title")
        _ (println "Debug - Title:" title)
        new-task {:title title :dueDate (LocalDate/now) :isDone false}
        _ (println "Debug - New task:" (pr-str new-task))
        result (jdbc/insert! db-spec :tasks new-task)
        _ (println "Debug - Insert result:" (pr-str result))
        created-task (assoc new-task :id (:generated_key (first result)))
        _ (println "Debug - Created task:" (pr-str created-task))]
    (wrap-response created-task 201)))

;; タスクの取得
(defn get-task-handler [id]
  (try
    (let [result (jdbc/query db-spec ["SELECT * FROM tasks WHERE id = ?" id])]
      (if (empty? result)
        (wrap-response {:error "Task not found"} 404)
        (wrap-response (first result) 200)))
    (catch Exception e
      (wrap-response {:error "Internal Server Error"} 500))))

;; タスクの完了状態を切り替え
(defn toggle-is-done-of-task-handler [id]
  (let [task (first (jdbc/query db-spec ["SELECT * FROM tasks WHERE id = ?" id]))]
    (if task
      (let [updated-task (update task :isDone not)
            _ (jdbc/update! db-spec :tasks updated-task ["id = ?" id])
            result (first (jdbc/query db-spec ["SELECT * FROM tasks WHERE id = ?" id]))]
        (wrap-response result 200))
      (wrap-response {:error "Task not found"} 404))))
