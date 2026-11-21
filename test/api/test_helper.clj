(ns api.test-helper
  (:require [cheshire.core :as json]))

;; レスポンスのJSONを、キーワードのマップに変える関数
(defn parse-body [response]
  (json/parse-string (:body response) true))
