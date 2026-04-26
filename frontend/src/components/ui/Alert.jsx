import { cn } from '../../lib/cn.js'

const TONE_CLASSES = {
  info: 'border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-ink-soft)]',
  error: 'border-[rgba(139,59,43,0.2)] bg-[var(--color-danger-soft)] text-[var(--color-danger)]',
  success: 'border-[rgba(62,207,120,0.18)] bg-[var(--color-success-soft)] text-[var(--color-success)]',
  warning: 'border-[rgba(139,97,38,0.18)] bg-[var(--color-warning-soft)] text-[var(--color-warning)]',
}

export function Alert({ title, message, tone = 'info' }) {
  return (
    <div
      className={cn(
        'rounded-[24px] border px-5 py-4 shadow-[var(--shadow-soft)]',
        TONE_CLASSES[tone] ?? TONE_CLASSES.info,
      )}
      role="status"
    >
      <strong className="block text-sm font-semibold">{title}</strong>
      <p className="mt-2 text-sm leading-6">{message}</p>
    </div>
  )
}
