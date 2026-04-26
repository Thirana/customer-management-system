import { cn } from '../../lib/cn.js'

const TONE_CLASSES = {
  neutral: 'bg-[var(--color-surface-muted)] text-[var(--color-ink-soft)]',
  info: 'bg-[var(--color-primary-soft)] text-[var(--color-primary)]',
  success: 'bg-[var(--color-success-soft)] text-[var(--color-success)]',
  warning: 'bg-[var(--color-warning-soft)] text-[var(--color-warning)]',
  danger: 'bg-[var(--color-danger-soft)] text-[var(--color-danger)]',
}

export function StatusBadge({ children, className, tone = 'neutral' }) {
  return (
    <span
      className={cn(
        'inline-flex min-h-8 items-center rounded-full px-3 text-sm font-medium',
        TONE_CLASSES[tone],
        className,
      )}
    >
      {children}
    </span>
  )
}
