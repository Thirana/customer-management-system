export function EmptyState({ title, message }) {
  return (
    <div className="rounded-[24px] border border-dashed border-[var(--color-border-strong)] bg-[rgba(246,241,232,0.72)] px-6 py-10 text-center">
      <h3 className="text-lg font-semibold text-[var(--color-ink)]">{title}</h3>
      <p className="mx-auto mt-3 max-w-2xl text-sm leading-6 text-[var(--color-ink-muted)]">
        {message}
      </p>
    </div>
  )
}
