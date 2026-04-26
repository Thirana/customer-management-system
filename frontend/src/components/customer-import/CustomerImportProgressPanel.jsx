import { Link } from 'react-router-dom'
import { Alert } from '../ui/Alert.jsx'
import { Button } from '../ui/Button.jsx'
import { EmptyState } from '../ui/EmptyState.jsx'
import { PageSection } from '../ui/PageSection.jsx'
import { StatusBadge } from '../ui/StatusBadge.jsx'
import { formatStatusLabel, statusTone } from './customerImportUtils.js'

export function CustomerImportProgressPanel({ importStatus, onReset }) {
  const isProcessing = Boolean(importStatus) && !isTerminalStatus(importStatus.status)

  return (
    <PageSection
      description="Check progress, totals, and any rows with issues here while the import runs and after it finishes."
      title="Import progress"
    >
      {!importStatus ? (
        <EmptyState
          title="No import started yet"
          message="Upload a valid `.xlsx` file to start the import and see its progress here."
        />
      ) : (
        <div className="space-y-6">
          <div className="flex flex-wrap gap-3">
            <StatusBadge tone={statusTone(importStatus.status)}>
              {formatStatusLabel(importStatus.status)}
            </StatusBadge>
            <StatusBadge tone="neutral">Import ID {importStatus.jobId}</StatusBadge>
            <StatusBadge tone="neutral">{importStatus.progressPercent}% complete</StatusBadge>
          </div>

          <div className="space-y-2">
            <div className="flex items-center justify-between gap-3 text-sm text-[var(--color-ink-soft)]">
              <span>Progress</span>
              <span>
                {importStatus.processedCount} of {importStatus.totalCount ?? 0} rows processed
              </span>
            </div>
            <div className="h-3 overflow-hidden rounded-full bg-[rgba(36,58,52,0.08)]">
              <div
                aria-hidden="true"
                className="h-full rounded-full bg-[var(--color-primary)] transition-[width] duration-300"
                style={{ width: `${Math.max(0, Math.min(importStatus.progressPercent, 100))}%` }}
              ></div>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <SummaryCard label="Processed rows" value={importStatus.processedCount} />
            <SummaryCard label="Successful rows" tone="success" value={importStatus.successCount} />
            <SummaryCard label="Failed rows" tone="danger" value={importStatus.failureCount} />
            <SummaryCard label="Total rows" value={importStatus.totalCount ?? 0} />
          </div>

          {isProcessing ? (
            <Alert
              title="Import in progress"
              message="This page checks the import every 2 seconds until it finishes."
              tone="info"
            />
          ) : importStatus.status === 'COMPLETED' ? (
            <Alert
              title="Import completed"
              message="The file has been processed. Review the totals and any rows with issues below."
              tone="success"
            />
          ) : importStatus.status === 'FAILED' ? (
            <Alert
              title="Import failed"
              message="The import stopped before finishing. Review the rows with issues below, then try again with a corrected file."
              tone="warning"
            />
          ) : null}

          <div className="flex flex-wrap gap-3">
            <Button onClick={onReset} tone="secondary" type="button">
              Start another import
            </Button>
            <Button as={Link} to="/customers" tone="secondary">
              Return to customer list
            </Button>
          </div>

          <div className="space-y-4 rounded-[24px] border border-dashed border-[var(--color-border-strong)] bg-[rgba(246,241,232,0.48)] px-5 py-5">
            <div className="space-y-2">
              <h3 className="text-xl font-semibold text-[var(--color-ink)]">Rows with issues</h3>
              <p className="text-sm leading-6 text-[var(--color-ink-soft)]">
                Rows with problems are skipped, while the rest of the file continues importing.
              </p>
            </div>
            {importStatus.errors.length === 0 ? (
              <QuietInfo message="No issues have been reported for this import." />
            ) : (
              <div className="space-y-3">
                {importStatus.errors.map((error) => (
                  <div
                    key={`${error.rowNumber}-${error.message}`}
                    className="rounded-[20px] border border-[rgba(139,59,43,0.18)] bg-[var(--color-danger-soft)] px-4 py-4"
                  >
                    <p className="text-sm font-semibold text-[var(--color-danger)]">
                      Row {error.rowNumber}
                    </p>
                    <p className="mt-2 text-sm leading-6 text-[var(--color-danger)]">{error.message}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </PageSection>
  )
}

function SummaryCard({ label, tone = 'neutral', value }) {
  const toneClassName = {
    neutral: 'border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] text-[var(--color-ink)]',
    success: 'border-[rgba(62,207,120,0.22)] bg-[var(--color-success-soft)] text-[var(--color-success)]',
    danger: 'border-[rgba(139,59,43,0.18)] bg-[var(--color-danger-soft)] text-[var(--color-danger)]',
  }

  return (
    <div className={`rounded-[24px] border px-4 py-4 ${toneClassName[tone] ?? toneClassName.neutral}`}>
      <p className="text-sm font-medium">{label}</p>
      <p className="mt-2 text-2xl font-semibold">{value}</p>
    </div>
  )
}

function QuietInfo({ message }) {
  return (
    <div className="rounded-[20px] border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-4 text-sm text-[var(--color-ink-soft)]">
      {message}
    </div>
  )
}

function isTerminalStatus(status) {
  return status === 'COMPLETED' || status === 'FAILED'
}
