(defproject ymilky/travel-zoo "0.0.2"
  :description "Embedded Zookeeper server and cluster for dev/unit testing with full configuration and validation, components, and more."
  :url "https://github.com/ymilky/travel-zoo"
  :author "ymilky"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories {"snapshots" {:url           "https://clojars.org/repo"
                              :username      :env
                              :password      :env
                              :sign-releases false}
                 "releases"  {:url           "https://clojars.org/repo"
                              :username      :env
                              :password      :env
                              :sign-releases false}}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.0.5"]
                 [com.taoensso/timbre "4.3.1"]
                 [com.stuartsierra/component "0.3.1"]
                 [org.apache.curator/curator-test "3.1.0"]]
  :plugins [[lein-codox "0.9.4"]]
  :codox {:metadata    {:doc/format :markdown}
          :doc-paths   ["README.md"]
          :output-path "doc/api"}
  :profiles {:dev
                               {:dependencies [[midje "1.7.0"]
                                               [jarohen/nomad "0.7.2"]]
                                :plugins      [[lein-midje "3.2"]
                                               [lein-set-version "0.4.1"]
                                               [lein-update-dependency "0.1.2"]
                                               [lein-pprint "1.1.1"]]}
             :reflection-check {:global-vars {*warn-on-reflection* true
                                              *assert*             false
                                              *unchecked-math*     :warn-on-boxed}}})
