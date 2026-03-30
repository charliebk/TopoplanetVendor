import { defineStore } from 'pinia'
import { buildApiUrl } from '@/renderer/stores/client'

export interface AppMessageResponse {
  key: string
  value: string
}

export const useAppMessageStore = defineStore('backend-app-message', () => {
  const endpoints = {
    helloWorld: buildApiUrl('/api/app-message/hello-world')
  }

  const fetchHelloWorldMessage = async (): Promise<AppMessageResponse> => {
    const response = await fetch(endpoints.helloWorld)

    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as AppMessageResponse
  }

  return {
    endpoints,
    fetchHelloWorldMessage
  }
})
