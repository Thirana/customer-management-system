import { Alert } from '../components/ui/Alert.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'

const COLUMNS = ['Name', 'NIC', 'Date of Birth', 'Mobile Count', 'Address Count', 'Actions']

export function CustomerListPage() {
  return (
    <div className="page-stack">
      <Alert
        title="Phase 1 scaffold"
        message="The list route, API client, and shared layout are in place. Data fetching and table actions will be connected in the next frontend phase."
      />

      <section className="surface">
        <div className="surface-header">
          <h3>Customer Registry</h3>
          <span className="surface-meta">Prepared for backend pagination and sorting</span>
        </div>

        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                {COLUMNS.map((column) => (
                  <th key={column}>{column}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              <tr>
                <td colSpan={COLUMNS.length}>
                  <EmptyState
                    title="No customer rows rendered yet"
                    message="This screen is ready for backend-driven customer summaries, loading states, and row actions."
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  )
}
