import { NavLink, Outlet } from 'react-router-dom'
import { cn } from '../../lib/cn.js'
import { StatusBadge } from '../ui/StatusBadge.jsx'

const NAV_ITEMS = [
  {
    to: '/customers',
    label: 'Customers',
    end: true,
    description: 'Browse customers, review key details, and open each profile or edit page.',
  },
  {
    to: '/customers/new',
    label: 'Create',
    description: 'Add a new customer with contact details, addresses, and family members.',
  },
  {
    to: '/customers/import',
    label: 'Import',
    description: 'Upload Excel files and track import progress in one place.',
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
                <StatusBadge tone="info">Customer records ready</StatusBadge>
                <StatusBadge tone="neutral">Import tools ready</StatusBadge>
              </div>

              <div className="space-y-3">
                <h1 className="max-w-4xl text-4xl font-semibold tracking-tight text-[var(--color-ink)] sm:text-5xl">
                  Customer Management System
                </h1>
                <p className="max-w-3xl text-base leading-7 text-[var(--color-ink-soft)]">
                  Manage customer records, profile details, family relationships, and Excel imports
                  from one focused place built for everyday admin work.
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
