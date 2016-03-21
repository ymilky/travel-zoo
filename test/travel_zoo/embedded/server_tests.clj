(ns travel-zoo.embedded.server-tests
  (:require [midje.sweet :refer :all]
            [travel-zoo.embedded.core-test :as core-test]
            [travel-zoo.embedded.server :as server]
            [travel-zoo.embedded.protocols :refer :all]
            [travel-zoo.embedded.types :as tzt]
            [travel-zoo.embedded.specs :as specs]
            [travel-zoo.embedded.parser :as parser]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Embedded Server Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(fact
  "The Zookeeper connection string can be queried from an embedded Zookeeper instance."
  (let [instance-spec (specs/prefab-instance-spec)]
    (with-open [zk-server (server/make-embedded-zookeeper instance-spec)]
      (let [conn-string (zk-connection-string zk-server)
            port (parser/parse-port conn-string)]
        port => (:port instance-spec)))))

(fact
  "The embedded Zookeper port is queryable."
  (let [instance-spec (specs/prefab-instance-spec)]
    (with-open [zk-server (server/make-embedded-zookeeper instance-spec)]
      (let [port (zk-ports zk-server)]
        port => (:port instance-spec)))))

(fact
  "The embedded Zookeper data directory is queryable."
  (with-open [zk-server (server/make-embedded-zookeeper {:data-directory "combos"})]
    (let [dir (zk-temp-directory zk-server)]
      (str dir) => "combos")))

(fact
  "The embedded Zookeeper server can be manually closed."
  (let [zk-server (server/make-embedded-zookeeper)]
    (.close zk-server)))

(fact
  "The embedded Zookeeper server can be restarted."
  (with-open [zk-server (server/make-embedded-zookeeper)]
    (restart-zk zk-server)))

(fact
  "The embedded Zookeeper server can be stopped and started."
  (with-open [zk-server (server/make-embedded-zookeeper)]
    (stop-zk zk-server)
    (start-zk zk-server)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Testing Server Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(fact
  "The embedded testing Zookeeper server can be manually closed."
  (let [zk-server (server/make-embedded-testing-zookeeper)]
    (.close zk-server)))

(fact
  "The embedded Zookeeper server can be restarted."
  (with-open [zk-server (server/make-embedded-testing-zookeeper)]
    (restart-zk zk-server)))

;(fact
;  "The embedded testing Zookeeper server can be killed."
;  (with-open [zk-server (server/make-embedded-testing-zookeeper)]
;    (stop-zk zk-server)
;    (kill-zk! zk-server)))

(fact
  "The embedded testing Zookeeper server's instance specs can be queried."
  (let [instance-spec-map (specs/prefab-instance-spec)]
    (with-open [zk-server (server/make-embedded-testing-zookeeper [instance-spec-map])]
      (let [instance-specs (instance-specifications zk-server)
            instance-spec (first instance-specs)]
        (nil? instance-specs) => false
        (coll? true)
        (sequential? instance-specs) => true
        instance-spec => (tzt/map->InstanceSpecification instance-spec-map)))))

(fact
  "The embedded testing Zookeeper server can be stopped and started."
  (with-open [zk-server (server/make-embedded-testing-zookeeper)]
    (stop-zk zk-server)
    (start-zk zk-server)))
