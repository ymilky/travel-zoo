(ns travel-zoo.embedded.parser
  (:import (java.net URI)))

;; we could do more with regexes here, look-ahead, blah blah to be fancy or possibly more efficient, but honestly no one cares

(defn parse-servers
  "Returns a vector of server connection strings from a connection string."
  [connection-string]
  (when connection-string
    (clojure.string/split connection-string #",")))

(defn fragment->uri
  "Extracts a connection string fragment as a URI.

  Useful for further parsing/usage with other APIs."
  ^URI [fragment]
  (URI. (str "tcp://" fragment)))

(defn connection-string->uris
  "Returns a collection of URIs from a connection connection string."
  [connection-string]
  (map fragment->uri (parse-servers connection-string)))

(defn parse-server
  "Parses a host/port fragment, and extracts them as a map."
  [fragment]
  (let [uri (fragment->uri fragment)
        path (.getPath uri)
        server-address {:host (.getHost uri)
                             :port (.getPort uri)}]
    ;;normally, zk connections don't have paths, but it is possible to use them
    (if (clojure.string/blank? path)
      server-address
      (assoc server-address :path path))))

(defn parse-port
  "Parses just the port from a host/port fragment in a connection string and returns it as a number."
  [fragment]
  (some-> (fragment->uri fragment) (.getPort)))

(defn parse-host
  "Parses just the host from a host/port fragment in a connection string and returns it as a number."
  [fragment]
  (some-> (fragment->uri fragment) (.getHost)))

(defn parse-server-list
  "Parses the servers and ports in a connection string and returns a sequence of server/port maps.

  Useful for logging and getting an overview of resource usage."
  [connection-string]
  (map parse-server (parse-servers connection-string)))

(defn parse-server-hosts
  "Returns a set of just the hosts from a connection string.

  Useful for seeing what hosts are active in a cluster."
  [connection-string]
  (into #{} (map parse-host (parse-servers connection-string))))

(defn parse-ports
  "Returns a set of the ports used by a connection string.

  Note that the ports may be open on different hosts, so this is more useful in scenarios such as testing on localhost
  or where a FQDN is used or in bridged configurations such as those used commonly by containers such as docker.

  Useful for checking what ports are open in a cluster."
  [connection-string]
  ;;this could be more efficient, but being lazy here
  (into #{} (map parse-port (parse-servers connection-string))))