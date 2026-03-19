<template>
  <div class="publish-container">

    <div class="meta-section">
      <el-input
          v-model="articleForm.title"
          placeholder="请输入文章标题..."
          class="title-input"
          size="large"
      />
      <el-select
          v-model="articleForm.categoryId"
          placeholder="选择分类"
          size="large"
          class="category-select"
      >
        <el-option
            v-for="item in categories"
            :key="item.id"
            :label="item.name"
            :value="item.id"
        />
      </el-select>

      <el-button
          type="primary"
          color="#6c5ce7"
          size="large"
          @click="submitArticle"
          :loading="publishing"
          class="publish-btn"
      >
        🚀 立即发布
      </el-button>
    </div>

    <div class="editor-wrapper">
      <MdEditor
          v-model="articleForm.content"
          class="my-editor"
          placeholder="开始你的奇妙幻想创作..."
          @onUploadImg="handleUploadImage"
          :theme="isDark ? 'dark' : 'light'"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { MdEditor } from 'md-editor-v3'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useRouter } from 'vue-router'

const router = useRouter()
const publishing = ref(false)
const categories = ref([])

// 读取系统当前的暗黑模式状态，让编辑器也能适配暗黑主题
const isDark = ref(document.documentElement.classList.contains('dark'))

// 表单数据
const articleForm = reactive({
  title: '',
  categoryId: null,
  content: ''
})

// 初始化获取分类列表
const fetchCategories = async () => {
  try {
    categories.value = await request.get('/categories')
  } catch (error) {}
}

onMounted(() => {
  fetchCategories()
})

// Markdown 图片上传对接阿里云 OSS
const handleUploadImage = async (files, callback) => {
  try {
    const resUrls = await Promise.all(
        files.map((file) => {
          return new Promise((resolve, reject) => {
            const formData = new FormData()
            formData.append('file', file)

            request.post('/upload', formData, {
              headers: { 'Content-Type': 'multipart/form-data' }
            }).then((url) => {
              resolve(url)
            }).catch((err) => reject(err))
          })
        })
    )
    callback(resUrls)
    ElMessage.success('图片上传云端成功！')
  } catch (error) {
    ElMessage.error('图片上传失败！')
  }
}

// 提交文章
const submitArticle = async () => {
  if (!articleForm.title || !articleForm.categoryId || !articleForm.content) {
    return ElMessage.warning('标题、分类和内容都不能为空哦！')
  }

  publishing.value = true
  try {
    await request.post('/articles', articleForm)
    ElMessage.success('🎉 文章发布成功！')

    // 发布成功后，清空表单并跳转到文章管理页
    articleForm.title = ''
    articleForm.content = ''
    router.push('/admin/articles')
  } catch (error) {
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.publish-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px); /* 减去 header 和 padding 的高度，让编辑器撑满屏幕 */
}

.meta-section {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

/* 覆盖 Element UI 的生硬边框，追求高级感 */
:deep(.el-input__wrapper) {
  box-shadow: 0 2px 10px rgba(0,0,0,0.02) !important;
  border-radius: 8px;
  background: var(--card-bg, #fff);
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #6c5ce7 inset !important;
}

.title-input { flex: 4; }
.category-select { flex: 1; min-width: 150px; }
.publish-btn { border-radius: 8px; font-weight: bold; padding: 0 25px; }

.editor-wrapper {
  flex: 1;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0,0,0,0.03);
  border: 1px solid var(--border-color, #f1f2f6);
}

.my-editor {
  height: 100%;
}

/* 暗黑模式下输入框文字颜色调整 */
:global(html.dark .el-input__inner) { color: #c9d1d9; }
</style>