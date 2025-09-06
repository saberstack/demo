(ns com.saberstack.live.index
  (:require [cljs-bean.core :as b]
            [clojure.core.async :as a]
            [org.saberstack.clojure.core.async.loop :as ss.loop]
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

(rc/defnrc navigation-component
  [{:keys [queries current-query] :as _props}]
  (let []
    (into []
      (map-indexed
        (fn [idx [{a-name :name query-doc :doc} -current-query]]
          (r/touchable-opacity
            {:onPress (fn [_]
                        (state/set-current-query! a-name)
                        (state/get-query-result! a-name))
             :key     idx :style {:borderRadius    6 :minHeight 60 :paddingLeft "5%" :paddingVertical "2.5%"
                                  :backgroundColor (->color-near-white (if (= a-name -current-query) 0.12 0))}}
            (r/text {:style {:marginVertical "auto" :fontFamily "Inter-Regular" :color color-near-white :fontSize 15 :marginBottom "auto"}}
              (str-arrow> query-doc)))))
      (sequence
        (map (fn [x y] [x y]))
        queries
        (repeat current-query)))))
(def navigation (rc/e navigation-component))

(defn live-item-count-refresh
  []
  (ss.loop/go-loop
    ^{:id :live-item-count-refresh}
    []
    ;(timbre/info query-name "::: going to refresh ")
    (state/get-items-count!)
    ;(timbre/info query-name "::: refreshed")
    (a/<! (a/timeout 3000))
    (recur)))

(rc/defnrc items-cnt-component
  [{:keys [items-count] :as _props}]
  (let [_ (rc/use-effect
            (fn []
              (live-item-count-refresh)
              (fn cleanup [] (ss.loop/stop :live-item-count-refresh)))
            #js [items-count])]
    (r/text {:style {:marginLeft "5%" :fontFamily "Inter-Regular" :fontSize 12 :color "darkgray"}}
      (str items-count " items")
      (live))))

(def items-cnt (rc/e items-cnt-component))

(defn live-result-refresh
  [query-name]
  (ss.loop/go-loop
    ^{:id :live-result-refresh}
    []
    ;(timbre/info query-name "::: going to refresh ")
    (state/get-query-result! query-name)
    ;(timbre/info query-name "::: refreshed")
    (a/<! (a/timeout 3000))
    (recur)))

(rc/defnrc live-query-result-component
  [{:keys [query-result query-name] :as _props}]
  (let [_ (timbre/info query-name "::: RENDER live-query-result-component")
        _ (rc/use-effect
            (fn []
              (live-result-refresh query-name)
              (fn cleanup [] (ss.loop/stop :live-result-refresh)))
            #js [query-name])])
  (r/view {}
    (r/text {:style {:marginBottom "3%" :fontFamily "Inter-SemiBold" :color color-gray :fontSize 23}}
      "Live Results")
    (r/text {:style {:color color-gray :fontFamily "monospace"}}
      query-result)))
(def live-query-result (rc/e live-query-result-component))


;; The primary UI of the application.
(rc/defnrc root-component [{:keys [] :as _props}]
  ;; A refresh hook to allow external processes to trigger a re-render
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _ (reset! state/*root-refresh-hook root-refresh-hook)
        [fonts-loaded fonts-error] (useFonts (font/inter))
        {:keys [queries query-result query-name items-count] :as app-state} @state/*app-state
        _ (println "items-count:::" items-count)]
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
                (str-arrow-down 15 "HackerNews"))
              (items-cnt {:items-count items-count}))
            (navigation {:queries queries :current-query query-name})))
        ;border
        (r/view {:style {:width 1 :backgroundColor (->color-near-white 0.08)}})
        ;right side
        (r/view {:style {:flex 6 :backgroundColor color-near-black :padding "2%" :justifyContent "flex-start"}}
          #_(r/text {:style {:marginBottom "4%" :fontFamily "Inter-SemiBold" :color color-gray :fontSize 23}} "Query")
          #_(r/text {:style {:color color-near-white :fontFamily "monospace" :marginBottom "5%"}}
              "'[:find ?txt\n  :where\n  [?e :hn.item/by ?user]\n  [?e :hn.item/text ?txt]\n  [(clojure.string/includes? ?user \"raspasov\")]\n  [(clojure.string/includes? ?txt \"Clojure\")]]")
          (live-query-result {:query-result query-result :query-name query-name})))
      (r/view {:style {:height 1 :backgroundColor (->color-near-white 0.08)}})

      )))
;; Creates a renderable React element from the root component.
(def root (rc/e root-component))
