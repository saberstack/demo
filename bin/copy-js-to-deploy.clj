#!/usr/bin/env bb

(require '[babashka.fs :as fs])

(let [cljs-compiled-output-path (first (mapv str (fs/glob "/Users/raspasov/projects/saberstack/demo/dist/_expo/static/js/web" "**{.js}")))
      new-compiled-js-code      (slurp cljs-compiled-output-path)
      template-index-html-path  "/Users/raspasov/projects/saberstack/saberstack.github.io/_template_index.html"
      index-html-code           (slurp template-index-html-path)
      new-index-html-code'      (clojure.string/replace-first index-html-code
                                  "<script></script>"
                                  (clojure.string/join
                                    "\n"
                                    ["<script>"
                                     new-compiled-js-code
                                     "</script>"]))]
  (spit
  "/Users/raspasov/projects/saberstack/saberstack.github.io/index.html"
    new-index-html-code'))
