(ns utils.convert-json-keys-to-camel-case.core
  (:require [clojure.string :as str]))

(defn- ->camel-case [s]
  (if (keyword? s)
    (let [words (str/split (name s) #"-")
          capitalize-word (fn [w] (str/capitalize w))]
      (keyword (str (first words) (apply str (map capitalize-word (rest words))))))
    s))

(defn convert-json-keys [data]
  (println "Debug - convert-json-keys input:" (pr-str data))
  (println "Debug - convert-json-keys input type:" (type data))
  (let [result (cond
    (map? data)
    (reduce-kv (fn [m k v]
                 (println "Debug - Processing key:" k)
                 (assoc m (->camel-case k) (convert-json-keys v)))
               {}
               data)

    (sequential? data)
    (map convert-json-keys data)

    :else
    data)]
    (println "Debug - convert-json-keys output:" (pr-str result))
    (println "Debug - convert-json-keys output type:" (type result))
    result))
    