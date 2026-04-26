import { Alert } from '../components/ui/Alert.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'
import { PageSection } from '../components/ui/PageSection.jsx'
import { StatusBadge } from '../components/ui/StatusBadge.jsx'

export function CustomerFormPage({ mode }) {
  const isEdit = mode === 'edit'

  return (
    <div className="space-y-6">
      <Alert
        title={isEdit ? 'Edit flow scaffolded' : 'Create flow scaffolded'}
        message="The reusable customer form route is ready. Dynamic mobile numbers, addresses, city lookup, and family-member selection will be implemented in the next phase."
      />

      <PageSection
        description="This route already separates create and edit intent, so the next phase can focus on field behavior, validation feedback, and backend binding instead of route wiring."
        eyebrow={isEdit ? 'Edit customer' : 'Create customer'}
        title={isEdit ? 'Shared customer form workspace' : 'New customer form workspace'}
      >
        <div className="flex flex-wrap gap-3">
          <StatusBadge tone="info">Reusable form route</StatusBadge>
          <StatusBadge tone="neutral">Backend CRUD already available</StatusBadge>
        </div>
      </PageSection>

      <PageSection
        description="The next implementation pass will introduce the actual field groups and submission workflow here."
        title={isEdit ? 'Customer edit form' : 'Customer create form'}
      >
        <EmptyState
          title="Form controls will land in Phase 3"
          message="This screen is reserved for the customer form, including repeatable mobile numbers, multiple addresses, city lookup, and family-member selection."
        />
      </PageSection>
    </div>
  )
}
