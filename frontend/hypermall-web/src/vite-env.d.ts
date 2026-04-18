/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_WS_URL: string
  readonly VITE_ENABLE_AI_CHAT?: string
  readonly VITE_AI_CHAT_STORAGE_KEY?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
