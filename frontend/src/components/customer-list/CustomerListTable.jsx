import { Link } from 'react-router-dom'
import { Button } from '../ui/Button.jsx'
import { PageSection } from '../ui/PageSection.jsx'
import { PaginationControls } from '../ui/PaginationControls.jsx'
import { formatDate } from './customerListUtils.js'

export function CustomerListTable({
  onDelete,
  onNextPage,
  onPreviousPage,
  onSortChange,
  pageState,
  pendingDeleteId,
  queryState,
}) {
  return (
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
                  onSort={onSortChange}
                />
                <SortableHeader
                  activeField={queryState.sortBy}
                  direction={queryState.sortDir}
                  field="nicNumber"
                  label="NIC"
                  onSort={onSortChange}
                />
                <SortableHeader
                  activeField={queryState.sortBy}
                  direction={queryState.sortDir}
                  field="dateOfBirth"
                  label="Date of Birth"
                  onSort={onSortChange}
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
                      <Button as={Link} size="sm" tone="ghost" to={`/customers/${customer.id}/edit`}>
                        Edit
                      </Button>
                      <Button
                        size="sm"
                        tone="destructive"
                        disabled={pendingDeleteId === customer.id}
                        onClick={() => onDelete(customer.id, customer.name)}
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
          onNext={onNextPage}
          onPrevious={onPreviousPage}
          totalPages={pageState.totalPages}
        />
      </div>
    </PageSection>
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
