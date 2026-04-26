import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <main className="not-found">
      <p className="eyebrow">Route not found</p>
      <h1>Page unavailable</h1>
      <p>The requested frontend route is not part of the current customer management workspace.</p>
      <Link className="primary-link" to="/customers">
        Go to customers
      </Link>
    </main>
  )
}
