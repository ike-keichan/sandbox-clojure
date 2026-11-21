(ns api.hello.handler
  (:require [ring.util.response :refer [response]]))

;; hello取得
(defn get-hello-handler []
  (response {:message "hello world!"}))
