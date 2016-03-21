(ns travel-zoo.embedded.components.cluster
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]
            [clojure.java.io :refer [file]]
            [travel-zoo.embedded.defaults :as defaults]
            [travel-zoo.embedded.specs :as specs]
            [travel-zoo.embedded.protocols :refer :all]
            [travel-zoo.embedded.codec :as codec]
            [travel-zoo.embedded.cluster :as embedded-cluster]
            [travel-zoo.embedded.parser :as parser])
  (:import (org.apache.curator.test TestingCluster TestingZooKeeperServer)
           (java.util Collection)
           (java.io Closeable)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Direct Implementation Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord EmbeddedZookeeperCluster [instance-specifications]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting embedded Zookeeper cluster...")
    (let [cluster (->> instance-specifications
                       ^Collection (mapv specs/new-instance-spec)
                       (TestingCluster.))]
      (.start cluster)
      (timbre/info "Started embedded Zookeeper cluster -" (.getConnectString cluster))
      (assoc component
        :cluster cluster)))
  (stop [{:keys [^TestingCluster cluster] :as component}]
    (timbre/info "Stopping embedded Zookeeper cluster...")
    (when cluster
      (.close cluster))
    (timbre/info "Stopped embedded Zookeeper cluster.")
    component)
  ZookeeperInstanceSpecificationMetadata
  (instance-specifications [{:keys [^TestingCluster cluster]}]
    (->> (.getInstances cluster)
         (codec/decode)))
  ZookeeperConnectionMetadata
  (zk-ports [{:keys [^TestingCluster cluster]}]
    (some-> cluster
            (.getConnectString)
            (parser/parse-ports)))
  (zk-connection-string [{:keys [^TestingCluster cluster]}]
    (.getConnectString cluster))
  (zk-server-info [this]
    (parser/parse-server-list (zk-connection-string this)))
  ZookeeperEmbeddedServerManager
  (restart-zk-server! [{:keys [^TestingCluster cluster]} instance-specification]
    (->> instance-specification
         (specs/new-instance-spec)
         (.restartServer cluster)))
  (kill-zk-server! [{:keys [^TestingCluster cluster]} instance-specification]
    (->> instance-specification
         (specs/new-instance-spec)
         (.killServer cluster)))
  (zk-servers [{:keys [^TestingCluster cluster]}]
    (->> (.getServers cluster)
         (codec/decode)
         (mapcat (fn [^TestingZooKeeperServer server] (->> (.getInstanceSpecs server)
                                                           (codec/decode)
                                                           (->EmbeddedZookeeperCluster)))))))
(defn make-embedded-zk-cluster
  "Creates an embedded Zookeeper cluster component."
  ^EmbeddedZookeeperCluster
  ([]
   (make-embedded-zk-cluster [(defaults/make-default-instance-specification)]))
  ([instance-specifications]
   (->EmbeddedZookeeperCluster instance-specifications)))

(defn make-embedded-zk-cluster-ensemble
  "Creates an embedded Zookeeper cluster given n-server to add to the cluster."
  ^EmbeddedZookeeperCluster
  [instance-quantity]
  (->> (specs/make-instance-specifications instance-quantity)
       (->EmbeddedZookeeperCluster)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Composite Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord CompositeEmbeddedZookeeperCluster [^EmbeddedZookeeperCluster cluster]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting composite embedded Zookeeper cluster...")
    (start-zk-cluster cluster)
    (timbre/info "Started composite embedded Zookeeper cluster.")
    component)
  (stop [component]
    (timbre/info "Stopping composite embedded Zookeeper clsuter...")
    (when cluster
      (.close ^Closeable cluster))
    (timbre/info "Stopped composite embedded Zookeeper cluster.")
    component))

(defn make-composite-embedded-zk-cluster
  "Creates a component that wraps an EmbeddedZookeeperCluster instance.

  This component is useful for those that prefer semantics through a concrete type or need to test
  a component implementation."
  (^CompositeEmbeddedZookeeperCluster []
   (-> (embedded-cluster/make-embedded-zk-cluster)
       (->CompositeEmbeddedZookeeperCluster)))
  (^CompositeEmbeddedZookeeperCluster [instance-specifications]
   (-> instance-specifications
       (embedded-cluster/make-embedded-zk-cluster)
       (->CompositeEmbeddedZookeeperCluster))))

(defn make-composite-embedded-zk-cluster-ensemble
  "Creates a component that wraps an EmbeddedZookeeperCluster instance of n-servers.

  This component is useful for those that prefer semantics through a concrete type or need to test
  a component implementation."
  ^CompositeEmbeddedZookeeperCluster
  [instance-quantity]
  (-> instance-quantity
      (embedded-cluster/make-embedded-zk-cluster-ensemble)
      (->CompositeEmbeddedZookeeperCluster)))
