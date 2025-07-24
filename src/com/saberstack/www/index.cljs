(ns com.saberstack.www.index
  (:require [ss.expo.core :as expo]
            [ss.react-native.core :as r]
            [ss.react.core :as rc]))

;; Manages the application's top-level state.
;; Using `defonce` and atoms ensures that state is preserved
;; during development across hot-reloads of the code.
(defonce *bootloader-refresh-hook (atom nil))
(defonce *root-refresh-hook (atom nil))
(defonce *bootloader-state (atom {:render :index}))
(defonce *app-state (atom {:company-name "Saberstack"}))

;; The primary UI of the application.
;; It captures its own refresh hook to allow external processes
;; to trigger a re-render, a simple and effective state management technique.
(rc/defnrc root-component [{:keys [] :as _props}]
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _         (reset! *root-refresh-hook root-refresh-hook)
        app-state @*app-state]
    (r/view
      {:style {:flex 1 :backgroundColor "black"}}
      (r/text {:style {:color "white"}}
        (:company-name app-state)))))
;; Creates a renderable React element from the root component.
(def root (rc/e root-component))

;; A wrapper function that renders the main application view.
(defn render-setup-index [& {:keys []}]
  (r/view {:style {:flex 1}}
    (root {})))

;; This component acts as a bootloader or a simple router.
;; It consults the shared application state (`*bootloader-state`)
;; to decide which view to render, decoupling the rendering logic
;; from the application's entry point.
(rc/defnrc bootloader-component [_]
  (let [[_ refresh-hook] (rc/use-state (random-uuid))
        _ (reset! *bootloader-refresh-hook refresh-hook)
        {:keys [render]} @*bootloader-state]
    (condp = render
      :index (render-setup-index))))
;; Creates a renderable React element from the bootloader component.
(def bootloader (rc/e bootloader-component))

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

;; Initializes the application by registering the top-level
;; component with Expo. This is the standard entry point.
(defn init []
  (add-watch *bootloader-refresh-hook :watch-1 (watch-refresh-hook *bootloader-refresh-hook))
  (add-watch *app-state :watch-1 (watch-refresh-hook *root-refresh-hook))
  (expo/register-root-component
    (fn []
      (bootloader {}))))

;; Provides the entry point for Figwheel's hot-reloading bridge.
;; This function MUST be provided for the development environment to work correctly.
(defn figwheel-rn-root []
  (bootloader {}))

(defn -main [& args]
  (init)
  (println "Hello RN web from CLJS"))

;; This form ensures the application is initialized for production builds,
;; where Figwheel is not present.
(when (expo/prod?)
  (init))
