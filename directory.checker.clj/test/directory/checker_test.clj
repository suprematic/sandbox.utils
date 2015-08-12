(ns directory.checker-test
  (:require [clojure.test :refer :all]
            [directory.checker :refer :all]))

(deftest simple-test
  (testing "Simple scenarios"
    (is (index-valid? (create-index ".") "."))))
