import axios from 'axios'
import { toApiClientError, unwrapApiResponse } from './api-response.js'
import { createRequestId } from './requestId.js'

const DEFAULT_API_BASE_URL = 'http://localhost:8080/api/v1'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || DEFAULT_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    Accept: 'application/json',
  },
})

// Attach a request id to every browser call so backend request logs stay traceable during manual review.
apiClient.interceptors.request.use((config) => {
  const nextConfig = { ...config }
  nextConfig.headers = {
    ...config.headers,
    'X-Request-ID': createRequestId(),
  }
  return nextConfig
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(toApiClientError(error)),
)

// Keep ApiResponse<T> unwrapping at the client boundary so feature code only deals with business data.
export async function performApiRequest(requestConfig) {
  try {
    const response = await apiClient(requestConfig)
    return unwrapApiResponse(response)
  } catch (error) {
    throw toApiClientError(error)
  }
}
