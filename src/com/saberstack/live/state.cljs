(ns com.saberstack.live.state
  (:require
   [clojure.core.async :as a]
   [ss.cljs.fetch :as fetch]
   [ss.cljs.promises]
   [goog.string :as gstr]
   [cljs.pprint :as pprint]
   [taoensso.timbre :as timbre]))


;; Manages the application's top-level state.
;; Using `defonce` and atoms ensures that state is preserved
;; during development across hot-reloads of the code.
(defonce *bootloader-refresh-hook (atom nil))
(defonce *root-refresh-hook (atom nil))
(defonce *bootloader-state (atom {:render :index}))
(defonce *app-state
  (atom {:queries      []
         :query-result (str "Select a query to display results")
         :query-name   nil
         :company-name "Saberstack"
         :one-liner    "Rebuilding databases to answer the hardest questions in milliseconds.\nNo Snowflake required."}))

;; A higher-order function that creates a watch handler for an atom.
;; This handler, when attached to a state atom, triggers a UI refresh
;; by invoking a refresh hook stored in `*refresh-hook`. It's a key part
;; of the state management system, connecting global state changes
;; to component re-renders. The state change is ignored if the new state
;; is the same as the old state, preventing unnecessary updates.
(defn watch-refresh-hook [*refresh-hook]
  (fn [_watch-key _atom old-state new-state]
    (if (= old-state new-state)
      true
      (when-let [refresh-hook @*refresh-hook]
        (refresh-hook (random-uuid))))))

(defn init-watches []
  ;Adding watches to bootloader and root refresh hooks
  (add-watch *bootloader-state :watch-1 (watch-refresh-hook *bootloader-refresh-hook))
  (add-watch *app-state :watch-1 (watch-refresh-hook *root-refresh-hook)))


(def domain "https://demo.saberstack.com")

(defn path [& params]
  (apply str domain params))

(defn get-queries! []
  (a/go
    (let [resp (a/<!
                 (fetch/fetch-transit (path "/queries")))]
      (when (vector? resp)
        (timbre/info "resp::" resp)
        (swap! *app-state assoc :queries resp))
      :done)))

(defn truncate-string [s limit]
  (if (< limit (count s))
    (str (subs s 0 limit) "...")
    s))

(defn get-query-result! [a-name]
  (when a-name
    (a/go
      (let [resp (a/<! (fetch/fetch-transit (path "/query/" (str a-name) "/result")))]
        (timbre/info "resp::" resp)
        (swap! *app-state assoc :query-result
          (str "Result count: "
            (count resp) "\n\n"
            (with-out-str
              (pprint/pprint
                (into #{}
                  (map (fn [[txt]]
                         [(gstr/unescapeEntities
                            (truncate-string txt 120))])
                    resp))))))
        :done))))

(defn set-current-query! [a-name]
  (swap! *app-state assoc :query-name a-name))

(comment
  (get-query-result! 'get-all-clojure-mentions-by-raspasov))
