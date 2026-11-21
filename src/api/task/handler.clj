(ns api.task.handler
  (:require [api.response :refer [error-response]]
            [api.task.repository :as repository]
            [clojure.string :as str]
            [ring.util.response :as response])
  (:import [java.time LocalDate]
           [java.time.format DateTimeParseException]))

;; タスク名の長さの上限。テーブルの定義（api.migrate の VARCHAR(1024)）と、同じ値にする。
(def title-max-length 1024)

;; タスクが見つからないときのレスポンス
(def ^:private not-found (error-response 404 "Task not found"))

;; タスク名が正しいかを確かめる関数
;; 文字列で、空白だけではなく、長さが上限以下であること。
(defn- valid-title? [title]
  (and (string? title)
       (not (str/blank? title))
       (<= (count title) title-max-length)))

;; 期日の文字列を、日付に変える関数
;; 期日がなければ nil を返す。正しくなければ :invalid を返す。
;; 正しいのは、"2024-09-01" の形で、年が 1000 から 9999 のもの（MySQL の DATE が扱える範囲）。
(defn- parse-due-date [due-date]
  (cond
    (nil? due-date) nil
    (string? due-date) (try
                         (let [date (LocalDate/parse due-date)]
                           (if (<= 1000 (.getYear date) 9999)
                             date
                             :invalid))
                         (catch DateTimeParseException _ :invalid))
    :else :invalid))

;; リクエストの本文から、タスクの項目を取り出す関数
;; 本文がJSONのオブジェクトでないときと、値が正しくないときは、nil を返す。
(defn- body->task [body]
  (when (map? body)
    (let [title (:title body)
          due-date (parse-due-date (:due_date body))]
      (when (and (valid-title? title)
                 (not= :invalid due-date))
        {:title title :due-date due-date}))))

;; 検索条件の is_done を、真偽値に変える関数
;; 指定がなければ nil を返す。"true" と "false" 以外は、:invalid を返す。
(defn- parse-is-done [is-done]
  (case is-done
    nil nil
    "true" true
    "false" false
    :invalid))

;; タスク一覧の取得
(defn get-tasks-handler [is-done]
  (let [parsed (parse-is-done is-done)]
    (if (= :invalid parsed)
      (error-response 422 "Invalid is_done")
      (response/response (repository/find-tasks parsed)))))

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
  (if-let [task (body->task body)]
    (if-let [updated (repository/update-task! id task)]
      (response/response updated)
      not-found)
    (error-response 422 "Invalid task")))

;; タスクの削除
(defn delete-task-handler [id]
  (if (repository/delete-task! id)
    (response/status 204)
    not-found))

;; タスクの完了状態の変更
;; is-done が true なら完了に、false なら未完了にする。
(defn change-is-done-handler [id is-done]
  (if-let [task (repository/update-is-done! id is-done)]
    (response/response task)
    not-found))
