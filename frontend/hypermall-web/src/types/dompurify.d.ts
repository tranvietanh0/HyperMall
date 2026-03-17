declare module 'dompurify' {
  type Config = {
    ALLOWED_TAGS?: string[]
    ALLOWED_ATTR?: string[]
    ALLOW_DATA_ATTR?: boolean
  }

  type DOMPurifyLike = {
    sanitize: (dirty: string, config?: Config) => string
  }

  const DOMPurify: DOMPurifyLike

  export default DOMPurify
}
