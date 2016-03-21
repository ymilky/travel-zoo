(ns travel-zoo.embedded.defaults
  (:require [travel-zoo.embedded.types :as tzt])
  (:import (travel_zoo.embedded.types InstanceSpecification)))

(defn make-default-instance-specification
  "Creates a default instance of an InstanceSpecification.

  Directly or indirectly ensures sane defaults for use with TestingServer"
  ^InstanceSpecification
  ([] (tzt/make-instance-specification {}))
  ([{:keys [port data-directory]
     :or   {port 2181}}]
   (make-default-instance-specification data-directory port))
  ([data-directory port]
   (tzt/make-instance-specification {:data-directory data-directory :port port})))
