## Rationale

Many people who need an embedded Zookeeper instance for testing are content to just use the Java API directly or include various libraries. While there's not anything wrong with this approach for some cases, there can be some serious problems and lost functionality if not handled carefully. Unfortunately it's been my experience that most examples I have seen using TestingServer in Clojure are poor.

Generally, there are a number of libraries and examples that use embedded Zookeeper instances. While these libraries sometimes work well for simple purposes, they typically have a few flaws outline below.

* Over-simplification or naive assumptions that Zookeeper instances will close cleanly between teardowns, unit tests, etc.
     * Reusing the same ports without checking if the ports are open. Failure to do this can lead to all tests failing.
     * Failure to cleanup data directories
     * Cleanup of data directories when the user wants to inspect the contents of the directory after the server is closed/stopped
* Hiding the API
     * Many cloaked or missing methods
     * Hiding important constructs, ex: `(TestingServer. 2181)`, data directory, delete on shutdown
     * Conflating close vs. stop - they have different behavior for both servers and clusters
* No validation of Zookeeper server parameters
     * It's easy to make mistakes/typos in Clojure, especially when writing tons of unit tests
     * The constructor parameters for some servers like testing servers is quite large and error-prone
* No support for clusters
     * A large part of Zookeeper is not accurately testable without a cluster
* Very non-Clojure-esque
     * No map-based configurations
     * Raw Java calls
     * Reflection issues with Java
     * API is put in a position to throw lots of exceptions
* Outdated or extra dependencies
     * For embedded servers, only a small part of the curator framework is required
     * Many libraries use old versions of curator that are incompatible with Kafka, Cassandra, etc
     * Old versions of Clojure itself
* Leak resources
* Bizarre macro usage
     * There's no need for an extra macro to use a resource that already implements closeable and can thus use with-open without reflection
     * Many wrapping macros break the startup/shutdown/close cycle (see close vs. shutdown)
     * I believe it's better to work with a pure data approach with maps rather than baking map keys hard-coded into macros
