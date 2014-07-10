(ns smoke-tests.core
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]))

(def http-conf {
  :keystore (System/getenv "DEV_CERT")
  :keystore-type "PKCS12"
  :keystore-pass (System/getenv "DEV_CERT_PASS")
  :throw-exceptions false
})

(defn ldp-uri [concept page]
  (str "https://api.live.bbc.co.uk/ldp-core/creative-works-v2?about=" concept "&format=TextualFormat&page=" page))

(def cps-headers {"X-Candy-Platform" "desktop" "X-Candy-Audience" "domestic" "Accept" "application/json"})
(def ldp-headers {"Accept" "application/json-ld"})

(defn get-asset-uris [body] 
  (for [asset (:results body)]
    (for [locator (:locator asset)]
      (when (.startsWith locator "urn:asset")
        (let [uri (.substring locator 10)]
          (str "https://api.live.bbc.co.uk/content/curie/asset:" uri))))))

(defn get-assets [asset-uris]
  (doseq [uri asset-uris]
    (let [res (client/get uri (merge http-conf {:headers cps-headers}))
          body (json/read-json (:body res))
          asset-uri (-> body :results first :assetUri)]
      (println asset-uri)
      (try
        (client/get (str "http://127.0.0.1:8185/content/cps" asset-uri))
        (catch Exception e (println "ex1: " (.getMessage e)))))))

(def ldp-concepts '(
  "32308873-9c53-4071-97b5-4aa7129a4bc5" ;bbc london
  "2e91364c-5c77-4660-b76e-d76202785e64" ;UK
  "2f2db234-3c2d-40a4-b4ac-eea661faadd0" ;business
  "82857f8e-8134-462a-bb32-b7b14f4eab75" ;United States
  "75612fa6-147c-4a43-97fa-fcf70d9cced3" ;politics
  ; sports...
  ;"ba6e1118-f874-054e-b159-b797c16e9250"
  ;"ea0aacda-0e5b-4fa2-9f94-dfe775b321a4"
  ;"5cd4682a-7643-f445-8b1f-bcbaf450bc89"
  ;"9631e5b8-6068-f74e-a4f0-bf267ce2bc21"
  ;"030b5eaf-15db-3e4e-bac9-a7789892f436"
))

(defn -main []
  (doseq [page (range 1 30)] ; for 30 pages of assets
    (doseq [concept ldp-concepts] ; call each page for each concept
      (try
        (let [uri (ldp-uri concept page)
          _ (println uri)
          response (client/get uri (merge http-conf {:headers ldp-headers}))
          body (json/read-json (-> response :body))
          asset-uris (remove nil? (flatten (get-asset-uris body)))]
          (get-assets asset-uris)      )
        (catch Exception e (println "ex0: " (.getMessage e)))))))
