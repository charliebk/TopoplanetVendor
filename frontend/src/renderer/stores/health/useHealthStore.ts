import { defineStore } from 'pinia'
import { buildApiUrl } from '@/renderer/stores/client'

export interface HealthStatusResponse {
  status: string
  service: string
  database: string
}

export const useHealthStore = defineStore('backend-health', () => {
  const endpoints = {
    health: buildApiUrl('/health')
  }

  const fetchHealthStatus = async (): Promise<HealthStatusResponse> => {
    const response = await fetch(endpoints.health)

    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as HealthStatusResponse
  }

  return {
    endpoints,
    fetchHealthStatus
  }
})
