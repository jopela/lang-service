(ns lang-service.core
  (:gen-class)
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.data.json :as json]
            [lang-service.detect :as detect]
            [langohr.core :as lbroker]
            [langohr.channel :as lchannel]
            [langohr.queue :as lqueue]
            [langohr.consumers :as lconsumers]
            [langohr.basic :as lbasic]))

(def cli-opts
  [["-H" "--host HOST" "hostname for the message broker. Defaults to localhost"
    :default "localhost"]
   ["-P" "--port PORT" "port the message borker listens on"
    :default 5672 :parse-fn #(Integer/parseInt %1) 
    :validate [#(< 0 %1 < 65536) "a port number must be between 0 and 65536"]]
   ["-q" "--queue QUEUE" "name of the queue from which the texts are pumped"
    :default "lang_service_queue"]
   ["-u" "--username USERNAME" "username used to connect to the mbroker"]
   ["-p" "--password PASSWORD" "password used to connect to the mbroker"]]) 

(defn message-callback
  "Receives an AMQP message and dispatches it to the right function. 
  Waits for the result and writes it back to the result queue. ACK the
  message at the end."
  [ch {:keys [content-type delivery-tag type] :as metastuff} ^bytes payload]
  (let [message (-> payload (String. "UTF-8") json/read-str)
        {function "function" arg "arg"} message
        rpc-func (condp = function
                   "detect-language" detect/detect-language
                   "detect-probabilities" detect/detect-probabilities
                   detect/detect-error)
        result (rpc-func arg)
        result-json (json/write-str result)
        correlation-id (metastuff :correlation-id)
        reply-to (metastuff :reply-to)]
    (lbasic/publish ch "" reply-to result-json :correlation-id correlation-id)
    (lbasic/ack ch delivery-tag)))

(declare setup-queue!)
(defn -main
  "sets up the service and accept messages"
  [& args]
  (let [{:keys [options arguments summary errors]} (parse-opts args cli-opts)]
    ; checking that we have mandatory CLI arguments. Post help/usage string
    ; when things are missing.
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

    ; Handles the queue creation/subscription.
    (let [{:keys [host port queue username password detach]} options]
      (let [conn (lbroker/connect {:host host 
                                   :port port 
                                   :username username
                                   :password password})
            ch (lchannel/open conn)]
        (setup-queue! ch queue message-callback)
        (println "service up and running. Waiting for messages")))))

(defn setup-queue!
  "sends the command for queue setup to the message broker."
  [ch queue-name callback]
  (lqueue/declare ch queue-name)
  (lconsumers/subscribe ch queue-name callback))

