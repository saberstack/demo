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
  [{:btc/keys [buy-query sell-query]}]
  [[:btc/buy-query (count (q/get-result buy-query))]
   [:btc/sell-query (count (q/get-result sell-query))]])

(defn render-state-datascript
  [{:btc/keys [buy-query sell-query]}]
  (let [conn @@*conn]
    {:todo :todo}
    [[:btc/buy-query (count (d/q '[:find ?te
                                   :where
                                   [?te :side "buy"]
                                   [?te :product_id "BTC-USD"]]
                              conn))]
     [:btc/sell-query (count (d/q '[:find ?te
                                    :where
                                    [?te :side "sell"]
                                    [?te :product_id "BTC-USD"]]
                               conn))]]))

(rc/defnrc demo-component [props]
  (timbre/info "render demo...")
  (r/view {:style {:backgroundColor "white" :flex 1}}
    (r/text {} (str props))))
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
  (timbre/info "Root RENDER")
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _ (reset! *root-refresh-hook root-refresh-hook)
        ;[t ret] (measure-time #(render-state props))
        [t ret] (measure-time #(render-state-datascript props))]
    (demo {:render-time t
           :state       ret})))
(def root (rc/e root-component))

(defn setup-websocket! [conn]
  (ticker-feed/new-web-socket!
    {:callback-f
     (fn [^js/Object ws-message]
       (let [json-string (.-data ws-message)
             data        (js->clj (js/JSON.parse json-string) :keywordize-keys true)]
         (d/transact! conn [data])))}))

(defn conn-render-listener []
  (when-let [refresh-root-hook @*root-refresh-hook]
    (refresh-root-hook (random-uuid))))

(defn pre-render-setup! []
  (timbre/info "pre-render...")
  (let [conn    (d/create-conn {})
        _       (d/listen! conn conn-render-listener)
        _       (reset! *conn conn)
        query-1 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "buy"]
                      [?te :product_id "BTC-USD"]]))
        _       (ds/init-query-with-conn query-1 conn)
        query-2 (q/create-query
                  (zsxf.c/static-compile
                    '[:find ?te
                      :where
                      [?te :side "sell"]
                      [?te :product_id "BTC-USD"]]))
        _       (ds/init-query-with-conn query-2 conn)]
    (setup-websocket! conn)
    (root {:btc/buy-query  query-1
           :btc/sell-query query-2})))

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
