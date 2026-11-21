(ns api.core
  (:require [api.hello.core :refer [hello-routes]]
            [api.response :refer [error-response]]
            [clojure.tools.logging :as log]
            [compojure.core :refer [context defroutes]]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]))

;; 全体のルーティング
;; 機能を足したら、ここにルーティングを追加する。
(defroutes app-routes
  (context "/api" []
    hello-routes)
  (route/not-found (error-response 404 "Not Found")))

;; 想定していない例外を受け止めるミドルウェア
;; 例外をそのままにすると、サーバーが、内部の情報を含んだ HTML のエラー画面を返してしまう。
;; 内容は記録に出して、利用者には、決まった形の500だけを返す。
(defn- wrap-exception [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error e "Unhandled exception")
        (error-response 500 "Internal Server Error")))))

;; ミドルウェアの適用
;; 下に書いたものほど外側になり、リクエストを先に受け取る。
;; wrap-json-response を一番外側にして、エラーのレスポンスも JSON になるようにしている。
(def app
  (-> app-routes
      ;; リクエストの JSON を、キーワードのマップに変える。JSON として読めなければ、400 を返す。
      (wrap-json-body {:keywords? true
                       :malformed-response (error-response 400 "Malformed JSON")})
      wrap-params
      wrap-exception
      ;; レスポンスのマップを、JSON に変える。
      wrap-json-response))

;; 開発中かどうか
(defn- development? []
  (= "development" (System/getenv "APP_ENV")))

;; サーバーに渡すハンドラ
;; 開発中は、保存したファイルを再読み込みする（サーバーを起動し直さなくてよい）。
(defn- handler []
  (if (development?)
    ((requiring-resolve 'ring.middleware.reload/wrap-reload) #'app)
    app))

;; ポート番号
(defn- port []
  (parse-long (or (System/getenv "PORT") "3000")))

;; メイン関数
(defn -main [& _]
  (log/info "Starting the server on port" (port))
  (run-jetty (handler) {:port (port)}))
