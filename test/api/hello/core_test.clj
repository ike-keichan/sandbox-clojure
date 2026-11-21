(ns api.hello.core-test
  (:require [api.core :refer [app]]
            [cheshire.core :as json]
            [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as mock]))

;; レスポンスのJSONを、キーワードのマップに変える関数
(defn- parse-body [response]
  (json/parse-string (:body response) true))

(deftest test-hello
  (testing "GET /api/hello"
    (let [response (app (mock/request :get "/api/hello"))]
      (is (= 200 (:status response)))
      (is (= {:message "hello world!"} (parse-body response))))))

(deftest test-not-found
  (testing "存在しないパス"
    (let [response (app (mock/request :get "/api/unknown"))]
      (is (= 404 (:status response)))
      (is (= {:detail "Not Found"} (parse-body response))))))
