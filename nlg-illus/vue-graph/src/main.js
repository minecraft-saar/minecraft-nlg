// main.ts
import { createApp } from "vue"
import VNetworkGraph from "v-network-graph"
import "v-network-graph/lib/style.css"
import App from "./App.vue"
import { Quasar } from 'quasar'
import quasarUserOptions from './quasar-user-options'

const app = createApp(App).use(Quasar, quasarUserOptions)

app.use(VNetworkGraph)
app.mount("#app")