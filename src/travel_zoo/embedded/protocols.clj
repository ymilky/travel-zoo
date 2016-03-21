(ns travel-zoo.embedded.protocols)

(defprotocol ZookeeperEmbeddedServerLifecycle
  "Defines the lifecycle of an embedded Zookeeper server."
  (start-zk [this])
  (stop-zk [this])
  (restart-zk [this]))

(defprotocol KillableZookeeperServer
  "Zookeeper server that may be killed."
  (kill-zk! [this]))

(defprotocol ZookeeperEmbeddedServerMetadata
  "Provides metadata about an embedded Zookeeper server."
  (zk-temp-directory [this]))

(defprotocol ZookeeperConnectionMetadata
  "Provides metadata about Zookeeper connections."
  (zk-ports [this])
  (zk-connection-string [this])
  (zk-server-info [this]))

(defprotocol ZookeeperEmbeddedClusterLifecycle
  "Defines the lifecycle of a Zookeeper cluster."
  (start-zk-cluster [this])
  (stop-zk-cluster [this]))

(defprotocol ZookeeperInstanceSpecificationMetadata
  "Provides metadata about an embedded Zookeeper cluster."
  (instance-specifications [this]))

(defprotocol ZookeeperEmbeddedServerManager
  "Manages servers inside a Zookeeper cluster."
  (restart-zk-server! [this instance-specification])
  (kill-zk-server! [this instance-specification])
  (zk-servers [this]))
