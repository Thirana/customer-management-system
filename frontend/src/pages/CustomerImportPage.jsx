import { Alert } from '../components/ui/Alert.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'
import { LoadingState } from '../components/ui/LoadingState.jsx'
import { PageSection } from '../components/ui/PageSection.jsx'
import { StatusBadge } from '../components/ui/StatusBadge.jsx'

export function CustomerImportPage() {
  // Preserve the final layout slots now because the upload and polling APIs are already stable on the backend.
  return (
    <div className="space-y-6">
      <Alert
        title="Import route scaffolded"
        message="The upload and polling API methods are ready. The user-facing import workflow will be connected in the final frontend phase."
      />

      <PageSection
        description="This workspace is reserved for the assignment’s strongest frontend interaction: uploading an Excel workbook, tracking progress, and reviewing row-level results."
        eyebrow="Bulk import"
        title="Excel import workspace"
      >
        <div className="flex flex-wrap gap-3">
          <StatusBadge tone="info">Async backend ready</StatusBadge>
          <StatusBadge tone="neutral">Status polling endpoint ready</StatusBadge>
        </div>
      </PageSection>

      <div className="grid gap-6 lg:grid-cols-2">
        <PageSection title="Workbook upload">
          <EmptyState
            title="Upload controls pending"
            message="The screen is prepared for `.xlsx` selection, upload trigger, and file-level validation feedback."
          />
        </PageSection>

        <PageSection title="Import progress">
          <LoadingState label="Progress panel placeholder" />
        </PageSection>
      </div>
    </div>
  )
}
