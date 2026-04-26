import { PageSection } from '../ui/PageSection.jsx'

export function CustomerImportFormatPanel({ formatRows }) {
  return (
    <PageSection
      description="Use the first sheet with the first four columns arranged like this."
      title="Expected workbook format"
    >
      <div className="overflow-hidden rounded-[24px] border border-[var(--color-border)]">
        <table className="min-w-full border-collapse bg-[var(--color-surface)]">
          <thead>
            <tr className="bg-[var(--color-surface-muted)] text-left">
              <th className="px-4 py-3 text-sm font-semibold text-[var(--color-ink-soft)]">Column</th>
              <th className="px-4 py-3 text-sm font-semibold text-[var(--color-ink-soft)]">Header</th>
              <th className="px-4 py-3 text-sm font-semibold text-[var(--color-ink-soft)]">Notes</th>
            </tr>
          </thead>
          <tbody>
            {formatRows.map((row) => (
              <tr
                key={row.column}
                className="border-t border-[var(--color-border)] text-sm text-[var(--color-ink-soft)]"
              >
                <td className="px-4 py-3 font-medium text-[var(--color-ink)]">{row.column}</td>
                <td className="px-4 py-3">{row.header}</td>
                <td className="px-4 py-3">{row.notes}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

    </PageSection>
  )
}
