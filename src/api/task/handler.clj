(ns api.task.handler
  (:require [api.task.repository :as repository]
            [ring.util.response :as response])
  (:import [java.time LocalDate]
           [java.time.format DateTimeParseException]))

;; エラーのレスポンスを作る関数
(defn- error-response [status-code detail]
  (-> (response/response {:detail detail})
      (response/status status-code)))

;; タスクが見つからないときのレスポンス
(def ^:private not-found (error-response 404 "Task not found"))

;; 期日の文字列を、日付に変える関数
;; 期日がなければ nil を返す。"2024-09-01" の形でなければ、:invalid を返す。
(defn- parse-due-date [due-date]
  (cond
    (nil? due-date) nil
    (string? due-date) (try
                         (LocalDate/parse due-date)
                         (catch DateTimeParseException _ :invalid))
    :else :invalid))

;; リクエストの本文から、タスクの項目を取り出す関数
;; 値が正しくなければ nil を返す。
(defn- body->task [body]
  (let [title (:title body)
        due-date (parse-due-date (:due_date body))]
    (when (and (or (nil? title) (string? title))
               (not= :invalid due-date))
      {:title title :due-date due-date})))

;; タスク一覧の取得
(defn get-tasks-handler [is-done]
  (response/response (repository/find-tasks (some-> is-done parse-boolean))))

;; タスクの新規作成
(defn create-task-handler [body]
  (if-let [task (body->task body)]
    (-> (response/response (repository/create-task! task))
        (response/status 201))
    (error-response 422 "Invalid task")))

;; タスクの取得
(defn get-task-handler [id]
  (if-let [task (repository/find-task id)]
    (response/response task)
    not-found))

;; タスクの更新
(defn update-task-handler [id body]
  (let [task (body->task body)]
    (cond
      (nil? task) (error-response 422 "Invalid task")
      (nil? (repository/find-task id)) not-found
      :else (response/response (repository/update-task! id task)))))

;; タスクの削除
(defn delete-task-handler [id]
  (if (repository/find-task id)
    (do
      (repository/delete-task! id)
      (response/status 204))
    not-found))

;; タスクの完了状態の切り替え
(defn toggle-is-done-handler [id]
  (if-let [task (repository/find-task id)]
    (response/response (repository/update-is-done! id (not (:is_done task))))
    not-found))
