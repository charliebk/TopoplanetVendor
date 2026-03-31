import { defineStore } from 'pinia'
import { buildApiUrl } from '@/client'
import type {
  CoreLogListFilters,
  CoreLogPayload,
  CoreLogResponse
} from './coreLog.types'

export const useCoreLogStore = defineStore('backend-core-log', () => {
  const endpoints = {
    list: buildApiUrl('/api/core-logs'),
    create: buildApiUrl('/api/core-log'),
    getById: (id: number) => buildApiUrl(`/api/core-log/${id}`),
    updateById: (id: number) => buildApiUrl(`/api/core-log/${id}`),
    deleteById: (id: number) => buildApiUrl(`/api/core-log/${id}`)
  }

  const buildListUrl = (filters?: CoreLogListFilters): string => {
    const url = new URL(endpoints.list)

    if (filters?.coreProjectId !== undefined) {
      url.searchParams.set('coreProjectId', String(filters.coreProjectId))
    }

    return url.toString()
  }

  const listCoreLogs = async (
    filters?: CoreLogListFilters
  ): Promise<CoreLogResponse[]> => {
    const response = await fetch(buildListUrl(filters))
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as CoreLogResponse[]
  }

  const getCoreLogById = async (id: number): Promise<CoreLogResponse> => {
    const response = await fetch(endpoints.getById(id))
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as CoreLogResponse
  }

  const createCoreLog = async (
    payload: CoreLogPayload
  ): Promise<CoreLogResponse> => {
    const response = await fetch(endpoints.create, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as CoreLogResponse
  }

  const updateCoreLogById = async (
    id: number,
    payload: CoreLogPayload
  ): Promise<CoreLogResponse> => {
    const response = await fetch(endpoints.updateById(id), {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    })

    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as CoreLogResponse
  }

  const deleteCoreLogById = async (id: number): Promise<void> => {
    const response = await fetch(endpoints.deleteById(id), {
      method: 'DELETE'
    })

    if (!response.ok && response.status !== 204) {
      throw new Error(`Backend API error: ${response.status}`)
    }
  }

  return {
    endpoints,
    listCoreLogs,
    getCoreLogById,
    createCoreLog,
    updateCoreLogById,
    deleteCoreLogById
  }
})
