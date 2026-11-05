import axios from 'axios'
import apiClient from '../config/axios.js'

async function postAuthRequest(endpoint, payload) {
  try {
    const response = await apiClient.post(`/api/v1/auth/${endpoint}`, payload)
    return response.data
  } catch (error) {
    throw getRequestError(error)
  }
}

export async function getCurrentUser() {
  try {
    const response = await apiClient.get('/api/v1/auth/me')
    return response.data
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      return null
    }

    throw getRequestError(error)
  }
}

function getRequestError(error) {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error ? error : new Error('An unexpected error occurred.')
  }

  const responseData = error.response?.data
  const serverMessage =
    typeof responseData === 'string'
      ? responseData
      : responseData && typeof responseData === 'object'
        ? responseData.message || responseData.error
        : null

  return new Error(
    typeof serverMessage === 'string' && serverMessage
      ? serverMessage
      : error.response
        ? `Request failed (${error.response.status}). Please try again.`
        : error.message,
    { cause: error },
  )
}

export function signup({ name, email, password }) {
  return postAuthRequest('signup', { name, email, password })
}

export function login({ email, password }) {
  return postAuthRequest('login', { email, password })
}

export function logout() {
  return postAuthRequest('logout')
}
