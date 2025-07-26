(ns com.saberstack.www.index
  (:require [cljs-bean.core :as b]
            [ss.expo.core :as expo]
            [ss.react-native.core :as r]
            [ss.react.core :as rc]
            [expo-font :refer [useFonts] :rename {useFonts use-fonts}]
            ["@expo-google-fonts/inter" :as font-inter
             :refer [Inter_900Black Inter_400Regular Inter_500Medium Inter_600SemiBold]]
            [taoensso.timbre :as timbre]))

;; Manages the application's top-level state.
;; Using `defonce` and atoms ensures that state is preserved
;; during development across hot-reloads of the code.
(defonce *bootloader-refresh-hook (atom nil))
(defonce *root-refresh-hook (atom nil))
(defonce *bootloader-state (atom {:render :index}))
(defonce *app-state
  (atom {:company-name "Saberstack"
         :one-liner
         "Rebuilding databases to answer the hardest questions in milliseconds.\nNo Snowflake required."
         }))

(def logo-transparent-background "https://github.com/saberstack/logo/blob/78264ed74f0a8b980dad30495a42d61b0de143f5/logo-transparent-background.png?raw=true")

;; The primary UI of the application.
;; It captures its own refresh hook to allow external processes
;; to trigger a re-render, a simple and effective state management technique.
(rc/defnrc root-component [{:keys [] :as _props}]
  (let [[_ root-refresh-hook] (rc/use-state (random-uuid))
        _         (reset! *root-refresh-hook root-refresh-hook)
        [fonts-loaded fonts-error] (use-fonts (b/->js {"Inter-Black"    Inter_900Black
                                                       "Inter-Regular"  Inter_400Regular
                                                       "Inter-Medium"   Inter_500Medium
                                                       "Inter-SemiBold" Inter_600SemiBold}))
        app-state @*app-state]
    ;(timbre/info "Rendering root component with app-state:" app-state)
    ;(timbre/info "Fonts loaded:" fonts-loaded "Error:" fonts-error)
    (r/scroll-view
      {:style                 {:flex 1 :backgroundColor "#08090a"}
       :contentContainerStyle {:marginLeft "auto" :marginRight "auto"}}
      (r/view {:style {:flex 1 :flexDirection "row"}}

        (r/view {:style {:flex             1
                         :flexDirection    "column"
                         :maxWidth         900
                         :justifyContent   "flex-start"
                         :marginVertical   "1%"
                         :marginHorizontal "5%"}}
          (r/image {:source {:uri    "./assets/saberstack-logo-t@2x.png"
                             :width  64
                             :height 64}})
          (r/view {:style {:marginVertical "1%"}}
            (r/view {:style {:marginTop "1"}}
              (r/text {:style
                       {:color    "#f7f8f8" :fontFamily "Inter-Medium" :letterSpacing "-0.038em"
                        :fontSize 40 :textAlign "left"
                        }}
                (:one-liner app-state)))
            (r/view {:style {:marginTop "3%"}}
              (r/text
                {:style
                 {:color      "#b5b6b6"
                  :fontFamily "Inter-Regular" :letterSpacing "-0.01em"
                  :fontSize   22 :textAlign "left"}}
                (str
                  "Data warehouse queries run for minutes, sync takes hours, and data is always stale. "
                  "Are we done? Is this it? At Saberstack, we don't think so.")))

            (r/view {:style {:marginTop "3%"}}
              (r/text {:style
                       {:color    "#f7f8f8" :fontFamily "Inter-Medium" :letterSpacing "-0.038em"
                        :fontSize 24 :textAlign "left"
                        }}
                (str
                  "A new incremental view maintenance engine for databases."
                  "\nPostgres, Datomic, and even Parquet files."
                  "\n"
                  "We are canceling the data swamp apocalypse.")))
            (r/view {:style {:marginTop "3%" :marginBottom "20%"}}
              (r/touchable-opacity
                {:onPress (fn [_] (r/open-url "https://github.com/saberstack/zsxf"))}
                (r/text {:style {:margin   "1%"
                                 :color    "#f7f8f8" :fontFamily "Inter-Medium"
                                 :fontSize 18 :textAlign "left"}}
                  "› github.com/saberstack/zsxf"))
              (r/touchable-opacity
                {:onPress (fn [_] (r/open-url "https://discord.gg/J4GWa4DBKu"))}
                (r/text {:style {:margin   "1%"
                                 :color    "#f7f8f8" :fontFamily "Inter-Medium"
                                 :fontSize 18 :textAlign "left"}}
                  "› discord: join here"))))))

      (comment
        "Every time a query runs, most databases compute it from scratch."
        "\nEven when the query changes very little. Even when the query is exactly the same."
        "\nAs the amount of data in a database grows, the time required to answer queries increases. Most databases timeout or crash completely."
        "\n\nThe need to answer analytics queries in some fashion gave rise to data warehouses."
        )
      )))
;; Creates a renderable React element from the root component.
(def root (rc/e root-component))

;; A wrapper function that renders the main application view.
(defn render-setup-index [& {:keys []}]
  (root {}))

;; This component acts as a bootloader or a simple router.
;; It consults the shared application state (`*bootloader-state`)
;; to decide which view to render, decoupling the rendering logic
;; from the application's entry point.
(rc/defnrc bootloader-component [_]
  (let [[_ refresh-hook] (rc/use-state (random-uuid))
        _ (reset! *bootloader-refresh-hook refresh-hook)
        {:keys [render]} @*bootloader-state]
    (condp = render
      :index (render-setup-index))))
;; Creates a renderable React element from the bootloader component.
(def bootloader (rc/e bootloader-component))

;; A higher-order function that creates a watch handler for an atom.
;; This handler, when attached to a state atom, triggers a UI refresh
;; by invoking a refresh hook stored in `*refresh-hook`. It's a key part
;; of the state management system, connecting global state changes
;; to component re-renders. The state change is ignored if the new state
;; is the same as the old state, preventing unnecessary updates.
(defn watch-refresh-hook [*refresh-hook]
  (fn [_watch-key _atom old-state new-state]
    ;;(timbre/debug "calling watch-refresh-hook")
    ;;(timbre/debug "old-state:" old-state)
    ;;(timbre/debug "new-state:" new-state)
    (if (= old-state new-state)
      true
      (when-let [refresh-hook @*refresh-hook]
        (refresh-hook (random-uuid))))))

(defn init-watches []
  ;(timbre/info "Adding watches to bootloader and root refresh hooks")
  (add-watch *bootloader-state :watch-1 (watch-refresh-hook *bootloader-refresh-hook))
  (add-watch *app-state :watch-1 (watch-refresh-hook *root-refresh-hook))
  )

;; Initializes the application by registering the top-level
;; component with Expo. This is the standard entry point.
(defn init []
  (init-watches)
  (expo/register-root-component
    (fn []
      (bootloader {}))))

;; Provides the entry point for Figwheel's hot-reloading bridge.
;; This function MUST be provided for the development environment to work correctly.
(defn figwheel-rn-root []
  ;(timbre/info "figwheel-rn-root called")
  (init-watches)
  (bootloader {}))

(defn -main [& args]
  ;(timbre/info "-main called with args:" args)
  ;(println "Hello RN web from CLJS")
  (init))

;; This form ensures the application is initialized for production builds,
;; where Figwheel is not present.
(when (expo/prod?)
  (init))
