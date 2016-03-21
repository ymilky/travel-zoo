(ns travel-zoo.embedded.schemas
  (:require [schema.core :as s]))

;;TODO: stricter schema with coercision to int checks, but this does the job for now
(def InstanceSpecification
  "Schema for an instance specification (InstanceSpec)."
  {(s/optional-key :data-directory)             s/Str
   (s/optional-key :port)                       s/Int
   (s/optional-key :election-port)              s/Int
   (s/optional-key :quorom-port)                s/Int
   (s/optional-key :delete-directory-on-close?) s/Bool
   (s/optional-key :server-id)                  s/Int
   (s/optional-key :tick-time)                  s/Int
   (s/optional-key :max-client-connections)     s/Int})
