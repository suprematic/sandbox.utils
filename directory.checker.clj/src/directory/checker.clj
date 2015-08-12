(ns directory.checker
  (:gen-class
    :name directory.checker
    :methods [#^{:static true} [createIndex [String] java.util.Map]
              #^{:static true} [isIndexValid [java.util.Map String] boolean]])
  (:import (java.security MessageDigest)
           (java.io DataInputStream)
           (clojure.lang IPersistentMap))
  (:require
    [clojure.java.io :as io]
    [clojure.string :as str]
    [clojure.set :as set :refer [difference intersection]]))

;; general helper functions

(defn md5
  "Returns MD5 hash for the given byte array"
  [bytes]
  (let [md (MessageDigest/getInstance "MD5")
        digest (.digest (doto md .reset (.update bytes)))]
    (apply str (map (partial format "%02x") digest))))

;; io helper functions

(defn file-to-byte-array
  [file]
  (let [result (byte-array (.length file))]
    (with-open [in (DataInputStream. (io/input-stream file))]
      (.readFully in result))
    result))

(defn list-files
  "Evaluates to a seq of files in the given dir"
  [dir]
  (filter #(.isFile %) (file-seq (io/file dir))))

;; API

(defn create-index
  "Evaluates to a map of all relative file names in the given dir to their corresponding MD5 hash"
  [dir]
  (let [files (list-files dir)

        dir-abs-path (.getAbsolutePath (io/file dir))
        relative-paths (pmap #(str/replace-first (.getAbsolutePath %) dir-abs-path "") files)

        hashes (pmap #(md5 (file-to-byte-array %)) files)]
    (zipmap relative-paths hashes)))

(defn index-valid?
  "Evaluates to true if the given dir has no changes compared to the given index"
  [index-old dir]
  (let [index-cur (create-index dir)

        files-cur (into #{} (keys index-cur))
        files-old (into #{} (keys index-old))
        added-files (difference files-cur files-old)
        deleted-files (difference files-old files-cur)

        files-same (intersection files-old files-cur)

        changed-files (reduce (fn[a f]
                                (if (= (get index-old f) (get index-cur f))
                                  a
                                  (conj a f)))
                              #{}
                              files-same)

        valid? (empty? (concat added-files deleted-files changed-files))]

    (when-not valid?
      (let [trace (fn [files msg]
                    (binding [*out* *err*] ;print to error stream
                      (println (count files) msg)
                      (doall (map #(println "\t" %) files))))]
        (trace added-files "aditions")
        (trace deleted-files "deletions")
        (trace changed-files "changes")))

    valid?))


;; java callable wrappers

(defn -createIndex
  "A Java-callable wrapper around the 'create-index' function."
  [dir]
  (create-index dir))

(defn -isIndexValid
  "A Java-callable wrapper around the 'index-valid?' function."
  [index dir]
  (index-valid? index dir))

;; usage example

(def test-dir "c:\\tmp\\test")
(def jdk-dir "c:\\ishinka\\bin\\jdk1.8.0_11")
(def index-file "c:\\tmp\\index.edn")
; creates the index and writes to file
(spit index-file (create-index test-dir))
; validates the index
(index-valid? (load-file index-file) test-dir)