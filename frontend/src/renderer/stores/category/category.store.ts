import { defineStore } from 'pinia'
import { buildApiUrl } from '@/client'
import type {
  CategoryListFilters,
  CategoryPayload,
  CategoryResponse
} from './category.types'

export const useCategoryStore = defineStore('backend-category', () => {
  const endpoints = {
    list: buildApiUrl('/api/categories'),
    create: buildApiUrl('/api/category'),
    getById: (id: number) => buildApiUrl(`/api/category/${id}`),
    updateById: (id: number) => buildApiUrl(`/api/category/${id}`),
    deleteById: (id: number) => buildApiUrl(`/api/category/${id}`)
  }

  const buildListUrl = (filters?: CategoryListFilters): string => {
    const url = new URL(endpoints.list)

    if (filters?.coreProjectId !== undefined) {
      url.searchParams.set('coreProjectId', String(filters.coreProjectId))
    }

    return url.toString()
  }

  const listCategories = async (
    filters?: CategoryListFilters
  ): Promise<CategoryResponse[]> => {
    const response = await fetch(buildListUrl(filters))
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as CategoryResponse[]
  }

  const getCategoryById = async (id: number): Promise<CategoryResponse> => {
    const response = await fetch(endpoints.getById(id))
    if (!response.ok) {
      throw new Error(`Backend API error: ${response.status}`)
    }

    return (await response.json()) as CategoryResponse
  }

  const createCategory = async (
    payload: CategoryPayload
  ): Promise<CategoryResponse> => {
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

    return (await response.json()) as CategoryResponse
  }

  const updateCategoryById = async (
    id: number,
    payload: CategoryPayload
  ): Promise<CategoryResponse> => {
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

    return (await response.json()) as CategoryResponse
  }

  const deleteCategoryById = async (id: number): Promise<void> => {
    const response = await fetch(endpoints.deleteById(id), {
      method: 'DELETE'
    })

    if (!response.ok && response.status !== 204) {
      throw new Error(`Backend API error: ${response.status}`)
    }
  }

  return {
    endpoints,
    listCategories,
    getCategoryById,
    createCategory,
    updateCategoryById,
    deleteCategoryById
  }
})
