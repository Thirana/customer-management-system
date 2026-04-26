import { useEffect, useId, useMemo, useRef, useState } from 'react'
import { cn } from '../../lib/cn.js'

export function SelectMenu({
  className,
  disabled = false,
  onChange,
  options = [],
  placeholder,
  value,
}) {
  const rootRef = useRef(null)
  const optionRefs = useRef([])
  const listboxId = useId()
  const [isOpen, setIsOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)

  const selectedIndex = useMemo(
    () => options.findIndex((option) => String(option.value) === String(value)),
    [options, value],
  )

  const selectedOption = selectedIndex >= 0 ? options[selectedIndex] : null

  useEffect(() => {
    if (!isOpen) {
      return undefined
    }

    function handlePointerDown(event) {
      if (!rootRef.current?.contains(event.target)) {
        setIsOpen(false)
      }
    }

    document.addEventListener('mousedown', handlePointerDown)
    return () => {
      document.removeEventListener('mousedown', handlePointerDown)
    }
  }, [isOpen])

  useEffect(() => {
    if (!isOpen) {
      return
    }

    const indexToFocus = activeIndex >= 0 ? activeIndex : Math.max(selectedIndex, 0)
    optionRefs.current[indexToFocus]?.focus()
  }, [activeIndex, isOpen, selectedIndex])

  function openMenu() {
    setActiveIndex(Math.max(selectedIndex, 0))
    setIsOpen(true)
  }

  function closeMenu() {
    setIsOpen(false)
    setActiveIndex(-1)
  }

  function commitSelection(nextValue) {
    onChange(nextValue)
    closeMenu()
  }

  function handleTriggerKeyDown(event) {
    if (disabled) {
      return
    }

    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault()
      setActiveIndex(
        event.key === 'ArrowDown'
          ? Math.min(Math.max(selectedIndex, 0) + 1, options.length - 1)
          : Math.max(Math.max(selectedIndex, 0) - 1, 0),
      )
      setIsOpen(true)
    }

    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      if (isOpen) {
        closeMenu()
      } else {
        openMenu()
      }
    }

    if (event.key === 'Escape') {
      closeMenu()
    }
  }

  function handleOptionKeyDown(event, index) {
    // Keep keyboard navigation local to the open listbox so it behaves like a real control.
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      setActiveIndex(Math.min(index + 1, options.length - 1))
    }

    if (event.key === 'ArrowUp') {
      event.preventDefault()
      setActiveIndex(Math.max(index - 1, 0))
    }

    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault()
      commitSelection(options[index].value)
    }

    if (event.key === 'Escape' || event.key === 'Tab') {
      closeMenu()
    }
  }

  return (
    <div className={cn('relative', className)} ref={rootRef}>
      <button
        aria-controls={listboxId}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
        className={cn(
          'flex min-h-11 w-full items-center justify-between rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface-muted)] px-4 text-left text-sm text-[var(--color-ink)] outline-none transition-colors focus-visible:border-[var(--color-border-strong)] focus-visible:ring-2 focus-visible:ring-[rgba(36,58,52,0.08)]',
          disabled ? 'cursor-not-allowed opacity-55' : '',
        )}
        disabled={disabled}
        type="button"
        onClick={() => {
          if (disabled) {
            return
          }

          if (isOpen) {
            closeMenu()
          } else {
            openMenu()
          }
        }}
        onKeyDown={handleTriggerKeyDown}
      >
        <span className="truncate">{selectedOption?.label ?? placeholder}</span>
        <span
          className={cn(
            'ml-4 shrink-0 text-[var(--color-ink-muted)] transition-transform',
            isOpen ? 'rotate-180' : '',
          )}
          aria-hidden="true"
        >
          ▾
        </span>
      </button>

      {isOpen ? (
        <div
          className="absolute left-0 right-0 top-[calc(100%+0.5rem)] z-20 overflow-hidden rounded-[20px] border border-[var(--color-border)] bg-[var(--color-surface)] shadow-[var(--shadow-soft)]"
        >
          <ul aria-label={placeholder} className="py-2" id={listboxId} role="listbox">
            {options.map((option, index) => {
              const isSelected = String(option.value) === String(value)
              const isActive = index === activeIndex

              return (
                <li key={option.value} role="presentation">
                  <button
                    ref={(element) => {
                      optionRefs.current[index] = element
                    }}
                    aria-selected={isSelected}
                    className={cn(
                      'flex w-full items-center justify-between gap-3 px-4 py-3 text-left text-sm transition-colors focus:outline-none',
                      isSelected
                        ? 'bg-[var(--color-primary-soft)] text-[var(--color-primary)]'
                        : isActive
                          ? 'bg-[rgba(36,58,52,0.05)] text-[var(--color-ink)]'
                          : 'text-[var(--color-ink-soft)] hover:bg-[rgba(36,58,52,0.05)] hover:text-[var(--color-ink)]',
                    )}
                    role="option"
                    tabIndex={isActive ? 0 : -1}
                    type="button"
                    onClick={() => commitSelection(option.value)}
                    onKeyDown={(event) => handleOptionKeyDown(event, index)}
                    onMouseEnter={() => setActiveIndex(index)}
                  >
                    <span>{option.label}</span>
                    {isSelected ? (
                      <span className="text-xs font-semibold uppercase tracking-[0.14em]">
                        Selected
                      </span>
                    ) : null}
                  </button>
                </li>
              )
            })}
          </ul>
        </div>
      ) : null}
    </div>
  )
}
