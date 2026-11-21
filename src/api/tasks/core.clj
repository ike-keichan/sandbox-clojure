(ns api.tasks.core
  (:require [compojure.core :refer [defroutes GET POST PUT DELETE]]
            [api.tasks.handler :refer [get-tasks-handler create-task-handler get-task-handler toggle-is-done-of-task-handler]]))

;; tasks APIのルーティング
(defroutes task-routes
  (GET "/tasks" [isDone] (get-tasks-handler isDone))
  (POST "/tasks" request (create-task-handler request))
  (GET "/tasks/:id" [id] (get-task-handler (Integer/parseInt id)))
  ;; (PUT "/tasks/:id" [id title dueDate] (update-task-handler (Integer/parseInt id) title dueDate))
  ;; (DELETE "/tasks/:id" [id] (delete-task-handler (Integer/parseInt id)))
  (PUT "/tasks/:id/done" [id] (toggle-is-done-of-task-handler (Integer/parseInt id)))
)
