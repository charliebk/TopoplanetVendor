import { defineStore } from 'pinia'
import { buildApiUrl } from '@/client'
import type {
  RequirementLevelListFilters,
  RequirementLevelPayload,
  RequirementLevelResponse
} from './requirementLevel.types'

export const useRequirementLevelStore = defineStore(
  'backend-requirement-level',
  () => {
    const endpoints = {
      list: buildApiUrl('/api/requirement-levels'),
      create: buildApiUrl('/api/requirement-level'),
      getById: (id: number) => buildApiUrl(`/api/requirement-level/${id}`),
      updateById: (id: number) => buildApiUrl(`/api/requirement-level/${id}`),
      deleteById: (id: number) => buildApiUrl(`/api/requirement-level/${id}`)
    }

    const buildListUrl = (filters?: RequirementLevelListFilters): string => {
      const url = new URL(endpoints.list)

      if (filters?.coreProjectId !== undefined) {
        url.searchParams.set('coreProjectId', String(filters.coreProjectId))
      }

      return url.toString()
    }

    const listRequirementLevels = async (
      filters?: RequirementLevelListFilters
    ): Promise<RequirementLevelResponse[]> => {
      const response = await fetch(buildListUrl(filters))
      if (!response.ok) {
        throw new Error(`Backend API error: ${response.status}`)
      }

      return (await response.json()) as RequirementLevelResponse[]
    }

    const getRequirementLevelById = async (
      id: number
    ): Promise<RequirementLevelResponse> => {
      const response = await fetch(endpoints.getById(id))
      if (!response.ok) {
        throw new Error(`Backend API error: ${response.status}`)
      }

      return (await response.json()) as RequirementLevelResponse
    }

    const createRequirementLevel = async (
      payload: RequirementLevelPayload
    ): Promise<RequirementLevelResponse> => {
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

      return (await response.json()) as RequirementLevelResponse
    }

    const updateRequirementLevelById = async (
      id: number,
      payload: RequirementLevelPayload
    ): Promise<RequirementLevelResponse> => {
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

      return (await response.json()) as RequirementLevelResponse
    }

    const deleteRequirementLevelById = async (id: number): Promise<void> => {
      const response = await fetch(endpoints.deleteById(id), {
        method: 'DELETE'
      })

      if (!response.ok && response.status !== 204) {
        throw new Error(`Backend API error: ${response.status}`)
      }
    }

    return {
      endpoints,
      listRequirementLevels,
      getRequirementLevelById,
      createRequirementLevel,
      updateRequirementLevelById,
      deleteRequirementLevelById
    }
  }
)
