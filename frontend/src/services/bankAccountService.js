import axios from 'axios'
import apiClient from '../config/axios.js'

function getRequestError(error, action) {
  if (!axios.isAxiosError(error)) {
    return error instanceof Error ? error : new Error(`Unable to ${action}.`)
  }

  const responseData = error.response?.data
  const serverMessage =
    typeof responseData === 'string'
      ? responseData
      : responseData && typeof responseData === 'object'
        ? responseData.message || responseData.error || responseData.detail
        : null

  return new Error(
    typeof serverMessage === 'string' && serverMessage
      ? serverMessage
      : error.response
        ? `Unable to ${action} (${error.response.status}). Please try again.`
        : error.message,
    { cause: error },
  )
}

async function request(action, callback) {
  try {
    return await callback()
  } catch (error) {
    throw getRequestError(error, action)
  }
}

export function getBankAccounts() {
  return request('load bank accounts', async () => {
    const response = await apiClient.get('/api/v1/bankaccount')
    return response.data
  })
}

export function getBankAccountBalance(bankAccountNumber) {
  return request('refresh balance', async () => {
    const response = await apiClient.get(
      `/api/v1/bankaccount/${encodeURIComponent(bankAccountNumber)}/balance`,
    )
    return response.data
  })
}

export function createBankAccount({ bankName, currency }) {
  return request('create bank account', async () => {
    const response = await apiClient.post('/api/v1/bankaccount', { bankName, currency })
    return response.data
  })
}

export function depositToBankAccount(bankAccountNumber, amount) {
  return request('add funds', async () => {
    const response = await apiClient.post(
      `/api/v1/bankaccount/${encodeURIComponent(bankAccountNumber)}/deposit`,
      { amount: Number(amount) },
    )
    return response.data
  })
}

export function withdrawFromBankAccount(bankAccountNumber, amount) {
  return request('withdraw funds', async () => {
    const response = await apiClient.post(
      `/api/v1/bankaccount/${encodeURIComponent(bankAccountNumber)}/withdraw`,
      { amount: Number(amount) },
    )
    return response.data
  })
}
