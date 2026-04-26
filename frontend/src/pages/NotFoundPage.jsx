import { Link } from 'react-router-dom'
import { Button } from '../components/ui/Button.jsx'
import { PageSection } from '../components/ui/PageSection.jsx'

export function NotFoundPage() {
  // Keep unknown routes inside the same product shell language instead of dropping to a generic fallback.
  return (
    <main className="min-h-screen bg-[var(--color-page)] px-4 py-8 sm:px-6 lg:px-8">
      <div className="mx-auto max-w-3xl">
        <PageSection
          actions={
            <Button as={Link} to="/customers">
              Go to customers
            </Button>
          }
          description="The page you tried to open is not available."
          eyebrow="Page not found"
          title="Page unavailable"
        />
      </div>
    </main>
  )
}
