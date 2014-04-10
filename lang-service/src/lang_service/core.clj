(ns lang-service.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [lang-service.detect :as detect]))

(def cli-opts
  [["-H" "--host HOST" "hostname for the message broker"]
   ["-p" "--port PORT" "port the message borker listens on"
    :default 5672 :parse-fn #(Integer/parseInt %1) 
    :validate [#(< 0 %1 < 65536) "a port number must be between 0 and 65536"]]
   ["-q" "--queue QUEUE" "name of the queue from which the texts are pumped"
    :default "lang_service_queue"]])

(defn -main
  "sets up the service and accept messages"
  [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-opts)]
    (when-not (options :host)
      (println (str "You must specify the hostname of a message broker. " 
               "Please see below usage instruction and try again."))
      (println summary)
      (System/exit -1))
    (doseq [[k v] options]
      (println (str k " " v)))))
