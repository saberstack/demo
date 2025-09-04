(ns com.saberstack.live.bootloader
  (:require [com.saberstack.live.state :as state]
            [ss.expo.core :as expo]
            [com.saberstack.live.index :as index]
            [ss.react.core :as rc]))

;; A wrapper function that renders the main application view.
(defn render-setup-index [& {:keys []}]
  (let [_ (rc/use-effect-once (fn []
                                (state/get-queries!)
                                (fn cleanup [])))]
    (index/root {})))

;; This component acts as a bootloader or a simple router.
;; It consults the shared application state (`*bootloader-state`)
;; to decide which view to render, decoupling the rendering logic
;; from the application's entry point.
(rc/defnrc bootloader-component [_]
  (let [[_ refresh-hook] (rc/use-state (random-uuid))
        _ (reset! state/*bootloader-refresh-hook refresh-hook)
        {:keys [render]} @state/*bootloader-state]
    (condp = render
      :index (render-setup-index))))
;; Creates a renderable React element from the bootloader component.
(def bootloader (rc/e bootloader-component))


;; Initializes the application by registering the top-level
;; component with Expo. This is the standard entry point.
(defn init []
  (state/init-watches)
  (expo/register-root-component
    (fn []
      (bootloader {}))))

;; Provides the entry point for Figwheel's hot-reloading bridge.
;; This function MUST be provided for the development environment to work correctly.
(defn figwheel-rn-root []
  ;(timbre/info "figwheel-rn-root called")
  (state/init-watches)
  (bootloader {}))

(defn -main [& args]
  ;(timbre/info "-main called with args:" args)
  ;(println "Hello RN web from CLJS")
  (init))

;; This form ensures the application is initialized for production builds,
;; where Figwheel is not present.
(when (expo/prod?)
  (init))
