;; Markdown の表を、列の幅をそろえた形に整えるプログラム
(ns table-formatter.core
  (:require [clojure.string :as string])
  (:gen-class))

;; 要素をフォーマットし、応答する関数
;;; ①要素が改行の場合、行の終わりの形にして応答する。
;;; ②要素が区切り（--- を含む）の場合、幅いっぱいの - にして応答する。
;;; ③それ以外の場合は、右側を空白で埋めて応答する。
(defn format-element [element width]
  (cond
    (string/includes? element "\n") "|\n"
    (string/includes? element "---") (str "| " (string/join (repeat width "-")) " ")
    :else (format (str "| %-" width "s ") element)))

;; テーブルをフォーマットし、応答する関数
;;; ①引数の文字列を | で分割し、要素の並びに変換する（行の境目は、改行だけの要素になる）。
;;; ②いちばん長い要素の長さを、すべての列の幅にする。
;;; ③要素ごとにフォーマットし、文字列として連結して応答する。
(defn format-table [lines]
  (let [elements (rest (mapv #(if (string/includes? % "\n") "\n" (string/trim %))
                             (string/split lines #"\|")))
        width (apply max (map count elements))]
    (string/join (conj (mapv #(format-element % width) elements) "|"))))

;; メイン関数
;;; 引数があれば、引数をつなげたものを、表として扱う。引数がなければ、標準入力から読み込む。
(defn -main [& arguments]
  (let [lines (if (seq arguments) (string/join arguments) (slurp *in*))]
    (try
      (println (format-table (string/trimr lines)))
      (catch Exception _
        (binding [*out* *err*]
          (println "エラー：入力フォーマットが違うよ(#°Д°)"))
        (System/exit 1)))))
