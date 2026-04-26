export class ApiClientError extends Error {
  constructor(message, options = {}) {
    super(message)
    this.name = 'ApiClientError'
    this.status = options.status ?? null
    this.validationErrors = options.validationErrors ?? null
    this.raw = options.raw ?? null
  }
}

export function unwrapApiResponse(response) {
  const payload = response?.data

  if (!payload) {
    throw new ApiClientError('Backend response is empty.', { status: response?.status ?? null })
  }

  if (payload.success === false) {
    throw new ApiClientError(payload.message || 'Request failed.', {
      status: response?.status ?? null,
      validationErrors: isValidationMap(payload.data) ? payload.data : null,
      raw: payload,
    })
  }

  return payload.data
}

export function toApiClientError(error) {
  if (error instanceof ApiClientError) {
    return error
  }

  const response = error?.response
  const payload = response?.data
  const validationErrors = isValidationMap(payload?.data) ? payload.data : null
  const message =
    payload?.message ||
    error?.message ||
    'The request could not be completed.'

  return new ApiClientError(message, {
    status: response?.status ?? null,
    validationErrors,
    raw: payload ?? null,
  })
}

function isValidationMap(value) {
  return Boolean(value) && typeof value === 'object' && !Array.isArray(value)
}
