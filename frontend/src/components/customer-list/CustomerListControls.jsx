import { SelectMenu } from '../ui/SelectMenu.jsx'

export function CustomerListControls({
  onPageSizeChange,
  onSortDirectionChange,
  onSortFieldChange,
  pageSizeOptions,
  queryState,
  sortableColumns,
}) {
  return (
    <div className="grid gap-4 lg:grid-cols-2">
      <section className="rounded-[24px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-5 shadow-[var(--shadow-soft)]">
        <p className="text-sm font-semibold text-[var(--color-ink)]">List density</p>
        <p className="mt-2 text-sm leading-6 text-[var(--color-ink-muted)]">
          Choose how many customers to show at once.
        </p>
        <label className="mt-4 block">
          <span className="mb-2 block text-sm font-medium text-[var(--color-ink-soft)]">Rows</span>
          <SelectMenu
            options={pageSizeOptions.map((option) => ({
              value: option,
              label: String(option),
            }))}
            placeholder="Select rows"
            value={queryState.size}
            onChange={onPageSizeChange}
          />
        </label>
      </section>

      <section className="rounded-[24px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-5 shadow-[var(--shadow-soft)]">
        <p className="text-sm font-semibold text-[var(--color-ink)]">Sort customers</p>
        <p className="mt-2 text-sm leading-6 text-[var(--color-ink-muted)]">
          Choose how customers should be ordered in the list.
        </p>

        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <label className="block">
            <span className="mb-2 block text-sm font-medium text-[var(--color-ink-soft)]">
              Sort field
            </span>
            <SelectMenu
              options={Object.entries(sortableColumns).map(([field, label]) => ({
                value: field,
                label,
              }))}
              placeholder="Select sort field"
              value={queryState.sortBy}
              onChange={onSortFieldChange}
            />
          </label>

          <label className="block">
            <span className="mb-2 block text-sm font-medium text-[var(--color-ink-soft)]">
              Direction
            </span>
            <SelectMenu
              options={[
                { value: 'asc', label: 'Ascending' },
                { value: 'desc', label: 'Descending' },
              ]}
              placeholder="Select direction"
              value={queryState.sortDir}
              onChange={onSortDirectionChange}
            />
          </label>
        </div>
      </section>
    </div>
  )
}
