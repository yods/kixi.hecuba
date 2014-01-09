(ns kixi.hecuba.kafka
  (:require
   jig

   [clj-kafka.producer    :as producer]
   [clj-kafka.zk          :as zk]
   [clj-kafka.core        :as kcore]
   [clj-kafka.consumer.zk :as consumer])
  (:import
   (jig Lifecycle)
   (kixi.hecuba.protocols Commander)))

(defn string-value
  [m]
  (String. (:value m)))

(defn create-msg
 [msg topic]
 (producer/message topic (.getBytes (str msg))))

(defn send-msg
  [message topic producer-config]
  (let [p (producer/producer producer-config)]
    (producer/send-message p (create-msg message topic))))

(defn receive
  [consumer-config]
  (kcore/with-resource [c (consumer/consumer consumer-config)]
    consumer/shutdown
    (doall (take 10 (consumer/messages c ["test"])))))

;; TODO jig console throws "java.lang.String cannot be cast to clojure.lang.Named" when displaying config
(defn create-kafka-connections
  [system config]
  (update-in system [(:jig/id config) ::producer-config] conj (:producer config))

  ;(update-in system [(:jig/id config) ::config] merge {:producer-config (:producer config)} {:consumer-config (:consumer config)} )
  )

(deftype KafkaCommander [producer-config]
  Commander
  (upsert! [_ payload]
    (send-msg payload)
    )
  )

(deftype Kafka [config]
  Lifecycle
  (init [_ system]
  ;  (assoc system :producer-config (into {} (:producer config)) :consumer-config (into {} (:consumer config)))
    (assoc system :producer-config {} :consumer-config {})
    )
  (start [_ system]
 ; (assoc system :producer-config (:producer config) :consumer-config (:consumer config))
    (create-kafka-connections system config)
    )
  (stop [_ system] system))
