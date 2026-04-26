import { Alert } from '../components/ui/Alert.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'
import { LoadingState } from '../components/ui/LoadingState.jsx'

export function CustomerImportPage() {
  return (
    <div className="page-stack">
      <Alert
        title="Import route scaffolded"
        message="The upload and polling API methods are ready. The user-facing import workflow will be connected in the final frontend phase."
      />

      <section className="surface import-grid">
        <div className="subsurface">
          <h3>Workbook Upload</h3>
          <EmptyState
            title="Upload controls pending"
            message="The screen is prepared for `.xlsx` selection, upload trigger, and file-level validation feedback."
          />
        </div>

        <div className="subsurface">
          <h3>Import Progress</h3>
          <LoadingState label="Progress panel placeholder" />
        </div>
      </section>
    </div>
  )
}
