;; 依存するライブラリを足したり、バージョンを変えたりしたら、コンテナをビルドし直す（make build）。
(defproject sandbox-clojure "0.1.0-SNAPSHOT"
  :description "Clojure による Web API のひな形"
  :dependencies [[org.clojure/clojure "1.12.7"]                  ;; 言語
                 [ring/ring-core "1.15.6"]                       ;; HTTP の処理
                 [ring/ring-jetty-adapter "1.15.6"]              ;; サーバー（Jetty）
                 [ring/ring-json "0.5.1"]                        ;; JSON のリクエストとレスポンス
                 [cheshire "6.2.1"]                              ;; JSON の変換
                 [compojure "1.7.2"]                             ;; ルーティング
                 [com.github.seancorfield/next.jdbc "1.4.72"]    ;; データベースの操作
                 [com.mysql/mysql-connector-j "26.10.0"]         ;; MySQL のドライバ
                 [org.clojure/tools.logging "1.3.2"]             ;; ログ
                 [org.slf4j/slf4j-simple "2.0.20"]]              ;; ログの出力先
  :plugins [[dev.weavejester/lein-cljfmt "0.16.6"]               ;; 書式の検査と整形
            [com.github.clj-kondo/lein-clj-kondo "2026.10.26"]   ;; 規則の検査
            [lein-ancient "1.0.0"]]                              ;; 新しいバージョンの確認
  :source-paths ["src"]
  :test-paths ["test"]
  :main ^:skip-aot api.core
  :profiles {:dev {:dependencies [[ring/ring-devel "1.15.6"]     ;; 保存したファイルの再読み込み
                                  [ring/ring-mock "0.6.2"]]}})   ;; テスト用のリクエスト
