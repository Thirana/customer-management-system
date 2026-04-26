import { NavLink, Outlet } from 'react-router-dom'
import { cn } from '../../lib/cn.js'
import { StatusBadge } from '../ui/StatusBadge.jsx'

const NAV_ITEMS = [
  {
    to: '/customers',
    label: 'Customers',
    end: true,
    description: 'Browse the registry, review summaries, and move into customer detail or edit flows.',
  },
  {
    to: '/customers/new',
    label: 'Create',
    description: 'Add a new customer record with contact details, addresses, and linked family members.',
  },
  {
    to: '/customers/import',
    label: 'Import',
    description: 'Upload Excel workbooks and monitor asynchronous import progress from one place.',
  },
]

export function AppShell() {
  return (
    <div className="min-h-screen bg-[var(--color-page)] text-[var(--color-ink)]">
      <div className="mx-auto max-w-7xl px-4 py-5 sm:px-6 sm:py-6 lg:px-8">
        <header className="rounded-[32px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-6 shadow-[var(--shadow-soft)] sm:px-7 sm:py-8">
          <div className="flex flex-col gap-6">
            <div className="space-y-4">
              <div className="flex flex-wrap gap-2">
                <StatusBadge tone="info">Spring Boot API ready</StatusBadge>
                <StatusBadge tone="neutral">Frontend Phase 2</StatusBadge>
              </div>

              <div className="space-y-3">
                <h1 className="max-w-4xl text-4xl font-semibold tracking-tight text-[var(--color-ink)] sm:text-5xl">
                  Customer Management System
                </h1>
                <p className="max-w-3xl text-base leading-7 text-[var(--color-ink-soft)]">
                  Local reviewer workspace for customer CRUD, master data lookup, and Excel import
                  verification. The interface stays quiet and utility-focused so the remaining
                  frontend phases can reuse the same structure cleanly.
                </p>
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center gap-3">
                <p className="text-lg font-semibold tracking-tight text-[var(--color-ink)] sm:text-xl">
                  Main workflows
                </p>
              </div>

              <nav className="grid gap-3 md:grid-cols-3" aria-label="Primary">
              {NAV_ITEMS.map((item) => (
                <NavLink
                  key={item.to}
                  className={({ isActive }) =>
                    cn(
                      'group rounded-[24px] border px-4 py-4 transition-colors',
                      isActive
                        ? 'border-[var(--color-border-strong)] bg-[var(--color-surface)] shadow-[inset_0_0_0_1px_rgba(214,201,178,0.28)]'
                        : 'border-[var(--color-border)] bg-[var(--color-surface-muted)] hover:border-[var(--color-border-strong)] hover:bg-[var(--color-surface)]',
                    )
                  }
                  end={item.end}
                  to={item.to}
                >
                  {({ isActive }) => (
                    <div className="space-y-3">
                      <div className="flex items-center justify-between gap-3">
                        <div className="min-w-0">
                          <p className="text-lg font-semibold text-[var(--color-ink)]">{item.label}</p>
                        </div>
                        <span
                          className={cn(
                            'mt-1 inline-flex size-2.5 shrink-0 rounded-full transition-colors',
                            isActive
                              ? 'bg-[#3ecf78]'
                              : 'bg-[var(--color-border-strong)] group-hover:bg-[#3ecf78]',
                          )}
                          aria-hidden="true"
                        ></span>
                      </div>

                      <p className="text-sm leading-6 text-[var(--color-ink-muted)]">
                        {item.description}
                      </p>
                    </div>
                  )}
                </NavLink>
              ))}
              </nav>
            </div>
          </div>
        </header>

        <main className="mt-6 space-y-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
