import { defineStore } from 'pinia'
import { buildApiUrl } from '@/renderer/stores/client'

export interface ProjectPayload {
  code: string
  name: string
  description?: string | null
}

export interface ProjectResponse {
  id: number
  code: string
  name: string
  description: string | null
  deleted: boolean
  createdAt: string
  updatedAt: string
}

export const useProjectStore = defineStore('backend-project', () => {
  const endpoints = {
    list: buildApiUrl('/api/projects'),
    create: buildApiUrl('/api/project'),
    getById: (id: number) => buildApiUrl(`/api/project/${id}`),
    updateById: (id: number) => buildApiUrl(`/api/project/${id}`),
    deleteById: (id: number) => buildApiUrl(`/api/project/${id}`)
  }

  const listProjects = async (): Promise<ProjectResponse[]> => {
    const response = await fetch(endpoints.list)
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }
    return (await response.json()) as ProjectResponse[]
  }

  const getProjectById = async (id: number): Promise<ProjectResponse> => {
    const response = await fetch(endpoints.getById(id))
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }
    return (await response.json()) as ProjectResponse
  }

  const createProject = async (
    payload: ProjectPayload
  ): Promise<ProjectResponse> => {
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

    return (await response.json()) as ProjectResponse
  }

  const updateProjectById = async (
    id: number,
    payload: ProjectPayload
  ): Promise<ProjectResponse> => {
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

    return (await response.json()) as ProjectResponse
  }

  const deleteProjectById = async (id: number): Promise<void> => {
    const response = await fetch(endpoints.deleteById(id), {
      method: 'DELETE'
    })

    if (!response.ok && response.status !== 204) {
      throw new Error(`Backend API error: ${response.status}`)
    }
  }

  return {
    endpoints,
    listProjects,
    getProjectById,
    createProject,
    updateProjectById,
    deleteProjectById
  }
})
