import { Alert } from '../components/ui/Alert.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'

export function CustomerFormPage({ mode }) {
  const isEdit = mode === 'edit'

  return (
    <div className="page-stack">
      <Alert
        title={isEdit ? 'Edit flow scaffolded' : 'Create flow scaffolded'}
        message="The reusable customer form route is ready. Dynamic mobile numbers, addresses, city lookup, and family-member selection will be implemented in the next phase."
      />

      <section className="surface form-surface">
        <div className="surface-header">
          <h3>{isEdit ? 'Customer Edit Form' : 'Customer Create Form'}</h3>
          <span className="surface-meta">Prepared for shared create and edit behavior</span>
        </div>

        <EmptyState
          title="Form controls will land in Phase 3"
          message="This route already separates create and edit intent, so later implementation can focus on field behavior rather than route wiring."
        />
      </section>
    </div>
  )
}
