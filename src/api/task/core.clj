(ns api.task.core
  (:require [api.task.handler :as handler]
            [compojure.core :refer [DELETE GET POST PUT defroutes]]))

;; task APIのルーティング
;; :id は、数字だけを受け付ける。数字でなければ、ルーティングに一致せず、404になる。
(defroutes task-routes
  (GET "/tasks" [is_done]
    (handler/get-tasks-handler is_done))
  (POST "/tasks" {body :body}
    (handler/create-task-handler body))
  (GET "/tasks/:id{[0-9]+}" [id]
    (handler/get-task-handler (parse-long id)))
  (PUT "/tasks/:id{[0-9]+}" [id :as {body :body}]
    (handler/update-task-handler (parse-long id) body))
  (DELETE "/tasks/:id{[0-9]+}" [id]
    (handler/delete-task-handler (parse-long id)))
  (PUT "/tasks/:id{[0-9]+}/done" [id]
    (handler/toggle-is-done-handler (parse-long id))))
