(ns travel-zoo.embedded.parser-tests
  (:require [midje.sweet :refer :all]
            [travel-zoo.embedded.parser :as parsers]
            [schema.core :as s]
            [travel-zoo.embedded.schemas :as ts])
  (:import (java.net URI)))

(fact
  "A connection string fragment may be parsed by its host."
  (parsers/parse-host "127.0.0.1:9092") => "127.0.0.1")

(fact
  "A connection string fragment may be parsed by its port."
  (parsers/parse-port "127.0.0.1:9092") => 9092)

(fact
  "The host, path, and port of a connection string fragment may be parsed into components"
  (let [broker-address (parsers/parse-server "127.0.0.1:9092/abc")]
    (s/check ts/ServerAddress broker-address) => nil
    broker-address => {:host "127.0.0.1" :port 9092 :path "/abc"}))

(fact
  "The ports from a connection string can be queried and returned as a set."
  (parsers/parse-ports "127.0.0.1:9092,127.0.0.1:9093,10.0.0.1:9092") => #{9092 9093})

(fact
  "The hosts from a connection string can be queried and returned as a set."
  (parsers/parse-server-hosts "127.0.0.1:9092,127.0.0.1:9093,10.0.0.1:9092,bro.grammer:9092") => #{"127.0.0.1" "10.0.0.1" "bro.grammer"})

(fact
  "A connection string can be transformed into a server info list."
  (let [server-list (parsers/parse-server-list "127.0.0.1:9092,127.0.0.1:9093,10.0.0.1:9092,bro.grammer:9092")]
    (s/check [ts/ServerAddress] server-list) => nil
    server-list => [{:host "127.0.0.1" :port 9092} {:host "127.0.0.1" :port 9093} {:host "10.0.0.1" :port 9092} {:host "bro.grammer" :port 9092}]))

(fact
  "A connection string may be parsed into its server fragments."
  (parsers/parse-servers "127.0.0.1:9092,127.0.0.1:9093,10.0.0.1:9092,bro.grammer:9092") => ["127.0.0.1:9092" "127.0.0.1:9093" "10.0.0.1:9092" "bro.grammer:9092"])

(fact
  "A connection string fragment may be parsed into a Java URI."
  (let [uri (parsers/fragment->uri "127.0.0.1:9092")]
    (nil? uri) => false
    (instance? URI uri) => true
    (.getPort uri) => 9092
    (.getHost uri) => "127.0.0.1"))

(fact
  "A list of URIs may be extracted from a connection string."
  (let [uris (parsers/connection-string->uris "127.0.0.1:9092,127.0.0.1:9093,10.0.0.1:9092,bro.grammer:9092")]
    (nil? uris) => false
    (coll? uris) => true
    (empty? uris) => false
    (s/check [URI] uris) => nil))