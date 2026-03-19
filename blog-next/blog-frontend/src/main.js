import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

// 引入 Element Plus 及全局样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 引入 Markdown 编辑器全局样式
import 'md-editor-v3/lib/style.css'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')