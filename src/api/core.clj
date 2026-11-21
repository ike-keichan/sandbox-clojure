(ns api.core
  (:require [api.hello.core :refer [hello-routes]]
            [api.task.core :refer [task-routes]]
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
    hello-routes
    task-routes)
  (route/not-found {:body {:detail "Not Found"}}))

;; ミドルウェアの適用
;; リクエストの JSON をキーワードのマップに変え、レスポンスのマップを JSON に変える。
(def app
  (-> app-routes
      (wrap-json-body {:keywords? true})
      wrap-params
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
