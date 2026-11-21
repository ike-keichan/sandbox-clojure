(ns api.task.repository
  (:require [api.db :as db]))

;; DBの行を、レスポンス用のマップに変える関数
;; 完了状態は真偽値に、期日は "2024-09-01" の形の文字列にそろえる。
(defn- row->task [row]
  (when row
    (-> row
        (update :is_done #(if (boolean? %) % (not (zero? %))))
        (update :due_date #(some-> % str)))))

;; タスク一覧の取得
;; is-done が nil でなければ、完了状態で絞り込む。
(defn find-tasks [is-done]
  (mapv row->task
        (if (nil? is-done)
          (db/execute! ["SELECT * FROM tasks ORDER BY id"])
          (db/execute! ["SELECT * FROM tasks WHERE is_done = ? ORDER BY id" is-done]))))

;; タスクの取得
;; 存在しない場合は nil を返す。
(defn find-task [id]
  (row->task (db/execute-one! ["SELECT * FROM tasks WHERE id = ?" id])))

;; タスクの新規作成
;; 作成したタスクを返す。
(defn create-task! [{:keys [title due-date]}]
  (let [result (db/execute-one! ["INSERT INTO tasks (title, due_date) VALUES (?, ?)" title due-date]
                                {:return-keys true})]
    (find-task (:generated_key result))))

;; タスクの更新
;; 更新したタスクを返す。
(defn update-task! [id {:keys [title due-date]}]
  (db/execute-one! ["UPDATE tasks SET title = ?, due_date = ? WHERE id = ?" title due-date id])
  (find-task id))

;; タスクの完了状態の更新
;; 更新したタスクを返す。
(defn update-is-done! [id is-done]
  (db/execute-one! ["UPDATE tasks SET is_done = ? WHERE id = ?" is-done id])
  (find-task id))

;; タスクの削除
(defn delete-task! [id]
  (db/execute-one! ["DELETE FROM tasks WHERE id = ?" id]))
