(ns com.saberstack.demo.web
  (:require [cljs-bean.core :as b]
            [org.zsxf.query :as q]
            [react]
            [react-native :as rn]
            [ss.expo.core :as expo]
            [react-native-chart-kit :as chart-kit]
            [org.zsxf.datalog.compiler :as zsxf.c]
            [org.zsxf.datascript :as ds]
            [datascript.core :as d]
            [com.saberstack.demo.ticker-feed :as ticker-feed]
            [ss.react-native.core :as r]
            [ss.react.root :as react-root]
            [ss.react.core :as rc]
            [org.zsxf.util :as util]
            [ss.react.state :as state]
            [taoensso.timbre :as timbre]))

(defonce *root-refresh-hook (atom nil))
(defonce *chart-refresh-hook (atom nil))

(defonce *conn (atom nil))

(defonce *line-data (atom
                      (let [data {:labels   []
                                  :datasets [{:data []}]}]
                        data)))

(defn add-data-to-chart
  [tx-time]
  (swap! *line-data
    (fn [m] (update-in m [:datasets 0 :data] (fn [v]
                                               (if (< 999 (count v))
                                                 [(int tx-time)]
                                                 (conj v (int tx-time))))))))


(defn render-state
  [{:btc/keys [buy-query sell-query] :as m}]
  (update-vals m (fn [query] (count (q/get-result query)))))

(defn render-state-datascript
  [{:btc/keys [buy-query sell-query]}]
  (let [conn @@*conn]
    {:btc/buy-query
     (util/time-f
       (count (d/q '[:find ?te
                     :where
                     [?te :side "buy"]
                     [?te :product_id "BTC-USD"]]
                conn))
       (fn [tx-time]
         (add-data-to-chart tx-time)))
     :btc/sell-query
     (util/time-f
       (count (d/q '[:find ?te
                     :where
                     [?te :side "sell"]
                     [?te :product_id "BTC-USD"]]
                conn))
       (fn [tx-time]
         (add-data-to-chart tx-time)))
     :ltc/buy-query
     (util/time-f
       (count (d/q '[:find ?te
                     :where
                     [?te :side "buy"]
                     [?te :product_id "LTC-USD"]]
                conn))
       (fn [tx-time]
         (add-data-to-chart tx-time)))
     :ltc/sell-query
     (util/time-f
       (count (d/q '[:find ?te
                     :where
                     [?te :side "sell"]
                     [?te :product_id "LTC-USD"]]
                conn))
       (fn [tx-time]
         (add-data-to-chart tx-time)))
     :eth/buy-query
     (util/time-f
       (count (d/q '[:find ?te
                     :where
                     [?te :side "buy"]
                     [?te :product_id "ETH-USD"]]
                conn))
       (fn [tx-time]
         (add-data-to-chart tx-time)))
     :eth/sell-query
     (util/time-f
       (count (d/q '[:find ?te
                     :where
                     [?te :side "sell"]
                     [?te :product_id "ETH-USD"]]
                conn))
       (fn [tx-time]
         (add-data-to-chart tx-time)))}))

(def font-size 28)

(rc/defnrc grid-cell-component [props]
  (r/view {:style {:flex           1
                   :alignItems     "center"
                   :justifyContent "center"}}
    (r/text {:style {:fontSize font-size}}
      props)))
(def grid-cell (rc/e grid-cell-component))

(rc/defnrc demo-component [{state :state}]
  (r/view {:style {:flex 1}}
    ;header
    (r/view {:style {:flex 0.05 :backgroundColor "black"}}
      (r/view {:style {:flex 1 :flexDirection "row"}}
        (r/view {:style {:flex           1
                         :alignItems     "center"
                         :justifyContent "center"
                         :flexDirection  "row"}}
          (r/text {:style {:fontSize 15 :color "white"}}
            "Cryptocurrency live sentiment statistics | ")
          (r/text {:style {:fontSize 15 :fontWeight "bold" :color "white"} :href "https://github.com/saberstack/zsxf"}
            "Github"))))
    (r/view {:style {:flex 0.2}}
      (r/view {:style {:flex 1 :flexDirection "row"}}
        (grid-cell "Symbol / Trade book")
        (grid-cell "Sell count")
        (grid-cell "Buy count")))
    (r/view {:style {:flex 1 :flexDirection "row" :backgroundColor "#fddb29"}}
      (r/view {:style {:flex 1 :backgroundColor "#f6cf00"}}
        (grid-cell "BTC-USD")
        (grid-cell "ETH-USD")
        (grid-cell "LTC-USD"))
      (r/view {:style {:flex 1}}
        (grid-cell (:btc/sell-query state))
        (grid-cell (:eth/sell-query state))
        (grid-cell (:ltc/sell-query state)))
      (r/view {:style {:flex 1}}
        (grid-cell (:btc/buy-query state))
        (grid-cell (:eth/buy-query state))
        (grid-cell (:ltc/buy-query state))))))
