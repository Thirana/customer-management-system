import { cn } from '../../lib/cn.js'

const VARIANT_CLASSES = {
  primary:
    'bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)] focus-visible:ring-[var(--color-primary)]',
  secondary:
    'border border-[var(--color-border)] bg-[var(--color-surface)] text-[var(--color-ink)] hover:border-[var(--color-border-strong)] hover:bg-[var(--color-surface-muted)] focus-visible:ring-[var(--color-primary)]',
  ghost:
    'bg-transparent text-[var(--color-ink-soft)] hover:bg-[rgba(36,58,52,0.06)] hover:text-[var(--color-ink)] focus-visible:ring-[var(--color-primary)]',
  destructive:
    'bg-[var(--color-danger)] text-white hover:bg-[#763123] focus-visible:ring-[var(--color-danger)]',
}

const SIZE_CLASSES = {
  md: 'min-h-11 px-5 text-sm',
  sm: 'min-h-9 px-4 text-sm',
}

export function Button({
  as: Component = 'button',
  children,
  className,
  size = 'md',
  tone = 'primary',
  type = 'button',
  ...props
}) {
  return (
    <Component
      className={cn(
        'inline-flex cursor-pointer items-center justify-center gap-2 rounded-full font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-[var(--color-page)] disabled:cursor-not-allowed disabled:opacity-60',
        VARIANT_CLASSES[tone],
        SIZE_CLASSES[size],
        className,
      )}
      type={Component === 'button' ? type : undefined}
      {...props}
    >
      {children}
    </Component>
  )
}
