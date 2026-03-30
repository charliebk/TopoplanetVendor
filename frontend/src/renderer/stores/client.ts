export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:7007'

export const buildApiUrl = (path: string): string => `${API_BASE_URL}${path}`
