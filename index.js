import {npmDeps} from "./target/public/cljs-out/com.saberstack.live-dev_rn/npm_deps.js";
import {assets} from "./target/public/cljs-out/com.saberstack.live-dev/krell_assets.js";
import {krellNpmDeps} from "./target/public/cljs-out/com.saberstack.live-dev/krell_npm_deps.js";
var options = {optionsUrl: "http://192.168.86.20:9500/cljs-out/com.saberstack.live-dev/cljsc_opts.json",
               autoRefresh: true};
var figBridge = require("./target/public/cljs-out/com.saberstack.live-dev_rn/figwheel-bridge.js");
figBridge.shimRequire({...assets, ...krellNpmDeps, ...npmDeps});
import { registerRootComponent } from 'expo';
registerRootComponent(figBridge.createBridgeComponent(options));