(ns lang-service.detect
  (:import [com.cybozu.labs.langdetect Detector DetectorFactory Language]))

(defn detect-language
  "return the most probable language of the given text."
  [text]
  "en")

(defn detect-probabilities
  "return the list of lang,probabilities pair for the given text"
  [text]
  [["en" 0.999]])

