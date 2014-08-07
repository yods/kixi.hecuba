(ns kixi.hecuba.tabs.hierarchy.programmes
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.bootstrap :refer (text-input-control static-text checkbox alert) :as bs]
   [kixi.hecuba.common :refer (log) :as common]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-programmes)]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; programmes

(defn error-handler [owner]
  (fn [{:keys [status status-text]}]
    (om/set-state! owner :error true)
    (om/set-state! owner :http-error-response {:status status
                                               :status-text status-text})))

(defn post-new-programme [data owner programme]
  (common/post-resource data (str "/4/programmes/")
                        (assoc programme :created_at (common/now->str))
                        (fn [_]
                          (fetch-programmes data)
                          (om/update! data [:programmes :adding-programme] false))
                        (error-handler owner)))

(defn put-edited-programme [data owner url programme]
  (common/put-resource data url 
                       (assoc programme :updated_at (common/now->str))
                       (fn [_]
                         (fetch-programmes data)
                         (om/update! data [:programmes :editing] false))
                       (error-handler owner)))

(defn programme-add-form [data]
  (fn [cursor owner]
    (om/component
     (html
      (let [{:keys [status-text]} (om/get-state owner :http-error-response)
            error      (om/get-state owner :error)
            alert-body (if status-text
                         (str " Server returned status: " status-text)
                         " Please enter name of the programme.")]
        [:div
         [:h3 "Add new programme"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button.btn.btn-success {:type "button"
                                       :onClick (fn [_] (let [programme (om/get-state owner [:programme])]
                                                          (if (:name programme)
                                                            (post-new-programme data owner programme)
                                                            (om/set-state! owner [:error] true))))}
              "Save"]
             [:button.btn.btn-danger {:type "button"
                                      :onClick (fn [_] (om/update! data [:programmes :adding-programme] false))}
              "Cancel"]]]
           (alert "alert alert-danger "
                  [:div [:div {:class "fa fa-exclamation-triangle"} alert-body]]
                  error
                  (str "new-programme-form-failure"))
           (text-input-control cursor owner :programme :name "Programme Name" true)
           (text-input-control cursor owner :programme :description "Description")
           (text-input-control cursor owner :programme :home_page_text "Home Page Text")
           (text-input-control cursor owner :programme :lead_organisations "Lead Organisations")
           (text-input-control cursor owner :programme :lead_page_text "Lead Page Text")
           (text-input-control cursor owner :programme :leaders "Leaders")
           (text-input-control cursor owner :programme :public_access "Public Access")]]])))))

(defn programme-edit-form [data]
  (fn [cursor owner]
    (om/component
     (let [programme-id (-> data :active-components :programmes)
           {:keys [status-text]} (om/get-state owner :http-error-response)
            error      (om/get-state owner :error)
            alert-body (str " Server returned status: " status-text)]
       (html
        [:div
         [:h3 "Editing Programme"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button.btn.btn-success {:type "button"
                                       :onClick (fn [_] (let [programme (om/get-state owner [:programme])
                                                              url       (str "/4/programmes/" programme-id)]
                                                          (put-edited-programme data owner url programme)))} "Save"]
             [:button.btn.btn-danger {:type "button"
                                       :onClick (fn [_] (om/update! data [:programmes :editing] false))} "Cancel"]]]
           (static-text cursor :id "Programme ID")
           (alert "alert alert-danger "
                  [:div [:div {:class "fa fa-exclamation-triangle"} alert-body]]
                  error
                  (str "edit-programme-form-failure"))
           (text-input-control cursor owner :programme :created_at "Created At")
           (text-input-control cursor owner :programme :description "Description")
           (text-input-control cursor owner :programme :home_page_text "Home Page Text")
           (text-input-control cursor owner :programme :lead_organisations "Lead Organisations")
           (text-input-control cursor owner :programme :lead_page_text "Lead Page Text")
           (text-input-control cursor owner :programme :leaders "Leaders")
           (checkbox cursor owner :programme :public_access "Public Access")]]])))))

(defn programmes-row [data history programmes table-id editing-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [id lead_organisations name description created_at editable]} cursor
               selected? (= (:selected programmes) id)]
           [:tr {:onClick (fn [e]
                            (let [div-id (.-id (.-target e))]
                              (when-not (= div-id (str id "-edit"))
                                (om/update! programmes :selected id)
                                (history/update-token-ids! history :programmes id)
                                (common/fixed-scroll-to-element "projects-div"))))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td [:div (when editable {:class "fa fa-pencil-square-o" :id (str id "-edit")
                                       :onClick (fn [_] (when selected? (put! editing-chan cursor)))})]]
            [:td name]
            [:td lead_organisations]
            [:td id]
            [:td created_at]]))))))

(defn programmes-table [editing-chan]
  (fn [data owner]
    (reify
      om/IRender
      (render [_]
        (let [programmes   (-> data :programmes)
              table-id     "programme-table"
              history      (om/get-shared owner :history)]
          (html
           [:table {:className "table table-hover"}
            [:thead
             [:tr
              [:th ""]
              [:th "Name"]
              [:th "Organisations"]
              [:th "ID"]
              [:th "Created At"]]]
            [:tbody
             (for [row (sort-by :name (:data programmes))]
               (om/build (programmes-row data history programmes table-id editing-chan) row))]]))))))

(defn programmes-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! data [:programmes :editing] true)
          (om/update! data [:programmes :edited-row] edited-row)
          (common/fixed-scroll-to-element "programmes-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (html
       (let [editing          (-> data :programmes :editing)
             adding-programme (-> data :programmes :adding-programme)]
         [:div.row#programmes-div
          [:div {:class "col-md-12"}
           [:h1 "Programmes"]
           (when (and
                  (not adding-programme)
                  (-> data :programmes :data first :admin))
             [:div.form-group
              [:div.btn-toolbar
               [:button.btn.btn-default {:type "button"
                                         :class (str "btn btn-primary")
                                         :onClick (fn [_] (om/update! data [:programmes :adding-programme] true))}
                "Add new"]]])
           [:div {:id "programmes-add-div" :class (if adding-programme "" "hidden")}
            (om/build (programme-add-form data) nil)]
           [:div {:id "programmes-edit-div" :class (if editing "" "hidden")}
            (om/build (programme-edit-form data) (-> data :programmes :edited-row))]
           [:div {:id "programmes-div" :class (if (or editing adding-programme) "hidden" "")}
            (om/build (programmes-table editing-chan) data)]]])))))