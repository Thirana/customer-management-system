export function FormField({ children, className = '', error, label, required = false }) {
  return (
    <label className={`block ${className}`}>
      <span className="mb-2 block text-sm font-medium text-[var(--color-ink-soft)]">
        {label}
        {required ? <span className="ml-1 text-[var(--color-danger)]">*</span> : null}
      </span>
      {children}
      {error ? <p className="mt-2 text-sm text-[var(--color-danger)]">{error}</p> : null}
    </label>
  )
}
