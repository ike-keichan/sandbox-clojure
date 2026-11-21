(ns api.response
  (:require [ring.util.response :as response]))

;; エラーのレスポンスを作る関数
;; エラーの本文は、どれも {:detail "<内容>"} の形にそろえる。
(defn error-response [status-code detail]
  (-> (response/response {:detail detail})
      (response/status status-code)))
