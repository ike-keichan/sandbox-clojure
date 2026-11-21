(ns api.hello.core-test
  (:require [api.core :refer [app]]
            [api.test-helper :refer [parse-body]]
            [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as mock]))

(deftest test-hello
  (testing "GET /api/hello は、メッセージを返す"
    (let [response (app (mock/request :get "/api/hello"))]
      (is (= 200 (:status response)))
      (is (= {:message "hello world!"} (parse-body response))))))
