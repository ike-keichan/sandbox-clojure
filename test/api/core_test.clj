(ns api.core-test
  (:require [api.core :refer [app]]
            [api.hello.handler :as hello-handler]
            [api.test-helper :refer [parse-body]]
            [clojure.test :refer [deftest is testing]]
            [ring.mock.request :as mock]))

(deftest test-not-found
  (testing "存在しないパスは、404をJSONで返す"
    (let [response (app (mock/request :get "/api/unknown"))]
      (is (= 404 (:status response)))
      (is (= "application/json; charset=utf-8" (get-in response [:headers "Content-Type"])))
      (is (= {:detail "Not Found"} (parse-body response))))))

(deftest test-malformed-json
  (testing "JSONとして読めない本文は、400をJSONで返す"
    (let [response (app (-> (mock/request :post "/api/hello")
                            (mock/content-type "application/json")
                            (mock/body "{\"message\": ")))]
      (is (= 400 (:status response)))
      (is (= {:detail "Malformed JSON"} (parse-body response))))))

(deftest test-unhandled-exception
  (testing "想定していない例外は、内容を出さずに、500をJSONで返す"
    (with-redefs [hello-handler/get-hello-handler (fn [] (throw (ex-info "secret" {})))]
      (let [response (app (mock/request :get "/api/hello"))]
        (is (= 500 (:status response)))
        (is (= {:detail "Internal Server Error"} (parse-body response)))))))
