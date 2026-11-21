(ns api.hello.core
  (:require [api.hello.handler :refer [get-hello-handler]]
            [compojure.core :refer [GET defroutes]]))

;; hello APIのルーティング
(defroutes hello-routes
  (GET "/hello" [] (get-hello-handler)))
