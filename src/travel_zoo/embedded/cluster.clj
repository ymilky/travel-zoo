(ns travel-zoo.embedded.cluster
  (:require [travel-zoo.embedded.protocols :refer [ZookeeperEmbeddedClusterLifecycle ZookeeperInstanceSpecificationMetadata
                                                   ZookeeperConnectionMetadata ZookeeperEmbeddedServerManager]]
            [travel-zoo.embedded.codec :as codec]
            [travel-zoo.embedded.specs :as specs]
            [travel-zoo.embedded.server :refer :all]
            [travel-zoo.embedded.defaults :as defaults]
            [travel-zoo.embedded.parser :as parser]
            [taoensso.timbre :as timbre])
  (:import (org.apache.curator.test TestingCluster)
           (java.io Closeable)
           (travel_zoo.embedded.server EmbeddedTestingZookeeperServer)
           (java.util Collection)))

(deftype EmbeddedZookeeperCluster [^TestingCluster cluster]
  ZookeeperEmbeddedClusterLifecycle
  (start-zk-cluster [_]
    (timbre/info "Starting Embedded Zookeeper cluster -" (.getConnectString cluster))
    (.start cluster))
  (stop-zk-cluster [_]
    (timbre/info "Stopping Embedded Zookeeper cluster -" (.getConnectString cluster))
    (.stop cluster))
  ZookeeperInstanceSpecificationMetadata
  (instance-specifications [_]
    (->> (.getInstances cluster)
         (codec/decode)))
  ZookeeperConnectionMetadata
  (zk-connection-string [_]
    (.getConnectString cluster))
  (zk-ports [_]
    (some-> cluster
            (.getConnectString)
            (parser/parse-ports)))
  (zk-server-info [_]
    (parser/parse-server-list (.getConnectString cluster)))
  ZookeeperEmbeddedServerManager
  (restart-zk-server! [_ instance-specification]
    (->> instance-specification
         (specs/new-instance-spec)
         (.restartServer cluster)))
  (kill-zk-server! [_ instance-specification]
    (->> instance-specification
         (specs/new-instance-spec)
         (.killServer cluster)))
  (zk-servers [_]
    (->> (.getServers cluster)
         (map #(EmbeddedTestingZookeeperServer. %))))
  Closeable
  (close [_]
    (.close cluster)))

(defn make-embedded-zk-cluster
  "Creates an embedded Zookeeper cluster from a collection of instance specifications."
  (^EmbeddedZookeeperCluster []
   (make-embedded-zk-cluster [(defaults/make-default-instance-specification)]))
  (^EmbeddedZookeeperCluster [instance-specifications]
   (->> instance-specifications
        ^Collection (mapv specs/new-instance-spec)
        (TestingCluster.)
        (EmbeddedZookeeperCluster.))))

(defn make-embedded-zk-cluster-ensemble
  "Creates an embedded Zookeeper cluster given n-server to add to the cluster."
  ^EmbeddedZookeeperCluster
  [instance-quantity]
  (-> (int instance-quantity)
      (TestingCluster.)
      (EmbeddedZookeeperCluster.)))
