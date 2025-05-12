(ns com.saberstack.demo.web
  (:require [react]
            [react-native :as rn]
            [ss.expo.core :as expo]
            [ss.react-native.core :as r]))

(defn build-root-view []
  (let []
    (r/view {}
      (r/text {} "Render Now!"))))

(defn init-expo []
  (expo/register-root-component
    (fn [] (build-root-view))))

;; the function figwheel-rn-root MUST be provided. It will be called by
;; by the react-native-figwheel-bridge to render your application.
(defn figwheel-rn-root []
  (init-expo))

(defn -main [& args]
  (init-expo)
  (println "Hello RN web from CLJS"))

(when (expo/prod?)
  (init-expo))
