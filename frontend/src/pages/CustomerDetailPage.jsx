import { Alert } from '../components/ui/Alert.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'

export function CustomerDetailPage() {
  return (
    <div className="page-stack">
      <Alert
        title="Detail route scaffolded"
        message="The detail screen is wired and ready for a full customer profile view backed by the existing customer detail API."
      />

      <section className="surface">
        <div className="surface-header">
          <h3>Customer Profile</h3>
          <span className="surface-meta">Prepared for summary, contact, address, and family sections</span>
        </div>

        <EmptyState
          title="No customer selected yet"
          message="This page is ready for detail loading, action buttons, and grouped profile sections."
        />
      </section>
    </div>
  )
}
