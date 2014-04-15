(ns lang-service.detect
  (:import [com.cybozu.labs.langdetect Detector DetectorFactory Language]))

; TODO: refactor this into something sensible please ....
(defonce 
  detector-factory 
  (DetectorFactory/loadProfile 
     "/root/misc/lang/resources/language-detection/profiles"))

(defn detect-language
  "return the most probable language of the given text."
  [text]
  (let [detector (DetectorFactory/create)]
    (try
      (.append detector text)
      (.detect detector)
    (catch com.cybozu.labs.langdetect.LangDetectException e
      "UNKNOWN"))))


(defn detect-probabilities
  "return the list of lang,probabilities pair for the given text"
  [text]
  (let [detector (DetectorFactory/create)
        probabilities (try
                        (.append detector text)
                        (.getProbabilities detector)
                      (catch com.cybozu.labs.langdetect.LangDetectException e
                        (println (format "problem with %s" text))
                        nil))]
    (if (nil? probabilities)
      [[0 "UNKNOWN"]]
      (map (fn [x] [(.prob x) (.lang x)]) probabilities))))

(defn detect-error
  "return an error message when the function name passed is invalid"
  [text]
  "ERROR")

