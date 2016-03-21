(ns travel-zoo.embedded.codec
  (:require [clojure.java.io :refer [file]]
            [travel-zoo.embedded.types :as tzt])
  (:import (org.apache.curator.test InstanceSpec)
           (travel_zoo.embedded.types InstanceSpecification)
           (java.util Map Set Collection List)))

(defprotocol EmbeddedZookeeperCodec
  "Codec for going between Java and Clojure Zookeeper types."
  (encode [this])
  (decode [this]))

(declare decode-xf)

(defn map->InstanceSpec [m]
  "Converts a map/record of an instance specification to an InstanceSpec."
  (let [{:keys [data-directory port election-port quorom-port delete-directory-on-close? server-id tick-time max-client-connections]} (tzt/make-instance-specification-map m)
        ;;init the data directory as a file instead of a string for InstanceSpec
        data-directory (when data-directory (file data-directory))]
    (InstanceSpec. data-directory port election-port quorom-port delete-directory-on-close? server-id tick-time max-client-connections)))

(extend-protocol EmbeddedZookeeperCodec
  InstanceSpec
  (encode [v] v)
  (decode [v]
    (tzt/->InstanceSpecification (str (.getDataDirectory v))
                                 (.getPort v)
                                 (.getElectionPort v)
                                 (.getQuorumPort v)
                                 (.deleteDataDirectoryOnClose v)
                                 (.getServerId v)
                                 (.getTickTime v)
                                 (.getMaxClientCnxns v)))
  InstanceSpecification
  (encode [v]
    (map->InstanceSpec v))
  (decode [v] v)

  List
  (encode [v] v)
  (decode [v]
    (when (seq v)
      (into [] decode-xf v)))

  Collection
  (encode [v] v)
  (decode [v]
    (when (seq v)
      (into [] decode-xf v)))

  Set
  (encode [v] v)
  (decode [v]
    (when (seq v)
      (into #{} decode-xf v)))

  Iterable
  (encode [it] it)
  (decode [it]
    (map decode (iterator-seq (.iterator it))))

  Map
  (encode [v] v)
  (decode [v]
    (->> v
         (reduce (fn [m [k val]]
                   (assoc! m (as-> (decode k) dk
                                   (if (string? dk) (keyword dk) dk))
                           (decode val)))
                 (transient {}))
         persistent!))

  nil
  (encode [v] v)
  (decode [v] v)

  Object
  (encode [v] v)
  (decode [v] v))

(def decode-xf
  (map decode))
