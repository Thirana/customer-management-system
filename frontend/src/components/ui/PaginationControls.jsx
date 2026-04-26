import { Button } from './Button.jsx'

export function PaginationControls({
  currentPage,
  hasNextPage,
  hasPreviousPage,
  onNext,
  onPrevious,
  totalPages,
}) {
  return (
    <div className="flex flex-col gap-3 border-t border-[var(--color-border)] pt-5 sm:flex-row sm:items-center sm:justify-between">
      <div className="text-sm text-[var(--color-ink-muted)]">
        Page <span className="font-medium text-[var(--color-ink)]">{currentPage}</span> of{' '}
        <span className="font-medium text-[var(--color-ink)]">{totalPages}</span>
      </div>

      <div className="flex gap-3">
        <Button size="sm" tone="secondary" disabled={!hasPreviousPage} onClick={onPrevious}>
          Previous
        </Button>
        <Button size="sm" tone="secondary" disabled={!hasNextPage} onClick={onNext}>
          Next
        </Button>
      </div>
    </div>
  )
}
