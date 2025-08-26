(ns com.saberstack.live.font.core
  (:require [cljs-bean.core :as b]
            ["@expo-google-fonts/inter" :as font-inter
             :refer [Inter_900Black Inter_400Regular Inter_500Medium Inter_600SemiBold]]))

(defn inter []
  (b/->js
    {"Inter-Black"    Inter_900Black
     "Inter-Regular"  Inter_400Regular
     "Inter-Medium"   Inter_500Medium
     "Inter-SemiBold" Inter_600SemiBold}))
