(ns holiday-ping-ui.channels.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core  :as reagent]
   [holiday-ping-ui.routes :as routes]
   [holiday-ping-ui.common.forms :as forms]
   [holiday-ping-ui.channels.forms :as channel-forms]
   [holiday-ping-ui.common.views :as views]
   [holiday-ping-ui.holidays.views :as holidays]
   [holiday-ping-ui.holidays.calendar :as calendar]))

(defn test-modal
  []
  (let [{name :name :as channel} @(re-frame/subscribe [:channel-to-test])
        cancel-test              #(re-frame/dispatch [:channel-test-cancel])
        confirm-test             #(re-frame/dispatch [:channel-test-confirm channel])]
    [:div.modal
     (when channel {:class "is-active"})
     [:div.modal-background {:on-click cancel-test}]
     [:div.modal-card
      [:header.modal-card-head
       [:p.modal-card-title "Test Channel"]
       [:button.delete {:aria-label "close"
                        :on-click   cancel-test}]]
      [:section.modal-card-body
       [:p "Do you want to test " [:b name] "? "]
       [:p "This will cause a test reminder to be delivered."]
       [:p]]
      [:footer.modal-card-foot
       [:div.modal-button-group
        [:button.button {:on-click cancel-test}
         "Cancel"]
        [:button.button.is-success {:on-click confirm-test}
         [:span.icon.is-small [:i.fa.fa-cogs]]
         [:span "Test"]]]]]]))

(defn item-stats
  [{:keys [name] :as channel}]
  (let [next-holiday  @(re-frame/subscribe [:next-holiday channel])
        last-reminder @(re-frame/subscribe [:last-reminder channel])]
    [:div
     [:p
      [:a {:href (routes/url-for :holidays :channel name)}
       [:i.fa.fa-calendar]
       (if next-holiday
         (str " Next holiday: " next-holiday)
         " No upcoming holidays")]]
     [:p [:i.fa.fa-bell-o]
      (if last-reminder
        (str " Last reminder: " last-reminder)
        " No reminders sent yet")]]))

(defn item-view
  [{:keys [name type] :as channel}]
  [:div.card.channel-card {:key name}
   [:header.card-header
    [:a.card-header-title
     {:href (routes/url-for :channel-edit :channel name)}
     [:p name [:span.card-header-subtitle  type " channel"]]]
    [:div.card-header-icon
     [:div.field.is-grouped
      [:p.control
       [:a.button.is-small
        {:href (routes/url-for :channel-edit :channel name)}
        [:span.icon.is-small [:i.fa.fa-edit]]
        [:span "Edit"]]]
      [:p.control
       [:button.button.is-small
        {:on-click #(re-frame/dispatch [:channel-test-start channel])}
        [:span.icon.is-small [:i.fa.fa-cogs]]
        [:span "Test"]]]]]]
   [:div.card-content
    [item-stats channel]]])

(defn add-button
  []
  [:div.has-text-centered
   [:a.button.is-success {:href (routes/url-for :channel-create)}
    [:span.icon.is-small [:i.fa.fa-plus]]
    [:span "New Channel"]]])

(defn list-view
  []
  (let [channels @(re-frame/subscribe [:channels])]
    [:div
     [test-modal]
     [views/section-size :is-two-thirds
      [:p.subtitle.has-text-centered
       "Setup the channels to send your holiday reminders."]
      [views/message-view]
      [add-button]
      [:br]
      (when-not (empty? channels)
        [:div
         (map item-view channels)])]]))

(defn edit-controls
  [{:keys [name] :as channel}]
  [:div.field.is-grouped.is-grouped-centered

   [:p.control
    [:a.button.is-small
     {:href (routes/url-for :holidays :channel name)}
     [:span.icon.is-small [:i.fa.fa-calendar]]
     [:span "Holidays"]]]
   [:p.control
    [:button.button.is-small
     {:on-click #(re-frame/dispatch [:channel-test-start channel])}
     [:span.icon.is-small [:i.fa.fa-cogs]]
     [:span "Test"]]]
   [:p.control
    [:button.button.is-small.is-danger
     {:on-click #(re-frame/dispatch [:channel-delete name])}
     [:span.icon.is-small [:i.fa.fa-times]]
     [:span "Delete"]]]])

(defn edit-view
  [channel-name]
  (let [channel @(re-frame/subscribe [:channel-to-edit])]
    [:div
     [test-modal]
     [views/section-size :is-half
      [views/breadcrumbs [["Channels" "/"]
                          [channel-name (routes/url-for :channel-edit :channel channel-name)]]]
      [:p.subtitle.has-text-centered "Channel configuration"]
      [edit-controls channel]
      [views/message-view]
      [forms/form-view {:submit-text "Save"
                        :on-submit   [:channel-edit-submit]
                        :on-cancel   [:navigate :channel-list]
                        :defaults    (channel-forms/edit-defaults channel)
                        :fields      (channel-forms/edit-fields channel)}]]]))

;;; WIZARD VIEWS

(defn inc-step
  [state]
  #(swap! state update :step-n inc))

(defn dec-step
  [state]
  #(swap! state update :step-n dec))

(defn wizard-navigation
  "Show next/prev buttons to navigate between steps."
  [prev next]
  (let [show-prev?   (boolean prev)
        show-next?   (boolean next)
        static-class (when (:static next) "is-static")
        on-prev      (get prev :event prev)
        on-next      (get next :event next)]
    [:div
     [:br]
     [:nav.level
      [:div.level-left
       (when show-prev?
         [:div.level-item
          [:button.button
           {:on-click on-prev}
           [:span.icon.is-small
            [:i.fa.fa-chevron-left]]
           [:span "Prev"]]])]
      (when show-next?
        [:div.level-right
         [:div.level-item
          [:button.button.is-right
           {:class static-class :on-click on-next}
           [:span "Next"]
           [:span.icon.is-small
            [:i.fa.fa-chevron-right]]]]])]]))

(defn step-title
  [text]
  [:p.subtitle.is-5.has-text-centered text])

(defn select-type-event
  [state type]
  #(do (swap! state update :step-n inc)
       (swap! state assoc :type type)))

(defn type-card
  [state type title image]
  [:div.column.is-one-quarter
   [:div.card
    [:a {:href "#" :on-click (select-type-event state type)}
     [:header.card-header
      [:p.card-header-title title]]
     [:div.card-content
      [:figure.image.is-2by1
       [:img {:src image}]]]]]])

(defn type-select
  [wizard-state]
  [:div
   [step-title "Select the type of the channel you want to use."]
   [:br]
   [:div.columns.is-centered
    [type-card wizard-state "email" "Email" "/img/email.png"]
    [type-card wizard-state "slack" "Slack" "/img/slack.png"]
    [type-card wizard-state "webhook" "Webhooks" "/img/webhooks.png"]]])

(defn configuration-form
  [wizard-state type]
  (let [form-fields   (channel-forms/wizard-config-fields type)
        channel-state (reagent/cursor wizard-state [:channel-config])
        valid-form?   @(re-frame/subscribe [:valid-form? @channel-state form-fields])]
    [:div.columns.is-centered
     [:div.column.is-half
      [step-title "Fill the configuration for the integration."]
      [forms/detached-form-view channel-state form-fields]
      [wizard-navigation (dec-step wizard-state) {:static (not valid-form?)
                                                  :event  (inc-step wizard-state)}]]]))

(defn reminder-config-form
  [wizard-state]
  (let [reminder-state (reagent/cursor wizard-state [:reminder-config])]
    [:div.columns.is-centered
     [:div.column.is-half
      [step-title "When do you want the reminders sent?"]
      [forms/detached-form-view reminder-state channel-forms/reminders]
      [wizard-navigation (dec-step wizard-state) (inc-step wizard-state)]]]))

(defn holiday-source-form
  [wizard-state]
  (let [source-state (reagent/cursor wizard-state [:source-config])
        channels     @(re-frame/subscribe [:channels])
        source       (:source @source-state)]
    [:div.columns.is-centered
     [:div.column.is-half
      [step-title "What holidays do you want by default on your calendar?"]
      [:form
       [:div.field
        [:div.control
         [:label.radio
          [:input {:type      "radio"
                   :name      "from-country"
                   :checked   (= source :country)
                   :on-change #(swap! source-state assoc :source :country)}]
          " Use country defaults"]]]
       [:div.field
        [:div.control
         [forms/input-view source-state
          {:key      :country
           :type     "select"
           :disabled (not= source :country)
           :options  ["Argentina" "Brazil" "Canada" "India" "Mexico" "Russia" "United States"]
           :help-text [:span "Your country is not listed? "
                       [:a {:href   "https://github.com/lambdaclass/holiday_pinger/issues/new"
                            :target "blank"} "File an issue."]]}]]]
       [:br]

       (when-not (empty? channels)
         [:div
          [:div.field
           [:div.control
            [:label.radio
             [:input {:type      "radio"
                      :name      "from-channel"
                      :checked   (= source :channel)
                      :on-change #(swap! source-state assoc :source :channel)}]
             " Copy from another channel"]]]
          [:div.field
           [:div.control
            [forms/input-view source-state
             {:key      :channel
              :type     "select"
              :disabled (not= source :channel)
              :options  (map :name channels)}]]]
          [:br]])

       [:div.field
        [:div.control
         [:label.radio
          [:input {:type      "radio"
                   :name      "empty"
                   :checked   (= source :empty)
                   :on-change #(swap! source-state assoc :source :empty)}]
          " Start with an empty calendar"]]]]

      [wizard-navigation
       (dec-step wizard-state)
       #(do (re-frame/dispatch [:load-base-holidays @source-state])
            (swap! wizard-state update :step-n inc))]]]))

(defn holiday-controls
  [wizard-state]
  (let [current-year  @(re-frame/subscribe [:current-year])
        next-year     (inc current-year)
        selected-year @(re-frame/subscribe [:calendar-selected-year])]
    [:nav.level
     [:div.level-left
      [:div.level-item
       [:button.button
        {:on-click (dec-step wizard-state)}
        [:span.icon.is-small
         [:i.fa.fa-chevron-left]]
        [:span "Prev"]]]]
     [:div.level-item.has-text-centered [holidays/holidays-year-switch current-year next-year selected-year]]
     [:div.level-right
      [:div.level-item
       [:button.button.is-right.is-success
        {:on-click #(re-frame/dispatch [:wizard-submit @wizard-state])}
        [:span "Save channel"]
        [:span.icon.is-small
         [:i.fa.fa-check]]]]]]))

(defn holiday-config
  [wizard-state]
  (let [current-year  @(re-frame/subscribe [:current-year])
        next-year     (inc current-year)
        selected-year @(re-frame/subscribe [:calendar-selected-year])]
    [:div
     [holidays/edit-holiday-modal]
     [step-title "Select the days of the year for which you want reminders."]
     [holiday-controls wizard-state]
     [:div (when-not (= selected-year current-year) {:hidden true})
      [calendar/year-view current-year]]
     [:div (when-not (= selected-year next-year) {:hidden true})
      [calendar/year-view next-year]]
     [:br]
     [holiday-controls wizard-state]
     [:br]
     [:p.has-text-centered "Missing a national holiday? "
      [:a {:href   "https://github.com/lambdaclass/holiday_pinger/issues/new"
           :target "blank"} "File an issue."]]]))

(defn wizard-steps
  "Show the wizard steps and navigate on click."
  [wizard-state step-n]
  [:div.columns.is-centered
   [:div.column.is-two-thirds
    [:div.steps.is-small
     (for [[i title] [[0 "Type"]
                      [1 "Config"]
                      [2 "Reminders"]
                      [3 "Holidays"]
                      [4 "Calendar"]]]
       (cond
         (= i step-n)
         [:div.step-item.is-active
          {:key i}
          [:div.step-marker]
          [:div.step-content [:p.step-title title]]]

         (< i step-n)
         [:div.step-item.is-completed {:key i}
          [:a.step-marker
           {:href "#" :on-click #(swap! wizard-state assoc :step-n i)}
           [:span.icon [:i.fa.fa-check]]]
          [:div.step-content [:p.step-title title]]]

         (> i step-n)
         [:div.step-item {:key i}
          [:div.step-marker]
          [:div.step-content [:p.step-title title]]]))]]])

(def initial-wizard-state
  {:step-n          0
   :type            "slack"
   :channel-config  {}
   :reminder-config {:same-day    true
                     :days-before 0
                     :time        "09:00"
                     :timezone    channel-forms/default-timezone}
   :source-config   {:source  :country
                     :country "Argentina"}})

(def step-keys [:type-select
                :channel-config
                :reminder-config
                :holidays-source
                :holidays])

(defn create-view
  []
  (let [wizard-state (reagent/atom initial-wizard-state)]
    (fn []
      (let [step-n (:step-n @wizard-state)
            step   (get step-keys step-n)]
        [:div
         [views/section
          [views/breadcrumbs [["Channels" "/"] ["New"]]]
          [wizard-steps wizard-state step-n]
          (case step
            :type-select     [type-select wizard-state]
            :channel-config  [configuration-form wizard-state (:type @wizard-state)]
            :reminder-config [reminder-config-form wizard-state]
            :holidays-source [holiday-source-form wizard-state]
            :holidays        [holiday-config wizard-state])]]))))
