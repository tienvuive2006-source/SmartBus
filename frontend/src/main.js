import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import vue3GoogleLogin from 'vue3-google-login'

// Clear old blurry sample images from localStorage automatically for the user
localStorage.removeItem('smartbus_route_images');

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(vue3GoogleLogin, {
  clientId: '364596436412-glfi5lc4m0r787ds442edu1nnr6ioa87.apps.googleusercontent.com'
})

app.mount('#app')
