import { Link } from 'react-router-dom'
import { Button } from '../ui/Button.jsx'
import { PageSection } from '../ui/PageSection.jsx'
import { StatusBadge } from '../ui/StatusBadge.jsx'

export function CustomerImportHero() {
  return (
    <PageSection
      actions={
        <Button as={Link} size="sm" to="/customers" tone="secondary">
          Back to customers
        </Button>
      }
      description="Upload an Excel file, follow its progress, and review any rows that need attention in one place."
      eyebrow="Bulk import"
      title="Import customers from Excel"
    >
      <div className="flex flex-wrap gap-3">
        <StatusBadge tone="info">.xlsx workbooks only</StatusBadge>
        <StatusBadge tone="neutral">Status updates every 2 seconds</StatusBadge>
      </div>
    </PageSection>
  )
}