(def demo (rc/e demo-component))

(defn measure-time
  "Similar to time but as a fn
  Returns a vector of elapsed time and result of calling f"
  [f]
  (let [t1       (system-time)
        f-return (f)
        t2       (system-time)]
    [(- t2 t1) f-return]))


;Chart
(def line-chart (partial rc/create-element-js (.-LineChart ^js/Object chart-kit)))


(rc/defnrc perf-chart-component [{:keys [line-data] :as props}]
  (let [[_ refresh-hook] (rc/use-state (random-uuid))
        {:keys [width height] :as w} (r/use-window-dimensions)
        _          (reset! *chart-refresh-hook refresh-hook)
        line-data' (b/->js line-data)]
    (line-chart
      (b/->js
        {:data        line-data'
         :width       width
         :height      110
         :yAxisLabel  ""
         :yAxisSuffix "ms"
         :chartConfig {:backgroundColor        "#fff"
                       :backgroundGradientFrom "#fff"
                       :backgroundGradientTo   "#fff"
                       :decimalPlaces          1
                       :color                  (fn [opacity] (str "rgba(0, 0, 0, " opacity ")"))
                       :labelColor             (fn [opacity] (str "rgba(0, 0, 0, " opacity ")"))
                       :style                  {:borderRadius 3}}
         :bezier      false}))))

(def perf-chart (rc/e perf-chart-component))

(rc/defnrc root-component [props]
  ;(timbre/info "Root RENDER" props)
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _         (reset! *root-refresh-hook root-refresh-hook)
        ret       (render-state-datascript props)
        line-data @*line-data]
    (r/view
      {:style {:flex 1}}
      (demo {:state ret})
      (perf-chart
        {:line-data line-data}))))

(def root (rc/e root-component))

(defn setup-websocket! [conn]
  (ticker-feed/new-web-socket!
    {:callback-f
     (fn [^js/Object ws-message]

       (let [json-string (.-data ws-message)
             data        (js->clj (js/JSON.parse json-string) :keywordize-keys true)]
         ;(timbre/info data)
         (d/transact! conn [data])))}))

(defn conn-render-listener []
  (when-let [refresh-root-hook @*root-refresh-hook]
    (refresh-root-hook (random-uuid))))



(defn render-setup! []
  ;(timbre/info "pre-render...")
  (let [conn    (d/create-conn {})
        _       (d/listen! conn conn-render-listener)
        _       (reset! *conn conn)
        query-1 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "buy"]
                      [?te :product_id "BTC-USD"]]))
        query-2 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "sell"]
                      [?te :product_id "BTC-USD"]]))
        query-3 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "buy"]
                      [?te :product_id "ETH-USD"]]))
        query-4 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "sell"]
                      [?te :product_id "ETH-USD"]]))
        query-5 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "buy"]
                      [?te :product_id "LTC-USD"]]))
        query-6 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "sell"]
                      [?te :product_id "LTC-USD"]]))
        _       (timbre/info "setup...")
        _       (ds/init-query-with-conn query-1 conn
                  :tx-time-f
                  (fn [tx-time]

                    ))
        _       (ds/init-query-with-conn query-2 conn
                  :tx-time-f
                  (fn [tx-time]

                    ))
        _       (ds/init-query-with-conn query-3 conn
                  :tx-time-f
                  (fn [tx-time]

                    ))
        _       (ds/init-query-with-conn query-4 conn
                  :tx-time-f
                  (fn [query-4-tx-time]
                    ))
        _       (ds/init-query-with-conn query-5 conn
                  :tx-time-f
                  (fn [query-5-tx-time]
                    ))
        _       (ds/init-query-with-conn query-6 conn
                  :tx-time-f
                  (fn [query-6-tx-time]
                    ))]
    (setup-websocket! conn)
    ;Render here
    (r/view {:style {:flex 1}}
      (root {:btc/buy-query  query-1
             :btc/sell-query query-2
             :eth/buy-query  query-3
             :eth/sell-query query-4
             :ltc/buy-query  query-5
             :ltc/sell-query query-6}))
    ))

(defn init []
  (expo/register-root-component
    (fn [] (render-setup!))))

;; the function figwheel-rn-root MUST be provided. It will be called by
;; by the react-native-figwheel-bridge to render your application.
(defn figwheel-rn-root []
  (render-setup!))

(defn -main [& args]
  (init)
  (println "Hello RN web from CLJS"))

(when (expo/prod?)
  (init))

(comment

  (ticker-feed/close! @ticker-feed/*ws)

  (d/transact! @*conn [{:side "buy" :product_id "USD"}])
  )
