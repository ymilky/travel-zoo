# Travel-Zoo

A Clojure toolbox for running embedded Zookeeper servers and clusters.

## What's in the Box

* A concrete embedded Zookeeper type that wraps [Apache Curator Testing Server](https://curator.apache.org/apidocs/org/apache/curator/test/TestingServer.html) correctly.
* A concrete embedded Zookeeper cluster type
* 4 [components](https://github.com/stuartsierra/component) for those who work with [Component](https://github.com/stuartsierra/component), 2 each for the server and cluster respectively (composite vs. concrete components).
* Schemas for validating your Zookeeper test config.
* Record type for defining your test specification as data.
* A set of protocols if you want to roll your own embedded server on something else or swap out with a non-testing implementation.
* Full conversions to/from TestingServer types
* Helpers for creating instance specifications and clusters

## Why

* Extreme care to avoid reflection and preserve the integrity of the [Testing Server](https://curator.apache.org/apidocs/org/apache/curator/test/TestingServer.html) API.
* You want to test Zookeeper in 100% Clojure and not worry about Java.
* You want to configure and validate your Zookeeper server/cluster in Clojure, perhaps building a dynamic config with a map or record.
* You want to test some code against Zookeeper in development or a unit test.
* You want to test both Zookeeper servers and full clusters.
* You are using a library or tech that requires Zookeeper such as Kafka or Cassandra and need something for testing/dev.
* You cannot or do not want to use Zookeeper in a VM or container such as Docker for your use-case.
* You need to test some things in a throw-away Zookeeper instance.
* You tried to roll your own embedded Zookeeper for testing, but don't feel like maintaining it or it doesn't quite work always.
* Other embedded Zookeeper servers that do something similar kill or hide the functionality of [Testing Server](https://curator.apache.org/apidocs/org/apache/curator/test/TestingServer.html).
* You want something to help you test with [Franzy](https://github.com/ymilky/franzy), a suite of Clojure Kafka libraries.
* A bit more legible and explicit than the Java TestingServer in the familiar Clojure way.
* You use other Clojure Zookeeper libraries such as zookeeper-clj and need a testing server/cluster to test the accompanying code.

## Docs

* Read the browsable [API](http://ymilky.github.io/travel-zoo/index.html).
* Read about [Apache Curator Testing](http://curator.apache.org/curator-test/).
* See the unit tests for a few simple, growing examples.
* For more about using, validating, and developing schemas, see the official repo for [Schema](https://github.com/plumatic/schema).
* For more about using component, see the official repo for [Component](https://github.com/stuartsierra/component).

## Notes

* Do not use this to run a production Zookeeper cluster. This should be obvious and you shouldn't be using Zookeeper if you thought to do this. You've been warned, now we can be friends.

## Installation

Add the necessary dependency to your project:

```clojure
[ymilky/travel-zoo "0.0.2"]
```

[![Clojars Project](https://img.shields.io/clojars/v/ymilky/travel-zoo.svg)](https://clojars.org/ymilky/travel-zoo)

## Usage

### Server Using Concrete Type

Require the appropriate namespaces, then start/stop/close the Zookeeper server as needed. It is best to

```clojure
(ns my.ns
  (:require [travel-zoo.embedded.server :as server]
            [travel-zoo.embedded.protocols :refer [start-zk stop-zk restart-zk
                                                   zk-server-info zk-connection-string
                                                   zk-ports zk-temp-directory]])
  (:import (travel_zoo.embedded.server EmbeddedZookeeperServer)))

;;creates a simple server on port 2181
(defn make-zk-server []
  (server/make-embedded-zookeeper {:port 2181}))

;;starts the server
(defn start-embedded-zk [zk-server]
  (start-zk zk-server))

;;stops the server, doesn't delete data
(defn stop-embedded-zk [zk-server]
  (stop-zk zk-server))

;;restarts the server
(defn restart-embedded-zk [zk-server]
  (restart-zk zk-server))

;;close the server and delete data directory if instance spec flag is set
(defn close-embedded-zk [^EmbeddedZookeeperServer zk-server]
  (.close zk-server))

;;1 server in non-clustered mode
(defn list-servers [zk-server]
  (zk-server-info zk-server))
;=>
({:host "127.0.0.1", :port 52681})

;;find out what temp directory is being used by zk
(defn temp-directory [zk-server]
  (temp-directory zk-server))
;=>
"/var/folders/ck/6bj9x9wd5lb7htmz18pd4qbw0000gn/T/1458579147656-0"

(defn list-open-ports [zk-server]
  (zk-ports zk-server))
;=>
2181

(defn connection-string [zk-server]
  (zk-connection-string zk-server))
;=>
"127.0.0.1:2181"
```

### Server Using Component

```clojure
(ns my.ns
  (:require [travel-zoo.embedded.components.server :as server]
            [travel-zoo.embedded.protocols :refer [zk-server-info zk-connection-string
                                                   zk-ports zk-temp-directory]]
            [com.stuartsierra.component :as component]))

;;creates a simple server on port 2181
(defn make-zk-server []
  (server/make-embedded-zookeeper {:port 2181}))

;;starts the server
(defn start-embedded-zk [zk-server]
  (component/start zk-server))

;;stops the server
(defn stop-embedded-zk [zk-server]
  (component/stop zk-server))

;;1 server in non-clustered mode
(defn list-servers [zk-server]
  (zk-server-info zk-server))
;=>
({:host "127.0.0.1", :port 2181})

;;find out what temp directory is being used by zk
(defn temp-directory [zk-server]
  (temp-directory zk-server))
;=>
"/var/folders/ck/6bj9x9wd5lb7htmz18pd4qbw0000gn/T/1458579147656-0"

(defn list-open-ports [zk-server]
  (zk-ports zk-server))
;=>
2181

(defn connection-string [zk-server]
  (zk-connection-string zk-server))
;=>
"127.0.0.1:2181"
```

### Cluster Using Concrete Type

```clojure
(ns my.ns
  (:require [travel-zoo.embedded.cluster :as cluster]
            [travel-zoo.embedded.protocols :refer [start-zk-cluster stop-zk-cluster zk-connection-string
                                                   zk-server-info instance-specifications zk-ports]])
  (:import (travel_zoo.embedded.cluster EmbeddedZookeeperCluster)))

;;creates a 5 server cluster with random ports, data dirs, etc.
(defn make-zk-cluster []
  (cluster/make-embedded-zk-cluster-ensemble 5))

;;alternatively, a vector of maps of instance specifications for more control
;;creates a 2 server cluster with ports 2181 and 2182 used
;;(cluster/make-embedded-zk-cluster [{:port 2181}{:port 2182}])

;;starts the cluster
(defn start-embedded-cluster [zk-cluster]
  (start-zk-cluster zk-cluster))

;;stops the cluster, but doesn't delete the data directory if the appropriate config flag is set
(defn stop-embedded-cluster [zk-cluster]
  (stop-zk-cluster zk-cluster))

;;type flag to prevent reflection, closeable is directly implemented so you may use with-open as well
(defn close-zk-cluster [^EmbeddedZookeeperCluster zk-cluster]
  (.close zk-cluster))

;;get a list of servers in the cluster
;;ex:
(defn list-servers [zk-cluster]
  (zk-server-info zk-cluster))
;=>
({:host "127.0.0.1", :port 52681} {:host "127.0.0.1", :port 52684} {:host "127.0.0.1", :port 52687}
  {:host "127.0.0.1", :port 52690} {:host "127.0.0.1", :port 52693})

;;list the instance specs used to construct the cluster
(defn list-instance-specs [zk-cluster]
  (instance-specifications zk-cluster))
;=>
[{:data-directory
                              "/var/folders/ck/6bj9x9wd5lb7htmz18pd4qbw0000gn/T/1458579147656-0",
  :port                       63370,
  :election-port              63371,
  :quorom-port                63372,
  :delete-directory-on-close? true,
  :server-id                  1,
  :tick-time                  -1,
  :max-client-connections     -1},
 ;.... more servers....
 ]

(defn list-open-ports [zk-cluster]
  (zk-ports zk-cluster))
;=>
(52999 53002 53005 53008 53011)

(defn connection-string [zk-cluster]
  (zk-connection-string zk-cluster))
;=>
"127.0.0.1:5299,127.0.0.1:53002,127.0.0.1:53005,127.0.0.1:53008,127.0.0.1:53011"
```

### Cluster Using Component

```clojure
(ns my.ns
  (:require [travel-zoo.embedded.components.cluster :as cluster]
            [travel-zoo.embedded.protocols :refer [zk-server-info instance-specifications zk-ports zk-connection-string]]
            [com.stuartsierra.component :as component]))

;;creates a 5 server cluster with random ports, data dirs, etc.
(defn make-zk-cluster []
  (cluster/make-embedded-zk-cluster-ensemble 5))

;;alternatively, a vector of maps of instance specifications for more control
;;creates a 2 server cluster with ports 2181 and 2182 used
;;(cluster/make-embedded-zk-cluster [{:port 2181}{:port 2182}])

;;starts the cluster
(defn start-embedded-cluster [zk-cluster]
  (component/start zk-cluster))

;;stops the cluster, but doesn't delete the data directory if the appropriate config flag is set
(defn stop-embedded-cluster [zk-cluster]
  (component/stop zk-cluster))

;;get a list of servers in the cluster
;;ex:
(defn list-servers [zk-cluster]
  (zk-server-info zk-cluster))
;=>
({:host "127.0.0.1", :port 52681} {:host "127.0.0.1", :port 52684} {:host "127.0.0.1", :port 52687}
 {:host "127.0.0.1", :port 52690} {:host "127.0.0.1", :port 52693})

;;list the instance specs used to construct the cluster
(defn list-instance-specs [zk-cluster]
  (instance-specifications zk-cluster))
;=>
[{:data-directory
                              "/var/folders/ck/6bj9x9wd5lb7htmz18pd4qbw0000gn/T/1458579147656-0",
  :port                       63370,
  :election-port              63371,
  :quorom-port                63372,
  :delete-directory-on-close? true,
  :server-id                  1,
  :tick-time                  -1,
  :max-client-connections     -1},
 ;.... more servers....
 ]

(defn list-open-ports [zk-cluster]
  (zk-ports zk-cluster))
;=>
(52999 53002 53005 53008 53011)

(defn connection-string [zk-cluster]
  (zk-connection-string zk-cluster))
;=>
"127.0.0.1:5299,127.0.0.1:53002,127.0.0.1:53005,127.0.0.1:53008,127.0.0.1:53011"
```

### Instance Specifications

Instance specifications are used to provide configuration. All flavors of servers and clusters have various arities that will except 1 or more instance specifications. You can use instance specifications to more directly control things like ports and data directories if required.

An instance specification may be constructed as a Clojure map or by using an instance specification record. If you only want to set a few of the possible values, you can safely construct servers and clusters this way, and the rest of the defaults such as ports will be randomly generated for you.

All ports and data directories are checked to be free before allocation. It is important to note if you create an instance specification, but delete a directory or use a port used in the specification before the server is started, you may cause an error. This is because these values are pre-allocated, so there is a window before a server uses these values that it can obtain a lock on them. As such, you should be careful about race conditions between threads, however this should rarely if ever be an issue for most use-cases.

```clojure
(ns my.ns
  (:require [travel-zoo.embedded.types :as types]
            [travel-zoo.embedded.specs :as specs]
            [travel-zoo.embedded.schemas :as tzs]
            [schema.core :as s]))

(defn make-instance-spec-record []
  ;;you can specify -1 for most defaults
  (types/->InstanceSpecification "~/my-zk-data" 2181 2182 2183 true -1 -1 -1))

(defn make-instance-spec-map []
  ;;as a map - you don't have to specify all keys
  {:data-directory             "~/hidden-stash/zk"
   ;;ports can be randomized
   :port                       (specs/random-port)
   :election-port              6665
   :quorom-port                6666
   :delete-directory-on-close? true
   ;;server ids can be atomically obtained in a sequence, mostly useful for clusters or multiple stand-alone servers
   :server-id                  (specs/next-server-id)
   :tick-time                  -1
   :max-client-connections     5})

;;and of course since we're using records, we can always do this
(defn make-instance-spec-record-from-map []
  (types/map->InstanceSpecification (make-instance-spec-map)))

;;make a spec with all defaults realized up-front if we want something more explicit in conjunction with other services
;;useful for example if you're allocating lots of ports and want to know what they are before the server starts, but want it all randomized
(defn make-prefabricated-spec []
  (specs/prefab-instance-spec))

;;useful for building clusters, returns multiple, unique instance specs with different ports, data dirs, etc.
(defn make-lots-of-specs []
  (specs/make-instance-specifications 20))

;;if you want a throw-away directory created up-front and to know the path
(defn create-data-dir []
  (specs/create-temp-dir))

;;manually validate a spec, or turn on schema always validate and your specs will be validated before trying to start servers/clusters
(defn validate-spec [my-spec]
  (s/validate my-spec tzs/InstanceSpecification))
```

## License

Copyright © 2016 Yossi M. (ymilky).

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

Use at your own risk, I am not responsible or liable. Please give credit if you use pieces of this library or otherwise, it is much appreciated.
