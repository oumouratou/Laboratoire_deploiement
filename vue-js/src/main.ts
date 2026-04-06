import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// Créer et installer Pinia
const pinia = createPinia()
app.use(pinia)

// Installer le router
app.use(router)

app.mount('#app')
