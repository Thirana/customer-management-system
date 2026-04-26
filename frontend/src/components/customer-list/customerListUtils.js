import {
  DEFAULT_PAGE,
  DEFAULT_SIZE,
  DEFAULT_SORT_BY,
  DEFAULT_SORT_DIR,
  PAGE_SIZE_OPTIONS,
  SORTABLE_COLUMNS,
} from './customerListConfig.js'

export function createEmptyPageState() {
  return {
    content: [],
    page: DEFAULT_PAGE,
    size: DEFAULT_SIZE,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  }
}

export function parsePositiveInteger(value, fallbackValue) {
  const parsed = Number(value)
  if (!Number.isInteger(parsed) || parsed < 0) {
    return fallbackValue
  }
  return parsed
}

export function parsePageSize(value) {
  const parsed = Number(value)
  if (!PAGE_SIZE_OPTIONS.includes(parsed)) {
    return DEFAULT_SIZE
  }
  return parsed
}

export function parseSortField(value) {
  return SORTABLE_COLUMNS[value] ? value : DEFAULT_SORT_BY
}

export function parseSortDirection(value) {
  return value === 'desc' ? 'desc' : DEFAULT_SORT_DIR
}

export function formatDate(value) {
  if (!value) {
    return '—'
  }

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('en-GB', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  }).format(date)
}
