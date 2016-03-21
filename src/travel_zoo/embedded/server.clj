(ns travel-zoo.embedded.server
  (:require [travel-zoo.embedded.protocols :refer :all]
            [travel-zoo.embedded.defaults :as defaults]
            [travel-zoo.embedded.specs :as specs]
            [travel-zoo.embedded.codec :as codec]
            [travel-zoo.embedded.quorom :as quorom]
            [travel-zoo.embedded.parser :as parser]
            [taoensso.timbre :as timbre])
  (:import [java.io Closeable]
           (org.apache.curator.test TestingServer TestingZooKeeperServer)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing Server - General use Zookeeper testing server, single-node
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(deftype EmbeddedZookeeperServer [^TestingServer server]
  ZookeeperEmbeddedServerLifecycle
  (start-zk [_]
    (timbre/info "Starting Embedded Zookeeper server -" (.getConnectString server))
    (.start server))
  (restart-zk [_]
    (.restart server))
  (stop-zk [_]
    (timbre/info "Stopping Embedded Zookeeper server -" (.getConnectString server))
    (.stop server))
  ZookeeperEmbeddedServerMetadata
  (zk-temp-directory [_]
    (.getTempDirectory server))
  ZookeeperConnectionMetadata
  (zk-ports [_]
    (.getPort server))
  (zk-connection-string [_]
    (.getConnectString server))
  (zk-server-info [_]
    (parser/parse-server-list (.getConnectString server)))
  Closeable
  (close [_]
    (.close server)))

(defn make-embedded-zookeeper
  "Creates a concrete type implementation of an embedded Zookeeper server."
  (^EmbeddedZookeeperServer []
   (make-embedded-zookeeper (defaults/make-default-instance-specification)))
  (^EmbeddedZookeeperServer
  [data-directory port]
   (make-embedded-zookeeper (defaults/make-default-instance-specification data-directory port)))
  (^EmbeddedZookeeperServer
  [instance-specification]
   (-> (specs/new-instance-spec instance-specification)
       (TestingServer. false)
       (EmbeddedZookeeperServer.))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing Zookeeper Server - Used for clusters
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftype EmbeddedTestingZookeeperServer [^TestingZooKeeperServer server]
  ZookeeperEmbeddedServerLifecycle
  (start-zk [_]
    (.start server))
  (restart-zk [_]
    (.restart server))
  (stop-zk [_]
    (.stop server))
  KillableZookeeperServer
  (kill-zk! [_]
    (.kill server))
  ZookeeperInstanceSpecificationMetadata
  (instance-specifications [_]
    (->> (.getInstanceSpecs server)
         (codec/decode)))
  Closeable
  (close [_]
    (.close server)))

(defn make-embedded-testing-zookeeper
  "Creates a concrete type implementation of an embedded testing Zookeeper server.

  Prefer the EmbeddedZookeeperServer if you do not require a cluster."
  (^EmbeddedTestingZookeeperServer []
   (make-embedded-testing-zookeeper [(defaults/make-default-instance-specification)]))
  (^EmbeddedTestingZookeeperServer
  [data-directory port]
   (make-embedded-testing-zookeeper [(defaults/make-default-instance-specification data-directory port)]))
  (^EmbeddedTestingZookeeperServer
  [instance-specifications]
   (-> (quorom/make-quorom-config-builder instance-specifications)
       (TestingZooKeeperServer.)
       (EmbeddedTestingZookeeperServer.))))
