(ns kixi.hecuba.history
    "Implement history"
    (:require  [cljs.core.async :refer [put!]]
               [clojure.browser.event :as event]
               [clojure.string :as str]
               [kixi.hecuba.common :refer (index-of)]
               [goog.string]
               [goog.History :as history]
               [goog.history.Html5History :as history5]))

;; (enable-console-print!)

(defn match-token [s]
  (re-matches #"((?:[A-Za-z0-9-_;~ ]+)(?:,(?:[A-Za-z0-9-_;~ ]+))*)(?:/search/(.*))?" s))

;; TODO let's not use an atom for this.
(def ^:private key-order (atom []))

(defn- add-navigation-chan!
  [history ch f]
  (do (event/listen history "navigate"
                    (fn [e]
                      (let [token (.-token e)]
                        (if-let [args (if (pos? (count token))
                                        (f token)
                                        {})]
                          (put! ch {:args args
                                    :type (.-type e)
                                    :navigation? (.-isNavigation e)})))))
      (.setEnabled history true)
      ch))

(defn- set-token!
  [history token f & {:keys [title] :or {title ""}}]
  (when-let [s (f token)]
    (.setToken history s title)))

(defn- token-invalid! [history]
  ;; (println "INVALID TOKEN!")
  (.replaceToken history ""))

(def ^:private key-comparator
  (comparator (fn [x y] (> (index-of y key-order) (index-of x key-order)))))

(defn- new-historian-map [& kvs]
  (into (sorted-map-by key-comparator)
        (apply hash-map kvs)))

(defn- ids->map [ids]
  (into (new-historian-map)
        (map vector key-order ids)))

(defn- token->historian
  "parse a token in a map for the historian"
  [s]
  (when-let [[_ & [ids search-terms]] (-> s
                                          goog.string/urlDecode
                                          match-token)]
     (hash-map
      :ids    (ids->map (str/split (goog.string/urlDecode ids) #","))
      :search (str/split search-terms #"@@@"))))

(defn- historian->token [{:keys [ids search]}]
  (str (->> ids
            vals
            (take-while identity)
            (interpose \,)
            (apply str))
       (when (pos? (count search)) (str "/search/" (str/join "@@@" search)))))

(defn- get-token [history]
   (.getToken history))

(defn token-as-map [history]
  (->> history
       get-token
       token->historian))

(extend-type goog.History
  event/IEventType
  (event-types [this]
    (into {}
          (map
           (fn [[k v]]
             [(keyword (. k (toLowerCase)))
              v])
           (js->clj goog.history.EventType)))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
;; API

(defn new-history
  "Create a history object."
  [order]
  (set! key-order order) ;; TODO don't use atom for this.
  (if (history5/isSupported)
    (goog.history.Html5History. false)
    (goog.History. false)))

(defn set-chan!
  "Set a channel onto which history should be put"
  [history ch]
  (add-navigation-chan! history ch token->historian))

(defn update-token-ids! [history k v & keep-all?]
  (let [{:keys [ids] :as tmap} (token-as-map history)
        keep-keys              (if keep-all? key-order (take-while #(not (keyword-identical? % k)) key-order))
        new-ids                (assoc (select-keys ids keep-keys) k (when v (goog.string/urlEncode v)))]
    (set-token! history (assoc tmap :ids new-ids) historian->token)))

(defn set-token-search! [history xs]
  (let [{:keys [search] :as tmap} (token-as-map history)]
    (set-token! history (assoc tmap :search
                               (map goog.string/urlEncode xs))
                historian->token)))
