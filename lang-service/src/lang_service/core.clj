(ns lang-service.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [lang-service.detect :as detect]
            [langohr.core :as mbroker]
            [langohr.channel :as channel]))

(def cli-opts
  [["-H" "--host HOST" "hostname for the message broker. Defaults to localhost"
    :default "localhost"]
   ["-P" "--port PORT" "port the message borker listens on"
    :default 5672 :parse-fn #(Integer/parseInt %1) 
    :validate [#(< 0 %1 < 65536) "a port number must be between 0 and 65536"]]
   ["-q" "--queue QUEUE" "name of the queue from which the texts are pumped"
    :default "lang_service_queue"]
   ["-e" "--exchange EXCHANGE" (str "name of the exchange to bind the queue to"
                                   " Defaults to ''")
    :default ""]
   ["-u" "--username USERNAME" "username used to connect to the mbroker"]
   ["-p" "--password PASSWORD" "password used to connect to the mbroker"]]) 


(defn message-callback
  "Receives an AMQP message and dispatches it to the right function. 
  Waits for the result and writes it back to the result queue. ACK the
  message at the end."
  [ch {:keys [content-type delevery-tag type] :as meta} ^bytes payload]
  (println "received message!")
  ; Acknowledge the message.
  nil)

(defn -main
  "sets up the service and accept messages"
  [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-opts)]
    (when-not (options :host)
      (println (str "You must specify the hostname of a message broker. " 
               "Please see below usage instruction and try again."))
      (println summary)
      (System/exit -1))
    (when-not (and (options :username) (options :password))
      (println (str "username/password is mandatory. "
                    "Please see below usage instruction and try again."))
      (println summary)
      (System/exit -1))

    ;(let [{:keys [host port queue exchange]} options]
    ;  (let [conn (mbroker/connect {:host host :port port})
    ;        ch (channel/open conn)]
    ))








