import { defineStore } from 'pinia'
import { buildApiUrl } from '@/client'
import type {
  CoreProjectPayload,
  CoreProjectResponse
} from './coreProject.types'

export const useCoreProjectStore = defineStore('backend-core-project', () => {
  const endpoints = {
    list: buildApiUrl('/api/core-projects'),
    create: buildApiUrl('/api/core-project'),
    getById: (id: number) => buildApiUrl(`/api/core-project/${id}`),
    updateById: (id: number) => buildApiUrl(`/api/core-project/${id}`),
    deleteById: (id: number) => buildApiUrl(`/api/core-project/${id}`)
  }

  const listCoreProjects = async (): Promise<CoreProjectResponse[]> => {
    const response = await fetch(endpoints.list)
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }
    return (await response.json()) as CoreProjectResponse[]
  }

  const getCoreProjectById = async (
    id: number
  ): Promise<CoreProjectResponse> => {
    const response = await fetch(endpoints.getById(id))
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }
    return (await response.json()) as CoreProjectResponse
  }

  const createCoreProject = async (
    payload: CoreProjectPayload
  ): Promise<CoreProjectResponse> => {
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

    return (await response.json()) as CoreProjectResponse
  }

  const updateCoreProjectById = async (
    id: number,
    payload: CoreProjectPayload
  ): Promise<CoreProjectResponse> => {
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

    return (await response.json()) as CoreProjectResponse
  }

  const deleteCoreProjectById = async (id: number): Promise<void> => {
    const response = await fetch(endpoints.deleteById(id), {
      method: 'DELETE'
    })

    if (!response.ok && response.status !== 204) {
      throw new Error(`Backend API error: ${response.status}`)
    }
  }

  return {
    endpoints,
    listCoreProjects,
    getCoreProjectById,
    createCoreProject,
    updateCoreProjectById,
    deleteCoreProjectById
  }
})
