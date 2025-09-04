(ns com.saberstack.live.index
  (:require [cljs-bean.core :as b]
            [ss.expo.core :as expo]
            [ss.react-native.core :as r]
            [com.saberstack.live.state :as state]
            [expo-font :refer [useFonts]]
            [com.saberstack.live.font.core :as font]
            [com.saberstack.live.component :as component]
            [ss.react.core :as rc]
            [ss.cljs.promises]
            [taoensso.timbre :as timbre]))

(ss.cljs.promises/extend-promises-as-pair-channels!)

(def color-near-black "#08090a")
(def color-near-white "#f7f8f8")
(def color-gray "#b5b6b6")
(def color-black "black")

(defn ->color-near-white [opacity]
  (str "rgba(247,248,248," opacity ")"))

(defn str-arrow>
  ([s] (str-arrow> 16 s))
  ([size s]
   (r/text {}
     (r/text {:style {:fontSize size}}
       "› ")
     s)))

(defn str-arrow-down
  ([s] (str-arrow-down 16 s))
  ([size s]
   (r/text {}
     (r/text {:style {:fontSize size :position "relative" :top -2}}
       (str "⌄ "))
     s)))

(defn live
  ([] (live 1))
  ([size]
   (r/text
     {:style {:marginHorizontal (* size 4)
              :fontSize         (* size 9) :color color-near-white :fontFamily "Inter-Bold" :backgroundColor "green" :padding (* size 3) :borderRadius (* size 4)}}
     "LIVE")))

(rc/defnrc navigation-component [{:keys [queries] :as _props}]
  (let []
    (into []
      (map-indexed
        (fn [idx {query-doc :doc}]
          (r/touchable-opacity {:key idx :style {:borderRadius 6 :minHeight 60 :paddingLeft "5%" :paddingVertical "2.5%" :backgroundColor (->color-near-white 0)}}
            (r/text {:style {:marginVertical "auto" :fontFamily "Inter-Regular" :color color-near-white :fontSize 15 :marginBottom "auto"}}
              (str-arrow> query-doc)))))
      queries)))
(def navigation (rc/e navigation-component))

;; The primary UI of the application.
(rc/defnrc root-component [{:keys [] :as _props}]
  ;; A refresh hook to allow external processes to trigger a re-render
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _ (reset! state/*root-refresh-hook root-refresh-hook)
        [fonts-loaded fonts-error] (useFonts (font/inter))
        {:keys [queries] :as app-state} @state/*app-state]
    (r/scroll-view
      {:style                 {:flex 1 :backgroundColor color-near-black}
       :contentContainerStyle {}}
      ;Header


      (r/view {:style {:alignSelf "flex-start" :padding "1%" :marginLeft "1%"}}
        (component/logo))

      (r/view {:style {:height 1 :backgroundColor (->color-near-white 0.08)}})



      (r/view {:style {:minHeight 500 :flexDirection "row"}}

        ;left side
        (r/view {:style {:flex 2.5 :backgroundColor color-near-black :maxWidth 400 :padding "2%" :justifyContent "flex-start"}}
          (r/text {:style {:marginBottom "8%"
                           :fontFamily   "Inter-SemiBold" :color color-gray :fontSize 23}} "Live Datasets")
          (r/view {}
            (r/view {:style {:marginBottom "6%"}}
              (r/text
                {:style {:fontFamily "Inter-SemiBold" :color color-near-white :fontSize 23 :marginBottom "auto"}}
                (str-arrow-down 15 "HackerNews")
                )

              (r/text {:style {:marginLeft "5%" :fontFamily "Inter-Regular" :fontSize 12 :color "darkgray"}}
                "242,761,759 items"
                (live)))
            (navigation {:queries queries})
            #_(r/touchable-opacity {:style {:borderRadius 6 :minHeight 60 :paddingLeft "5%" :paddingVertical "2.5%" :backgroundColor (->color-near-white 0.12)}}
              (r/text {:style {:marginVertical "auto" :fontFamily "Inter-Regular" :color color-near-white :fontSize 15 :marginBottom "auto"}}
                (str-arrow> "All mentions of \"Clojure\" by specific user")))

            #_(r/touchable-opacity {:style {:borderRadius 6 :minHeight 60 :paddingLeft "5%" :paddingVertical "2.5%" :backgroundColor (->color-near-white 0)}}
              (r/text {:style {:marginVertical "auto" :fontFamily "Inter-Regular" :color color-near-white :fontSize 15 :marginBottom "auto"}}
                (str-arrow> "All HackerNews users since launch")))))
        ;border
        (r/view {:style {:width 1 :backgroundColor (->color-near-white 0.08)}})
        ;right side
        (r/view {:style {:flex 6 :backgroundColor color-near-black :padding "2%" :justifyContent "flex-start"}}
          (r/text {:style {:marginBottom "4%" :fontFamily "Inter-SemiBold" :color color-gray :fontSize 23}} "Query")
          (r/text {:style {:color color-near-white :fontFamily "monospace" :marginBottom "5%"}}
            "'[:find ?txt\n  :where\n  [?e :hn.item/by ?user]\n  [?e :hn.item/text ?txt]\n  [(clojure.string/includes? ?user \"raspasov\")]\n  [(clojure.string/includes? ?txt \"Clojure\")]]")
          (r/text {:style {:marginBottom "3%"
                           :fontFamily   "Inter-SemiBold" :color color-gray :fontSize 23}}
            "Live Results")
          (r/text {:style {:color color-gray :fontFamily "monospace"}}
            "#{...\n  ...\n  ...\n  ...\n  ...\n  ...\n  ...\n  ...\n  ...}")))
      (r/view {:style {:height 1 :backgroundColor (->color-near-white 0.08)}})

      )))
;; Creates a renderable React element from the root component.
(def root (rc/e root-component))
