(ns com.saberstack.demo.web
  (:require [org.zsxf.query :as q]
            [react]
            [react-native :as rn]
            [ss.expo.core :as expo]
            [org.zsxf.datalog.compiler :as zsxf.c]
            [org.zsxf.datascript :as ds]
            [datascript.core :as d]
            [com.saberstack.demo.ticker-feed :as ticker-feed]
            [ss.react-native.core :as r]
            [ss.react.root :as react-root]
            [ss.react.core :as rc]
            [ss.react.state :as state]
            [taoensso.timbre :as timbre]))

(defonce *root-refresh-hook (atom nil))

(defonce *conn (atom nil))

(defn render-state
  [{:btc/keys [buy-query sell-query] :as m}]
  (update-vals m (fn [query] (count (q/get-result query)))))

(defn render-state-datascript
  [{:btc/keys [buy-query sell-query]}]
  (let [conn @@*conn]
    (into {}
      [[:btc/buy-query (count (d/q '[:find ?te
                                     :where
                                     [?te :side "buy"]
                                     [?te :product_id "BTC-USD"]]
                                conn))]
       [:btc/sell-query (count (d/q '[:find ?te
                                      :where
                                      [?te :side "sell"]
                                      [?te :product_id "BTC-USD"]]
                                 conn))]])))

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

(rc/defnrc root-component [props]
  ;(timbre/info "Root RENDER" props)
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _ (reset! *root-refresh-hook root-refresh-hook)
        ;[t ret] (measure-time #(render-state props))
        [t ret] (measure-time #(render-state props))]
    (demo {:render-time (int t)
           :state       ret})))

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

(defn pre-render-setup! []
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
        _       (ds/init-query-with-conn query-1 conn)
        _       (ds/init-query-with-conn query-2 conn)
        _       (ds/init-query-with-conn query-3 conn)
        _       (ds/init-query-with-conn query-4 conn)
        _       (ds/init-query-with-conn query-5 conn)
        _       (ds/init-query-with-conn query-6 conn)]
    (setup-websocket! conn)
    (root {:btc/buy-query  query-1
           :btc/sell-query query-2
           :eth/buy-query  query-3
           :eth/sell-query query-4
           :ltc/buy-query  query-5
           :ltc/sell-query query-6
           })))

(defn init []
  (expo/register-root-component
    (fn [] (pre-render-setup!))))

;; the function figwheel-rn-root MUST be provided. It will be called by
;; by the react-native-figwheel-bridge to render your application.
(defn figwheel-rn-root []
  (pre-render-setup!))

(defn -main [& args]
  (init)
  (println "Hello RN web from CLJS"))

(when (expo/prod?)
  (init))

(comment

  (ticker-feed/close! @ticker-feed/*ws)

  (d/transact! @*conn [{:side "buy" :product_id "USD"}])
  )
