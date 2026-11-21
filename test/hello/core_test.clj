(ns hello.core-test
  (:require [clojure.test :refer :all]
            [cheshire.core :as json]
            [utils.mock-request.core :refer [mock-request]]
            [api.hello.core :refer [hello-routes]]))

(deftest test-hello-world
  (testing "GET /api/hello"
    (let [response (hello-routes (mock-request :get "/hello"))
          body (json/parse-string (slurp (:body response)) true)]
      (is (= 200 (:status response)))
      (is (= {:message "hello world!"} body)))))
