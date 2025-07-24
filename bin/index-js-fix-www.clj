#!/usr/bin/env bb

(let [code  (slurp "index.js")
      code' (clojure.string/replace code "module.exports = { renderFn: com.saberstack.www.index.figwheel_rn_root };" "")]
  (spit "index.js" code'))
