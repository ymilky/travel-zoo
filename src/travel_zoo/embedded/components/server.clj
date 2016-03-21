(ns travel-zoo.embedded.components.server
  (:require [com.stuartsierra.component :as component]
            [taoensso.timbre :as timbre]
            [clojure.java.io :refer [file]]
            [travel-zoo.embedded.defaults :as defaults]
            [travel-zoo.embedded.specs :as specs]
            [travel-zoo.embedded.protocols :refer :all
             ]
            [travel-zoo.embedded.server :as embedded-server]
            [travel-zoo.embedded.parser :as parser])
  (:import (org.apache.curator.test TestingServer)
           (travel_zoo.embedded.server EmbeddedZookeeperServer)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Direct Implementation Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord EmbeddedZookeeper [instance-specification]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting embedded Zookeeper...")
    (let [instance-spec (specs/new-instance-spec instance-specification)
          server (TestingServer. instance-spec true)]
      (timbre/info "Embedded Zookeeper started -" (.getConnectString server))
      (assoc component
        :server server)))
  (stop [{:keys [^TestingServer server] :as component}]
    (timbre/info "Stopping embedded Zookeeper...")
    (when server
      (timbre/info "Stopping embedded Zookeeper server -" (.getConnectString server))
      (.close server))
    (timbre/info "Embedded Zookeeper stopped.")
    component)
  ;;we only implement this protocol, not the ZookeeperEmbeddedServerLifecycle
  ;;because implemented the Lifecycle makes things a bit hairy without some work for Component + Systems
  ZookeeperEmbeddedServerMetadata
  (zk-temp-directory [{:keys [^TestingServer server]}]
    (when server (.getTempDirectory server)))
  ZookeeperConnectionMetadata
  (zk-ports [{:keys [^TestingServer server]}]
    (when server (.getPort server)))
  (zk-server-info [this]
    (parser/parse-server-list (zk-connection-string this)))
  (zk-connection-string [{:keys [^TestingServer server]}]
    (when server
      (.getConnectString server))))

(defn make-embedded-zookeeper
  "Creates an embedded Zookeeper component."
  ^EmbeddedZookeeper
  ([]
   (make-embedded-zookeeper (defaults/make-default-instance-specification)))
  ([data-directory port]
   (make-embedded-zookeeper (defaults/make-default-instance-specification data-directory port)))
  ([instance-specification]
   (->EmbeddedZookeeper instance-specification)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Composite Component
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defrecord CompositeEmbeddedZookeeper [^EmbeddedZookeeperServer server]
  component/Lifecycle
  (start [component]
    (timbre/info "Starting composite embedded Zookeeper...")
    (start-zk server)
    (timbre/info "Started composite embedded Zookeeper.")
    component)
  (stop [component]
    (timbre/info "Stopping composite embedded Zookeeper...")
    (when server
      (.close server))
    (timbre/info "Stopped composite embedded Zookeeper.")
    component))

(defn make-composite-embedded-zookeeper
  "Creates a component that wraps an EmbeddedZookeeperServer instance.

  This component is useful for those that prefer semantics through a concrete type or need to test
  a component implementation."
  ^CompositeEmbeddedZookeeper
  ([]
   (-> (embedded-server/make-embedded-zookeeper)
       (->CompositeEmbeddedZookeeper)))
  ([data-directory port]
   (-> (embedded-server/make-embedded-zookeeper data-directory port)
       (->CompositeEmbeddedZookeeper)))
  ([instance-specification]
   (-> (embedded-server/make-embedded-zookeeper instance-specification)
       (->CompositeEmbeddedZookeeper))))
