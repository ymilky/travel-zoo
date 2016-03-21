(ns travel-zoo.embedded.cluster-tests
  (:require [midje.sweet :refer :all]
            [travel-zoo.embedded.core-test :as core-test]
            [travel-zoo.embedded.parser :as parser]
            [travel-zoo.embedded.cluster :as cluster]
            [travel-zoo.embedded.protocols :refer :all]
            [travel-zoo.embedded.types :as tzt]
            [travel-zoo.embedded.specs :as specs]))

;;ex list of instance spec
;;    ;[{:data-directory "/var/folders/ck/6bj9x9wd5lb7htmz18pd4qbw0000gn/T/1234",
;  :port 49260, :election-port 49261, :quorom-port 49262, :delete-directory-on-close? true, :server-id 6, :tick-time -1, :max-client-connections -1}
; {:data-directory "/var/folders/ck/6bj9x9wd5lb7htmz18pd4qbw0000gn/T/ezk/56778",
;  :port 49263, :election-port 49264, :quorom-port 49265, :delete-directory-on-close? true, :server-id 7, :tick-time -1, :max-client-connections -1}]

(fact
  "An embedded zookeeper cluster can be started with multiple servers in the cluster."
  (with-open [zk-cluster (cluster/make-embedded-zk-cluster-ensemble 5)]
    (start-zk-cluster zk-cluster)
    (count (zk-servers zk-cluster)) => 5))

(fact
  "An embedded zookeeper cluster can be started with multiple servers in the cluster, defined by instance specifications."
  (with-open [zk-cluster (cluster/make-embedded-zk-cluster [(tzt/make-instance-specification {:tick-time -1}) (tzt/make-instance-specification {:tick-time -1})])]
    (start-zk-cluster zk-cluster)
    (count (zk-servers zk-cluster)) => 2))

(fact
  "The Zookeeper cluster connection string can be queried from an embedded Zookeeper instance."
  (let [instance-spec (specs/prefab-instance-spec)]
    (with-open [zk-cluster (cluster/make-embedded-zk-cluster [instance-spec])]
      (let [conn-string (zk-connection-string zk-cluster)
            ;;a single port in a 1 server cluster
            port (parser/parse-port conn-string)]
        port => (:port instance-spec)))))

(fact
  "The Zookeeper cluster can return a list of ports used."
  ;;ex: (52999 53002 53005 53008 53011)
  (with-open [zk-cluster (cluster/make-embedded-zk-cluster-ensemble 5)]
    (let [ports (zk-ports zk-cluster)]
      (nil? ports) => false
      (coll? ports) => true
      ports => (has every? integer?)
      (count ports) => 5)))

(fact
  "The Zookeeper cluster can return a list of servers in the cluster."
  ;;ex: ({:host "127.0.0.1", :port 52681} {:host "127.0.0.1", :port 52684} {:host "127.0.0.1", :port 52687}
  ;; {:host "127.0.0.1", :port 52690} {:host "127.0.0.1", :port 52693})
  (with-open [zk-cluster (cluster/make-embedded-zk-cluster-ensemble 5)]
    (let [server-list (zk-server-info zk-cluster)]
      (nil? server-list) => false
      (coll? server-list) => true
      (count server-list) => 5)))

(fact
  "The Zookeeper cluster servers can be queried."
  (let [instance-specs (specs/make-instance-specifications 3)]
    (with-open [zk-cluster (cluster/make-embedded-zk-cluster instance-specs)]
      (let [servers (zk-servers zk-cluster)]
        (count servers) => 3))))

(fact
  "The Zookeeper cluster servers can be killed."
  (let [instance-specs (specs/make-instance-specifications 3)]
    (with-open [zk-cluster (cluster/make-embedded-zk-cluster instance-specs)]
      (let [instance-spec (first instance-specs)]
        (kill-zk-server! zk-cluster instance-spec)))))

(fact
  "The Zookeeper cluster servers instance specs can be queried."
  (let [instance-specs (specs/make-instance-specifications 3)]
    (with-open [zk-cluster (cluster/make-embedded-zk-cluster instance-specs)]
      (let [instance-spec (first instance-specs)]
        (map tzt/map->InstanceSpecification (instance-specifications zk-cluster)) => (contains instance-spec)))))

(fact
  "The embedded Zookeeper cluster can be manually closed."
  (let [zk-cluster (cluster/make-embedded-zk-cluster)]
    (.close zk-cluster)))

(fact
  "The embedded Zookeeper cluster can be stopped and started."
  (let [zk-cluster (cluster/make-embedded-zk-cluster)]
    (stop-zk-cluster zk-cluster)
    (start-zk-cluster zk-cluster)))
