(ns com.saberstack.demo.ticker-feed
  (:require
   [cljs-bean.core :as b]
   [taoensso.timbre :as timbre]
   [goog.object :as obj]))

(declare send!)

(defonce *ws (atom nil))

(defn coinbase-subscribe-msg []
  (js/JSON.stringify
    (clj->js
      {"product_ids" ["ETH-USD" "LTC-USD" "BTC-USD"]
       "type"        "subscribe"
       "channels"    [
                      "ticker"
                      ;"heartbeat"
                      ;{"product_ids" ["ETH-BTC" "ETH-USD"] "name" "ticker"}
                      ]})))

(defn on-open []
  (timbre/info "WebSocket open!")
  (send! @*ws (coinbase-subscribe-msg)))

(defn on-error [x]
  (timbre/info "WebSocket error:" x))

(defn ->on-message [callback-f]
  (fn [^js/Object ws-message]
    (callback-f ws-message)))

(defn on-close [x]
  (timbre/info "WebSocket closed!" x))

(defn send! [^js/Object ws-conn data]
  (.send ws-conn data))

(defn close! [^js/Object ws-conn]
  (.close ws-conn))

(defn new-web-socket!
  [{:keys [callback-f] :or {callback-f identity}}]
  (let [^js/WebSocket ws (try
                           (js/WebSocket. "wss://ws-feed.exchange.coinbase.com")
                           (catch js/Error e (do (timbre/info e) nil)))]
    (if (nil? ws)
      (timbre/warn "Could not get a websocket... That's annoying.")
      (doto ws
        (obj/set "onopen" on-open)
        (obj/set "onerror" on-error)
        (obj/set "onmessage" (->on-message callback-f))
        (obj/set "onclose" on-close)))
    (reset! *ws ws)
    ws))

(comment

  (new-web-socket! {:callback-f (fn [data] (timbre/info "data:::" data))})

  (send! @*ws (coinbase-subscribe-msg))

  (close! @*ws)

  )
