export function LoadingState({ label = 'Loading...' }) {
  return (
    <div
      className="flex min-h-36 items-center justify-center gap-3 rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] px-6 py-10 text-[var(--color-ink-soft)]"
      aria-live="polite"
    >
      <span
        className="size-5 animate-spin rounded-full border-2 border-[rgba(36,58,52,0.2)] border-t-[var(--color-primary)]"
        aria-hidden="true"
      ></span>
      <span className="text-sm font-medium">{label}</span>
    </div>
  )
}
