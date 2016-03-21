(ns travel-zoo.embedded.quorom
  (:require [travel-zoo.embedded.specs :as specs])
  (:import (org.apache.curator.test QuorumConfigBuilder)
           (java.util List)))

(defn make-quorom-config-builder
  "Returns an instance of a Quorom config builder, used to build Zookeeper clusters."
  ^QuorumConfigBuilder
  [instance-specifications]
  (QuorumConfigBuilder. ^List (mapv specs/new-instance-spec instance-specifications)))
