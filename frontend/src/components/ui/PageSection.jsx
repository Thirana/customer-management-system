import { cn } from '../../lib/cn.js'

export function PageSection({
  actions,
  children,
  className,
  description,
  eyebrow,
  title,
}) {
  return (
    <section
      className={cn(
        'rounded-[28px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-5 shadow-[var(--shadow-soft)] sm:px-6 sm:py-6',
        className,
      )}
    >
      {title || description || actions ? (
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="min-w-0 space-y-2">
            {eyebrow ? (
              <p className="text-xs font-medium uppercase tracking-[0.16em] text-[var(--color-ink-muted)]">
                {eyebrow}
              </p>
            ) : null}
            {title ? <h2 className="text-2xl font-semibold text-[var(--color-ink)]">{title}</h2> : null}
            {description ? (
              <p className="max-w-3xl text-sm leading-6 text-[var(--color-ink-soft)] sm:text-base">
                {description}
              </p>
            ) : null}
          </div>
          {actions ? <div className="flex flex-wrap gap-3">{actions}</div> : null}
        </div>
      ) : null}

      <div className={title || description || actions ? 'mt-6' : ''}>{children}</div>
    </section>
  )
}
