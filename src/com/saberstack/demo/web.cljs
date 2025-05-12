(ns com.saberstack.demo.web
  (:require [org.zsxf.query :as q]
            [react]
            [react-native :as rn]
            [ss.expo.core :as expo]
            [org.zsxf.datalog.compiler :as zsxf.c]
            [org.zsxf.datascript :as ds]
            [datascript.core :as d]
            [ss.react-native.core :as r]))

(defn init-conn []
  (let [conn      (d/create-conn {})
        query     (q/create-query
                    (zsxf.c/static-compile
                      '[:find ?person-name
                        :where
                        [?e :person/name ?person-name]
                        [?e :person/name ?person-name]]))
        _         (ds/init-query-with-conn query conn)
        _         (d/transact! conn [{:person/name "Alice"}])
        _         (d/transact! conn [{:person/else "Bob"}])
        result-ds (d/q
                    '[:find ?person-name
                      :where
                      [?e :person/name ?person-name]
                      [?e :person/name ?person-name]]
                    @conn)]

    {:ivm (q/get-result query)
     :ds result-ds}

    ))

(defn build-root-view []
  (let []
    (r/view {}
      (r/text {}
        (str
          (init-conn))))))

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

(defn init-compile []
  (zsxf.c/static-compile
    '[:find ?currency
      :where
      [?te :side "buy"]
      [?te :product_id ?currency]])
  )
