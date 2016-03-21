(ns travel-zoo.embedded.specs
  (:require [schema.core :as s]
            [clojure.java.io :refer [file]]
            [travel-zoo.embedded.codec :as codec]
            [travel-zoo.embedded.types :as tzt])
  (:import [org.apache.curator.test InstanceSpec]
           (com.google.common.io Files)))

(defn new-instance-spec
  "Factory function to creates an instance of an InstanceSpec used with TestingServer. "
  ^InstanceSpec [m]
  (-> m
      (codec/map->InstanceSpec)))

(defn random-port []
  "Returns an open, random port on the current machine.

  Useful for when you want to know up-front what random port was generated for use
  with an instance specification port value."
  (InstanceSpec/getRandomPort))

(defonce server-id (atom 1))

(defn next-server-id
  "Creates an atomic server id for testing.

  The ids may be optionally reset by specifing true for reset-server-id?"
  ([] (next-server-id false))
  ([reset-server-id?]
   (if reset-server-id?
     (reset! server-id 1)
     (swap! server-id inc))))

(defn create-temp-dir []
  "Creates a simple, prefixed, increasing temp directory."
  ;;We use this to conform to InstanceSpec's implementation
  (Files/createTempDir))

(defn prefab-instance-spec
  "Creates an instance specification with more predictable values up-front.

  Useful for any comparisons of an instance specification before/after a test."
  ([] (prefab-instance-spec false))
  ([reset-server-id?]
   (merge (tzt/make-default-instance-specification-map) {:data-directory (str (create-temp-dir)) :server-id (next-server-id reset-server-id?)})))

(defn make-instance-specifications
  "Creates n-instance specifications, optionally resetting server-ids"
  ([instance-quantity]
   (make-instance-specifications instance-quantity false))
  ([instance-quantity reset-server-ids?]
   (repeatedly instance-quantity #(prefab-instance-spec reset-server-ids?))))
