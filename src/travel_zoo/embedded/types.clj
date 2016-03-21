(ns travel-zoo.embedded.types
  (:require [travel-zoo.embedded.schemas :as tzs]
            [schema.core :as s])
  (:import (org.apache.curator.test InstanceSpec)))

(defrecord InstanceSpecification
  [data-directory port election-port quorom-port delete-directory-on-close? server-id tick-time max-client-connections])

(defn make-default-instance-specification-map []
  "Creates an instance specification map with sane defaults."
  {:data-directory             nil                          ;;this is the same method used in InstanceSpec, we do it here earlier if we want to build something in Clojure first
   :port                       (InstanceSpec/getRandomPort)
   :election-port              (InstanceSpec/getRandomPort)
   :quorom-port                (InstanceSpec/getRandomPort)
   :delete-directory-on-close? true
   :server-id                  -1
   :tick-time                  -1
   :max-client-connections     -1})

(s/defn make-instance-specification-map [m :- (s/maybe tzs/InstanceSpecification)] :- tzs/InstanceSpecification
  "Creates an instance specification map with sane default merged."
  (merge (make-default-instance-specification-map) m))

(s/defn make-instance-specification :- InstanceSpecification
  "Factory function for creating an instance specification.

  An instance specificaiton is used to define the configuration and behavior of a Zookeeper TestingServer."
  [m :- (s/maybe tzs/InstanceSpecification)] :- tzs/InstanceSpecification
  (map->InstanceSpecification (make-instance-specification-map m)))
