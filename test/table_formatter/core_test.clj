(ns table-formatter.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [table-formatter.core :refer [format-element format-table]]))

;; format-element 関数のテスト
(deftest test-format-element
  (testing "通常,要素幅6"
    (is (= (format-element "text" 6) "| text   ")))
  (testing "デリミタ,要素幅6"
    (is (= (format-element "---" 6) "| ------ ")))
  (testing "改行,要素幅6"
    (is (= (format-element "\n" 6) "|\n")))
  (testing "通常,要素幅10"
    (is (= (format-element "text" 10) "| text       ")))
  (testing "デリミタ,要素幅10"
    (is (= (format-element "---" 10) "| ---------- ")))
  (testing "改行,要素幅10"
    (is (= (format-element "\n" 10) "|\n"))))

;; format-table 関数のテスト
(deftest test-format-table
  (testing "Sample1"
    (is (= (format-table
            "| header1 | header2 |
| ------- | --- |
| 1  | 2          |
| 3     | 4 |")
           "| header1 | header2 |
| ------- | ------- |
| 1       | 2       |
| 3       | 4       |")))
  (testing "Sample2"
    (is (=  (format-table
             "| header1 | header2 |
|------ | --- |
| 1  |          |
| 3     | 4 |")
            "| header1 | header2 |
| ------- | ------- |
| 1       |         |
| 3       | 4       |")))
  (testing "Sample3"
    (is (=  (format-table
             "|             artist name|    url|
| --- | ------ |
|Tame Impala                  | https://tameimpala.com/        |
|     cocteau twins|  https://cocteautwins.com        |
|Fishmans|                               http://www.fishmans.jp/                             |")
            "| artist name              | url                      |
| ------------------------ | ------------------------ |
| Tame Impala              | https://tameimpala.com/  |
| cocteau twins            | https://cocteautwins.com |
| Fishmans                 | http://www.fishmans.jp/  |")))
  (testing "Sample4"
    (is (=  (format-table
             "| header1 | header2 |
| ------- | --- |
| 1  | 2          |
| 3     |    4 |
| 5 | 6 |")
            "| header1 | header2 |
| ------- | ------- |
| 1       | 2       |
| 3       | 4       |
| 5       | 6       |")))
  (testing "Sample5"
    (is (=  (format-table
             "| header1 | header2 | header3 |
|------ | --- | ---- |
| 1  | 2         |    3   |
| 4     | 5 |      6|")
            "| header1 | header2 | header3 |
| ------- | ------- | ------- |
| 1       | 2       | 3       |
| 4       | 5       | 6       |"))))

;; 入力が表の形でないときのテスト
(deftest test-format-table-with-invalid-input
  (testing "空の文字列は、例外になる"
    (is (thrown? Exception (format-table ""))))
  (testing "| を含まない文字列は、例外になる"
    (is (thrown? Exception (format-table "header1 header2")))))
