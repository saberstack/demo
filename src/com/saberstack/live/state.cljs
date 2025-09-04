(ns com.saberstack.live.state
  (:require
   [ss.cljs.fetch :as fetch]
   [ss.cljs.promises]))

(def domain "https://demo.saberstack.com")

(defn path [& params]
  (apply str domain params))

(defn get-queries []
  (fetch/fetch-transit (path "/queries")))

;; Manages the application's top-level state.
;; Using `defonce` and atoms ensures that state is preserved
;; during development across hot-reloads of the code.
(defonce *bootloader-refresh-hook (atom nil))
(defonce *root-refresh-hook (atom nil))
(defonce *bootloader-state (atom {:render :index}))
(defonce *app-state
  (atom {:company-name "Saberstack"
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
