import {npmDeps} from "./target/public/cljs-out/web_rn/npm_deps.js";
import {assets} from "./target/public/cljs-out/web/krell_assets.js";
import {krellNpmDeps} from "./target/public/cljs-out/web/krell_npm_deps.js";
var options = {optionsUrl: "http://localhost:9500/cljs-out/web/cljsc_opts.json",
               autoRefresh: true};
var figBridge = require("./target/public/cljs-out/web_rn/figwheel-bridge.js");
figBridge.shimRequire({...assets, ...krellNpmDeps, ...npmDeps});
import { registerRootComponent } from 'expo';
registerRootComponent(figBridge.createBridgeComponent(options));