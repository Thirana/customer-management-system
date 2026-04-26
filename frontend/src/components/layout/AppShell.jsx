import { NavLink, Outlet, useLocation } from 'react-router-dom'

const PAGE_METADATA = [
  {
    matches: (pathname) => pathname === '/customers',
    title: 'Customers',
    description: 'Browse, create, and manage customer records.',
  },
  {
    matches: (pathname) => pathname === '/customers/new',
    title: 'Create Customer',
    description: 'Prepare a new customer profile with contact and relationship details.',
  },
  {
    matches: (pathname) => pathname === '/customers/import',
    title: 'Import Customers',
    description: 'Upload Excel workbooks and monitor asynchronous import progress.',
  },
  {
    matches: (pathname) => pathname.endsWith('/edit'),
    title: 'Edit Customer',
    description: 'Update a stored customer profile while keeping backend validation intact.',
  },
  {
    matches: (pathname) => /^\/customers\/[^/]+$/.test(pathname),
    title: 'Customer Detail',
    description: 'Inspect a full customer profile, including nested contact and family data.',
  },
]

const NAV_ITEMS = [
  { to: '/customers', label: 'Customers', end: true },
  { to: '/customers/new', label: 'Create' },
  { to: '/customers/import', label: 'Import' },
]

function resolvePageMetadata(pathname) {
  return PAGE_METADATA.find((item) => item.matches(pathname)) ?? {
    title: 'Customer Management System',
    description: 'Frontend workspace for the customer management assignment.',
  }
}

export function AppShell() {
  const location = useLocation()
  const page = resolvePageMetadata(location.pathname)

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand-block">
          <p className="eyebrow">Assignment Workspace</p>
          <h1 className="brand-title">Customer Management System</h1>
        </div>
        <div className="runtime-chip">Frontend Phase 1</div>
      </header>

      <div className="workspace">
        <aside className="sidebar">
          <nav className="nav-list" aria-label="Primary">
            {NAV_ITEMS.map((item) => (
              <NavLink
                key={item.to}
                className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}
                end={item.end}
                to={item.to}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <section className="sidebar-panel">
            <h2 className="sidebar-heading">Prepared Backend Surface</h2>
            <ul className="sidebar-list">
              <li>Customer CRUD</li>
              <li>City lookup</li>
              <li>Async import status polling</li>
            </ul>
          </section>
        </aside>

        <main className="main-panel">
          <section className="page-header">
            <div>
              <p className="eyebrow">Frontend Scaffold</p>
              <h2>{page.title}</h2>
            </div>
            <p className="page-description">{page.description}</p>
          </section>

          <section className="page-content">
            <Outlet />
          </section>
        </main>
      </div>
    </div>
  )
}
