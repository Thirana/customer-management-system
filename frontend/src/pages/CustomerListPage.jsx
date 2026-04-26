import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { Alert } from '../components/ui/Alert.jsx'
import { Button } from '../components/ui/Button.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'
import { LoadingState } from '../components/ui/LoadingState.jsx'
import { PageSection } from '../components/ui/PageSection.jsx'
import { PaginationControls } from '../components/ui/PaginationControls.jsx'
import { SelectMenu } from '../components/ui/SelectMenu.jsx'
import { StatusBadge } from '../components/ui/StatusBadge.jsx'
import { deleteCustomer, getCustomers } from '../features/customers/customer-api.js'

const DEFAULT_PAGE = 0
const DEFAULT_SIZE = 10
const DEFAULT_SORT_BY = 'name'
const DEFAULT_SORT_DIR = 'asc'
const PAGE_SIZE_OPTIONS = [10, 20, 50]
const SORTABLE_COLUMNS = {
  name: 'Name',
  nicNumber: 'NIC',
  dateOfBirth: 'Date of Birth',
}

export function CustomerListPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [pageState, setPageState] = useState(createEmptyPageState())
  const [status, setStatus] = useState('loading')
  const [errorMessage, setErrorMessage] = useState('')
  const [pendingDeleteId, setPendingDeleteId] = useState(null)

  const queryState = useMemo(
    () => ({
      page: parsePositiveInteger(searchParams.get('page'), DEFAULT_PAGE),
      size: parsePageSize(searchParams.get('size')),
      sortBy: parseSortField(searchParams.get('sortBy')),
      sortDir: parseSortDirection(searchParams.get('sortDir')),
    }),
    [searchParams],
  )

  useEffect(() => {
    let cancelled = false

    async function loadCustomers() {
      setStatus('loading')
      setErrorMessage('')

      try {
        const result = await getCustomers(queryState)
        if (cancelled) {
          return
        }

        setPageState(result)
        setStatus(result.content.length === 0 ? 'empty' : 'success')
      } catch (error) {
        if (cancelled) {
          return
        }

        setPageState(createEmptyPageState())
        setStatus('error')
        setErrorMessage(error.message || 'Customers could not be loaded.')
      }
    }

    loadCustomers()

    return () => {
      cancelled = true
    }
  }, [queryState])

  async function handleDelete(customerId, customerName) {
    const confirmed = window.confirm(
      `Delete customer "${customerName}"? This removes the customer and related contact records.`,
    )

    if (!confirmed) {
      return
    }

    setPendingDeleteId(customerId)
    setErrorMessage('')

    try {
      await deleteCustomer(customerId)

      const nextPage =
        pageState.content.length === 1 && queryState.page > 0
          ? queryState.page - 1
          : queryState.page

      updateQueryState({ page: nextPage })
    } catch (error) {
      setStatus(pageState.content.length === 0 ? 'empty' : 'success')
      setErrorMessage(error.message || 'Customer could not be deleted.')
    } finally {
      setPendingDeleteId(null)
    }
  }

  function handlePageSizeChange(nextSize) {
    updateQueryState({
      page: DEFAULT_PAGE,
      size: Number(nextSize),
    })
  }

  function handleSortChange(field) {
    if (!SORTABLE_COLUMNS[field]) {
      return
    }

    const nextSortDir =
      queryState.sortBy === field && queryState.sortDir === 'asc' ? 'desc' : 'asc'

    updateQueryState({
      page: DEFAULT_PAGE,
      sortBy: field,
      sortDir: nextSortDir,
    })
  }

  function handleSortFieldChange(nextValue) {
    const nextField = parseSortField(nextValue)

    updateQueryState({
      page: DEFAULT_PAGE,
      sortBy: nextField,
    })
  }

  function handleSortDirectionChange(nextValue) {
    const nextDirection = parseSortDirection(nextValue)

    updateQueryState({
      page: DEFAULT_PAGE,
      sortDir: nextDirection,
    })
  }

  function updateQueryState(partialState) {
    const nextState = {
      ...queryState,
      ...partialState,
    }

    setSearchParams({
      page: String(nextState.page),
      size: String(nextState.size),
      sortBy: nextState.sortBy,
      sortDir: nextState.sortDir,
    })
  }

  return (
    <div className="space-y-6">
      <PageSection
        actions={
          <>
            <Button as={Link} size="sm" tone="secondary" to="/customers/import">
              Import workbook
            </Button>
            <Button as={Link} size="sm" tone="secondary" to="/customers/new">
              Create customer
            </Button>
          </>
        }
        description="Browse customer records from the backend, adjust list controls in place, and move directly into create, detail, edit, and import workflows."
        eyebrow="Customer registry"
        title="Review and manage customer records"
      >
        <div className="flex flex-wrap gap-3">
          <StatusBadge tone="info">
            {pageState.totalElements} {pageState.totalElements === 1 ? 'customer' : 'customers'}
          </StatusBadge>
          <StatusBadge tone="neutral">
            Page {pageState.totalPages === 0 ? 0 : pageState.page + 1} of {pageState.totalPages}
          </StatusBadge>
          <StatusBadge tone="neutral">
            Sorted by {SORTABLE_COLUMNS[queryState.sortBy]} {queryState.sortDir.toUpperCase()}
          </StatusBadge>
        </div>
      </PageSection>

      <div className="grid gap-4 lg:grid-cols-2">
        <section className="rounded-[24px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-5 shadow-[var(--shadow-soft)]">
          <p className="text-sm font-semibold text-[var(--color-ink)]">List density</p>
          <p className="mt-2 text-sm leading-6 text-[var(--color-ink-muted)]">
            Tune the page size without leaving the list view.
          </p>
          <label className="mt-4 block">
            <span className="mb-2 block text-sm font-medium text-[var(--color-ink-soft)]">Rows</span>
            <SelectMenu
              options={PAGE_SIZE_OPTIONS.map((option) => ({
                value: option,
                label: String(option),
              }))}
              placeholder="Select rows"
              value={queryState.size}
              onChange={handlePageSizeChange}
            />
          </label>
        </section>

        <section className="rounded-[24px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-5 shadow-[var(--shadow-soft)]">
          <p className="text-sm font-semibold text-[var(--color-ink)]">Sort customers</p>
          <p className="mt-2 text-sm leading-6 text-[var(--color-ink-muted)]">
            Change the backend sort field and direction from this control panel.
          </p>

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <label className="block">
              <span className="mb-2 block text-sm font-medium text-[var(--color-ink-soft)]">
                Sort field
              </span>
              <SelectMenu
                options={Object.entries(SORTABLE_COLUMNS).map(([field, label]) => ({
                  value: field,
                  label,
                }))}
                placeholder="Select sort field"
                value={queryState.sortBy}
                onChange={handleSortFieldChange}
              />
            </label>

            <label className="block">
              <span className="mb-2 block text-sm font-medium text-[var(--color-ink-soft)]">
                Direction
              </span>
              <SelectMenu
                options={[
                  { value: 'asc', label: 'Ascending' },
                  { value: 'desc', label: 'Descending' },
                ]}
                placeholder="Select direction"
                value={queryState.sortDir}
                onChange={handleSortDirectionChange}
              />
            </label>
          </div>
        </section>
      </div>

      {errorMessage ? <Alert title="Request failed" message={errorMessage} tone="error" /> : null}

      {status === 'loading' ? <LoadingState label="Loading customers..." /> : null}

      {status === 'empty' ? (
        <EmptyState
          title="No customers found"
          message="Create a customer or import a workbook to populate the registry."
        />
      ) : null}

      {status === 'success' ? (
        <PageSection
          description="The table stays aligned to the backend pagination and sort contract. Actions remain available on each row for the next workflow step."
          title="Customer list"
        >
          <div className="overflow-hidden rounded-[24px] border border-[var(--color-border)]">
            <div className="overflow-x-auto">
              <table className="min-w-full border-collapse bg-[var(--color-surface)]">
                <thead>
                  <tr className="bg-[var(--color-surface-muted)] text-left">
                    <SortableHeader
                      activeField={queryState.sortBy}
                      direction={queryState.sortDir}
                      field="name"
                      label="Name"
                      onSort={handleSortChange}
                    />
                    <SortableHeader
                      activeField={queryState.sortBy}
                      direction={queryState.sortDir}
                      field="nicNumber"
                      label="NIC"
                      onSort={handleSortChange}
                    />
                    <SortableHeader
                      activeField={queryState.sortBy}
                      direction={queryState.sortDir}
                      field="dateOfBirth"
                      label="Date of Birth"
                      onSort={handleSortChange}
                    />
                    <th className="px-5 py-4 text-sm font-semibold text-[var(--color-ink-soft)]">
                      Mobile Count
                    </th>
                    <th className="px-5 py-4 text-sm font-semibold text-[var(--color-ink-soft)]">
                      Address Count
                    </th>
                    <th className="px-5 py-4 text-sm font-semibold text-[var(--color-ink-soft)]">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {pageState.content.map((customer) => (
                    <tr
                      key={customer.id}
                      className="border-t border-[var(--color-border)] text-sm text-[var(--color-ink-soft)]"
                    >
                      <td className="px-5 py-4">
                        <div className="font-medium text-[var(--color-ink)]">{customer.name}</div>
                      </td>
                      <td className="px-5 py-4">{customer.nicNumber}</td>
                      <td className="px-5 py-4">{formatDate(customer.dateOfBirth)}</td>
                      <td className="px-5 py-4">{customer.mobileNumberCount}</td>
                      <td className="px-5 py-4">{customer.addressCount}</td>
                      <td className="px-5 py-4">
                        <div className="flex flex-wrap gap-2">
                          <Button as={Link} size="sm" tone="ghost" to={`/customers/${customer.id}`}>
                            View
                          </Button>
                          <Button
                            as={Link}
                            size="sm"
                            tone="ghost"
                            to={`/customers/${customer.id}/edit`}
                          >
                            Edit
                          </Button>
                          <Button
                            size="sm"
                            tone="destructive"
                            disabled={pendingDeleteId === customer.id}
                            onClick={() => handleDelete(customer.id, customer.name)}
                          >
                            {pendingDeleteId === customer.id ? 'Deleting...' : 'Delete'}
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="mt-5">
            <PaginationControls
              currentPage={pageState.totalPages === 0 ? 0 : pageState.page + 1}
              hasNextPage={!pageState.last && pageState.totalPages > 0}
              hasPreviousPage={!pageState.first}
              onNext={() =>
                updateQueryState({
                  page: Math.min(queryState.page + 1, Math.max(pageState.totalPages - 1, 0)),
                })
              }
              onPrevious={() => updateQueryState({ page: Math.max(queryState.page - 1, 0) })}
              totalPages={pageState.totalPages}
            />
          </div>
        </PageSection>
      ) : null}
    </div>
  )
}

function SortableHeader({ activeField, direction, field, label, onSort }) {
  const isActive = activeField === field

  return (
    <th className="px-5 py-4">
      <button
        type="button"
        className="inline-flex items-center gap-2 rounded-full bg-transparent px-0 text-sm font-semibold text-[var(--color-ink-soft)] transition-colors hover:text-[var(--color-ink)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--color-primary)] focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--color-surface-muted)]"
        onClick={() => onSort(field)}
      >
        <span>{label}</span>
        <span
          className={`text-xs ${isActive ? 'text-[var(--color-primary)]' : 'text-[var(--color-ink-muted)]'}`}
          aria-hidden="true"
        >
          {isActive ? (direction === 'asc' ? '▲' : '▼') : '↕'}
        </span>
      </button>
    </th>
  )
}

function createEmptyPageState() {
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

function parsePositiveInteger(value, fallbackValue) {
  const parsed = Number(value)
  if (!Number.isInteger(parsed) || parsed < 0) {
    return fallbackValue
  }
  return parsed
}

function parsePageSize(value) {
  const parsed = Number(value)
  if (!PAGE_SIZE_OPTIONS.includes(parsed)) {
    return DEFAULT_SIZE
  }
  return parsed
}

function parseSortField(value) {
  return SORTABLE_COLUMNS[value] ? value : DEFAULT_SORT_BY
}

function parseSortDirection(value) {
  return value === 'desc' ? 'desc' : DEFAULT_SORT_DIR
}

function formatDate(value) {
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
