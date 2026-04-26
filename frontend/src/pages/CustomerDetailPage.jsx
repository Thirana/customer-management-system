import { Alert } from '../components/ui/Alert.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'
import { PageSection } from '../components/ui/PageSection.jsx'
import { StatusBadge } from '../components/ui/StatusBadge.jsx'

export function CustomerDetailPage() {
  return (
    <div className="space-y-6">
      <Alert
        title="Detail route scaffolded"
        message="The detail screen is wired and ready for a full customer profile view backed by the existing customer detail API."
      />

      <PageSection
        description="The final detail screen will group the profile into summary, contact, address, and family sections while preserving the same warm utility shell."
        eyebrow="Customer detail"
        title="Customer profile workspace"
      >
        <div className="flex flex-wrap gap-3">
          <StatusBadge tone="info">Detail API ready</StatusBadge>
          <StatusBadge tone="neutral">Actions come in the next phase</StatusBadge>
        </div>
      </PageSection>

      <PageSection
        description="The route is already aligned to the backend detail endpoint and ready for a richer profile layout."
        title="Profile view"
      >
        <EmptyState
          title="No customer selected yet"
          message="This page is ready for detail loading, action buttons, and grouped profile sections."
        />
      </PageSection>
    </div>
  )
}
