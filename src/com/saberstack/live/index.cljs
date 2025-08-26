(ns com.saberstack.live.index
  (:require [cljs-bean.core :as b]
            [ss.expo.core :as expo]
            [ss.react-native.core :as r]
            [com.saberstack.live.state :as state]
            [expo-font :refer [useFonts]]
            [com.saberstack.live.font.core :as font]
            [com.saberstack.live.component :as component]
            [ss.react.core :as rc]
            [taoensso.timbre :as timbre]))

;; The primary UI of the application.
(rc/defnrc root-component [{:keys [] :as _props}]
  ;; A refresh hook to allow external processes to trigger a re-render
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _         (reset! state/*root-refresh-hook root-refresh-hook)
        [fonts-loaded fonts-error] (useFonts (font/inter))
        app-state @state/*app-state]
    ;(timbre/info "Rendering root component with app-state:" app-state)
    ;(timbre/info "Fonts loaded:" fonts-loaded "Error:" fonts-error)
    (r/scroll-view
      {:style                 {:flex 1 :backgroundColor "#08090a"}
       :contentContainerStyle {:marginLeft "auto" :marginRight "auto"}}
      (r/view {:style {:flex 1 :flexDirection "row"}}

        (r/view {:style {:flex             1
                         :flexDirection    "column"
                         :maxWidth         900
                         :justifyContent   "flex-start"
                         :marginVertical   "1%"
                         :marginHorizontal "5%"}}
          (component/logo)
          (r/view {:style {:marginVertical "1%"}}
              (r/view {:style {:marginTop "1"}}
                (r/text {:style
                         {:color    "#f7f8f8" :fontFamily "Inter-Medium" :letterSpacing "-0.038em"
                          :fontSize 40 :textAlign "left"
                          }}
                  (str (:one-liner app-state))))
              (r/view {:style {:marginTop "3%"}}
                  (r/text
                    {:style
                     {:color      "#b5b6b6"
                      :fontFamily "Inter-Regular" :letterSpacing "-0.01em"
                      :fontSize   22 :textAlign "left"}}
                    (str)))

              #_(r/view {:style {:marginTop "3%"}}
                  (r/text {:style
                           {:color    "#f7f8f8" :fontFamily "Inter-Medium" :letterSpacing "-0.038em"
                            :fontSize 24 :textAlign "left"
                            }}
                    (str
                      "A new incremental view maintenance engine for databases."
                      "\nPostgres, Datomic, and even Parquet files."
                      "\n"
                      "We are canceling the data swamp apocalypse.")))
              #_(r/view {:style {:marginTop "3%" :marginBottom "20%"}}
                  (r/touchable-opacity
                    {:onPress (fn [_] (r/open-url "https://github.com/saberstack/zsxf"))}
                    (r/text {:style {:margin   "1%"
                                     :color    "#f7f8f8" :fontFamily "Inter-Medium"
                                     :fontSize 18 :textAlign "left"}}
                      "› github.com/saberstack/zsxf"))
                  (r/touchable-opacity
                    {:onPress (fn [_] (r/open-url "https://discord.gg/J4GWa4DBKu"))}
                    (r/text {:style {:margin   "1%"
                                     :color    "#f7f8f8" :fontFamily "Inter-Medium"
                                     :fontSize 18 :textAlign "left"}}
                      "› discord: join here")))))))))
;; Creates a renderable React element from the root component.
(def root (rc/e root-component))
