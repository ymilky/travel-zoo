(ns travel-zoo.embedded.component-tests
  (:require [midje.sweet :refer :all]
            [travel-zoo.embedded.components.server :as server]
            [travel-zoo.embedded.components.cluster :as cluster]
            [travel-zoo.embedded.protocols :refer :all]
            [travel-zoo.embedded.specs :as specs]
            [com.stuartsierra.component :as component]
            [travel-zoo.embedded.types :as tzt]
            [travel-zoo.embedded.parser :as parser]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Embedded Server Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(fact
  "The Zookeeper connection string can be queried from an embedded Zookeeper instance."
  (let [instance-spec (specs/prefab-instance-spec)
        zk-server (component/start (server/make-embedded-zookeeper instance-spec))
        conn-string (zk-connection-string zk-server)
        port (parser/parse-port conn-string)]
    port => (:port instance-spec)
    (component/stop zk-server)))

(fact
  "The embedded Zookeper port is queryable."
  (let [instance-spec (specs/prefab-instance-spec)
        zk-server (component/start (server/make-embedded-zookeeper instance-spec))
        ;;a single port for a single server
        port (zk-ports zk-server)]
    port => (:port instance-spec)
    (component/stop zk-server)))

(fact
  "The embedded Zookeper data directory is queryable."
  (let [zk-server (component/start (server/make-embedded-zookeeper {:data-directory "combos"}))
        dir (zk-temp-directory zk-server)]
    (str dir) => "combos"
    (component/stop zk-server)))
;
(fact
  "The embedded Zookeeper server can be manually stopped."
  (let [zk-server (component/start (server/make-embedded-zookeeper))]
    (component/stop zk-server)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Cluster Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(fact
  "An embedded zookeeper cluster can be started with multiple servers in the cluster."
  (let [zk-cluster (component/start (cluster/make-embedded-zk-cluster-ensemble 5))]
    (count (zk-servers zk-cluster)) => 5
    (component/stop zk-cluster)))

(fact
  "An embedded zookeeper cluster can be started with multiple servers in the cluster, defined by instance specifications."
  (let [zk-cluster (component/start (cluster/make-embedded-zk-cluster [(tzt/make-instance-specification {:tick-time -1}) (tzt/make-instance-specification {:tick-time -1})]))]
    (count (zk-servers zk-cluster)) => 2
    (component/stop zk-cluster)))

(fact
  "The Zookeeper cluster connection string can be queried from an embedded Zookeeper instance."
  (let [instance-spec (specs/prefab-instance-spec)
        zk-cluster (component/start (cluster/make-embedded-zk-cluster [instance-spec]))
        conn-string (zk-connection-string zk-cluster)
        [_ port] (clojure.string/split conn-string #":")]
    port => (str (:port instance-spec))
    (component/stop zk-cluster)))

(fact
  "The Zookeeper cluster servers can be queried."
  (let [instance-specs (specs/make-instance-specifications 3)
        zk-cluster (component/start (cluster/make-embedded-zk-cluster instance-specs))
        servers (zk-servers zk-cluster)]
    (count servers) => 3
    (component/stop zk-cluster)))

(fact
  "The Zookeeper cluster servers can be killed."
  (let [instance-specs (specs/make-instance-specifications 3)
        zk-cluster (component/start (cluster/make-embedded-zk-cluster instance-specs))
        instance-spec (first instance-specs)]
    (kill-zk-server! zk-cluster instance-spec)
    (component/stop zk-cluster)))

(fact
  "The Zookeeper cluster servers instance specs can be queried."
  (let [instance-specs (specs/make-instance-specifications 3)
        zk-cluster (component/start (cluster/make-embedded-zk-cluster instance-specs))
        instance-spec (first instance-specs)]
    (map tzt/map->InstanceSpecification (instance-specifications zk-cluster)) => (contains instance-spec)
    (component/stop zk-cluster)))

(fact
  "The Zookeeper cluster can return a list of ports used."
  ;;ex: (52999 53002 53005 53008 53011)
  (let [zk-cluster (component/start (cluster/make-embedded-zk-cluster-ensemble 5))
        ports (zk-ports zk-cluster)]
    (nil? ports) => false
    (coll? ports) => true
    ports => (has every? integer?)
    (count ports) => 5
    (component/stop zk-cluster)))

(fact
  "The Zookeeper cluster can return a list of servers in the cluster."
  ;;ex: ({:host "127.0.0.1", :port 52681} {:host "127.0.0.1", :port 52684} {:host "127.0.0.1", :port 52687}
  ;; {:host "127.0.0.1", :port 52690} {:host "127.0.0.1", :port 52693})
  (let [zk-cluster (component/start (cluster/make-embedded-zk-cluster-ensemble 5))
        server-list (zk-server-info zk-cluster)]
    (nil? server-list) => false
    (coll? server-list) => true
    (count server-list) => 5
    (component/stop zk-cluster)))
