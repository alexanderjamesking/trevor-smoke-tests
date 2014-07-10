(defproject smoke-tests "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.9.2"]
                 [org.clojure/data.json "0.2.4"]]
  :main smoke-tests.core
  :jvm-opts ["-Djsse.enableSNIExtension=false"
             "-Djavax.net.ssl.trustStore=/etc/pki/jssecacerts_allow_cloud"])