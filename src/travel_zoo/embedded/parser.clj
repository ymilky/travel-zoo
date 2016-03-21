(ns travel-zoo.embedded.parser)

;; we could do more with regexes here, look-ahead, blah blah to be fancy or possibly more efficient, but honestly no one cares
;; I could also deficate blood as well and you could introduce corner cases to all of us via your regex wizardry
;; regexes are the worst thing to ever happen to computer science since the words hacker, engineer, rockstar, and ninja
;; you're welcome

(defn parse-servers [connection-string]
  "Returns a vector of servers from a Zookeeper connection string."
  (when connection-string
    (clojure.string/split connection-string #",")))

(defn parse-host-and-port [host-and-port]
  "Parses a host/port fragment, and extracts them as a map."
  (let [[_ host ^String port] (re-find #"(?<host>.*):(?<port>\d*)" host-and-port)]
    {:host host
     :port (Integer. port)}))

(defn parse-port [host-and-port]
  "Parses just the port from a host/port fragment in a connection string and returns it as a number."
  (some-> (clojure.string/split host-and-port #":")
          ^String (peek)
          (Integer.)))

(defn parse-server-list [connection-string]
  "Parses the servers and ports in a connection string and returns a sequence of server/port maps."
  (map parse-host-and-port (parse-servers connection-string)))

(defn parse-ports [connection-string]
  "Parses the ports in a connection string and returns a numerical sequence of ports."
  ;;this could be more efficient, but being lazy here
  (map parse-port (parse-servers connection-string)))
