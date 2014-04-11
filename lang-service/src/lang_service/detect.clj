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
    (.append detector text)
    (.detect detector)))

(defn detect-probabilities
  "return the list of lang,probabilities pair for the given text"
  [text]
  (let [detector (DetectorFactory/create)
        probabilities (do
                        (.append detector text)
                        (.getProbabilities detector))]
    (map (fn [x] [(.lang x) (.prob x)]) probabilities)))

(defn detect-error
  "return an error message when the function name passed is invalid"
  [text]
  "ERROR")

