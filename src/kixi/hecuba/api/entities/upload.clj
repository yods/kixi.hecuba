(ns kixi.hecuba.api.entities.upload
  (:require
   [cheshire.core               :as json]
   [clojure.core.match          :refer (match)]
   [clojure.tools.logging       :as log]
   [kixipipe.pipeline           :as pipe]
   [kixipipe.storage.s3]
   [kixi.hecuba.security        :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.webutil         :as util]
   [kixi.hecuba.webutil         :refer (authorized? content-type-from-context)]
   [liberator.core              :refer (defresource)]
   [liberator.representation    :refer (ring-response)]
   [qbits.hayt                  :as hayt]
   [kixi.hecuba.storage.db      :as db]
   [kixi.hecuba.storage.uuid    :refer (uuid)]
   [kixi.hecuba.data.programmes :as programmes]
   [kixi.hecuba.data.projects   :as projects]
   [kixi.hecuba.data.entities   :as entities]
   [kixi.hecuba.data.users      :as users]
   [kixi.hecuba.web-paths       :as p]
   [clojure.java.io             :as io]
   [clj-time.core               :as t]))

(def ^:private entity-resource-path (p/resource-path-string :entity-resource))

(defmethod kixipipe.storage.s3/s3-key-from "media-resources" media-resources-s3-key-from [item]
  (str "media-resources/"(:entity_id item) "/" (:feed-name item) "/" (or (-> item :metadata :filename) (:filename item))))

(defmethod kixipipe.storage.s3/item-from-s3-key "media-resources" media-resources-item-from-s3-key [key]
  (when-let [[src-name entity_id feed-name filename] (next (re-matches #"^([^/]+)/([^/]+)/([^/]+)/([^/]+)$" key))]
    {:src-name "media-resources"
     :feed-name feed-name
     :metadata {:filename filename}
     :entity_id entity_id}))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects role request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects role request-method)
  (match  [(has-admin? role)
           (has-programme-manager? programme-id allowed-programmes)
           (has-project-manager? project-id allowed-projects)
           (has-user? programme-id allowed-programmes project-id allowed-projects)
           request-method]

          [true _ _ _ _]    true
          [_ true _ _ _]    true
          [_ _ true _ _]    true
          [_ _ _ true :get] true
          :else false))

(defn upload->items [files public? feed-name entity_id username]
  (let [timestamp (t/now)
        uuid      (uuid)]
    (map (fn [{:keys [tempfile content-type filename]}]
           {:dest      :upload
            :type      (keyword feed-name)
            :entity_id entity_id
            :public?   public?
            :src-name  "media-resources"
            :feed-name feed-name
            :dir       (.getParent tempfile)
            :date      timestamp
            :filename  (.getName tempfile)
            :metadata  {:timestamp    timestamp
                        :content-type content-type
                        :user         username
                        :filename     filename}}) files)))

(defn uri->feed-name [uri]
  (second (re-matches #".*/(.*)/" uri)))

(defn ensure-vector [data]
  (if (vector? data) data [data]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-allowed?
(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes roles]} (sec/current-authentication session)
          project_id (:project_id (:entity ctx))
          programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects roles request-method)
        true))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-created
(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-malformed?
(defmulti has-correct-content-type? (fn [feed-name _] feed-name))

(defmethod has-correct-content-type? "images" [_ {:keys [content-type]}]
  (when content-type (.startsWith content-type "image/" )))

(defmethod has-correct-content-type? "documents" [_ {:keys [content-type]}]
  ;; TODO - do we need to have a restrictive list of mime type here?
  (when content-type (not (.startsWith content-type "image/" ))))

(defmulti index-malformed? content-type-from-context)

(defmethod index-malformed? "multipart/form-data" [ctx]
  (let [request (:request ctx)
        feed-name (uri->feed-name (:uri request))
        multipart-params (:multipart-params request)
        {:strs [data public]} multipart-params
        file-data (ensure-vector data)]
    (if (every? (partial has-correct-content-type? feed-name) file-data)
      [false  {::file-data file-data
               ::public? (= "on" public)
               ::feed-name feed-name}]
      true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-post!
(defmulti index-post! content-type-from-context)

(defmethod index-post! "multipart/form-data" [store s3 pipe ctx]
  (let [file-data    (::file-data ctx)
        feed-name    (::feed-name ctx)
        public?      (::public? ctx)
        request      (:request ctx)
        session      (:session request)
        username     (sec/session-username session)
        route-params (:route-params request)
        entity_id    (:entity_id route-params)
        auth         (sec/current-authentication session)]
        (doseq [item (upload->items file-data public? feed-name entity_id username)]
          (pipe/submit-item pipe item))
        {:response {:status  202
                    :headers {"Location" (format entity-resource-path entity_id)}
                    :body    "Accepted"}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES

(defresource index [store s3 pipe]
  :allowed-methods #{:get :post}
  :available-media-types #{"application/json"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :malformed? #(index-malformed? %)
  :post! (partial index-post! store s3 pipe)
  :handle-created index-handle-created)
