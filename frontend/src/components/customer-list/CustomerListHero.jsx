import { Link } from 'react-router-dom'
import { Button } from '../ui/Button.jsx'
import { PageSection } from '../ui/PageSection.jsx'
import { StatusBadge } from '../ui/StatusBadge.jsx'

export function CustomerListHero({ pageState, queryState, sortableColumns }) {
  return (
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
          Sorted by {sortableColumns[queryState.sortBy]} {queryState.sortDir.toUpperCase()}
        </StatusBadge>
      </div>
    </PageSection>
  )
}
