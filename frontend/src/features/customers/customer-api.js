import { performApiRequest } from '../../lib/api.js'

export function getCustomers(params = {}) {
  return performApiRequest({
    method: 'get',
    url: '/customers',
    params,
  })
}

// Family-member lookup reuses the normal list contract so large datasets stay paged and searchable.
export function searchCustomerSummaries(search, params = {}) {
  return getCustomers({
    page: 0,
    size: 8,
    sortBy: 'name',
    sortDir: 'asc',
    ...params,
    search,
  })
}

export function getCustomer(id) {
  return performApiRequest({
    method: 'get',
    url: `/customers/${id}`,
  })
}

export function createCustomer(payload) {
  return performApiRequest({
    method: 'post',
    url: '/customers',
    data: payload,
  })
}

export function updateCustomer(id, payload) {
  return performApiRequest({
    method: 'put',
    url: `/customers/${id}`,
    data: payload,
  })
}

export function deleteCustomer(id) {
  return performApiRequest({
    method: 'delete',
    url: `/customers/${id}`,
  })
}

export function getCities() {
  return performApiRequest({
    method: 'get',
    url: '/cities',
  })
}

export function uploadCustomerImport(file) {
  const formData = new FormData()
  formData.append('file', file)

  // Multipart upload is the one place where the customer API surface does not use JSON bodies.
  return performApiRequest({
    method: 'post',
    url: '/customers/import',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function getImportStatus(jobId) {
  return performApiRequest({
    method: 'get',
    url: `/customers/import/${jobId}/status`,
  })
}
