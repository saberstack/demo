(ns com.saberstack.live.component
  (:require [ss.react-native.core :as r]
            [ss.react.core :as rc]))

(rc/defnrc logo-component [_props]

  (r/image {:style {:backgroundColor "black"}
            :source {:uri "./assets/saberstack-logo-t@2x.png" :width 64 :height 64}}))
(def logo (rc/e logo-component))
