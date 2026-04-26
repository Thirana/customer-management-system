import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { deleteCustomer, getCustomer } from '../features/customers/customer-api.js'
import { formatDate } from '../components/customer-list/customerListUtils.js'
import { Alert } from '../components/ui/Alert.jsx'
import { Button } from '../components/ui/Button.jsx'
import { EmptyState } from '../components/ui/EmptyState.jsx'
import { LoadingState } from '../components/ui/LoadingState.jsx'
import { PageSection } from '../components/ui/PageSection.jsx'
import { StatusBadge } from '../components/ui/StatusBadge.jsx'

export function CustomerDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [status, setStatus] = useState('loading')
  const [customer, setCustomer] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')
  const [isDeleting, setIsDeleting] = useState(false)

  useEffect(() => {
    let cancelled = false

    async function loadCustomer() {
      setStatus('loading')
      setErrorMessage('')

      try {
        const result = await getCustomer(id)
        if (cancelled) {
          return
        }

        setCustomer(result)
        setStatus('success')
      } catch (error) {
        if (cancelled) {
          return
        }

        setCustomer(null)
        setErrorMessage(error.message || 'The customer could not be loaded.')
        setStatus(error.status === 404 ? 'not-found' : 'error')
      }
    }

    loadCustomer()

    return () => {
      cancelled = true
    }
  }, [id])

  async function handleDelete() {
    if (!customer) {
      return
    }

    const confirmed = window.confirm(
      `Delete customer "${customer.name}"? This removes the customer and related contact records.`,
    )

    if (!confirmed) {
      return
    }

    setIsDeleting(true)
    setErrorMessage('')

    try {
      await deleteCustomer(customer.id)
      navigate('/customers', {
        state: {
          flash: {
            title: 'Customer deleted',
            message: 'The customer was removed successfully.',
            tone: 'success',
          },
        },
      })
    } catch (error) {
      setErrorMessage(error.message || 'The customer could not be deleted.')
    } finally {
      setIsDeleting(false)
    }
  }

  if (status === 'loading') {
    return (
      <PageSection
        description="Loading the selected customer's profile, contact records, addresses, and family links."
        eyebrow="Customer detail"
        title="Preparing customer profile"
      >
        <LoadingState label="Loading customer profile..." />
      </PageSection>
    )
  }

  if (status === 'not-found') {
    return (
      <div className="space-y-6">
        <Alert
          title="Customer not found"
          message={errorMessage || 'The requested customer record does not exist.'}
          tone="warning"
        />

        <PageSection
          actions={
            <Button as={Link} to="/customers" tone="secondary">
              Back to customers
            </Button>
          }
          description="We could not find a customer for this link."
          eyebrow="Customer detail"
          title="Record unavailable"
        />
      </div>
    )
  }

  if (status === 'error') {
    return (
      <div className="space-y-6">
        <Alert
          title="Customer detail failed"
          message={errorMessage || 'The customer detail screen could not be loaded.'}
          tone="error"
        />

        <PageSection
          actions={
            <Button as={Link} to="/customers" tone="secondary">
              Back to customers
            </Button>
          }
          description="The customer details could not be loaded right now."
          eyebrow="Customer detail"
          title="Unable to load profile"
        />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageSection
        actions={
          <>
            <Button as={Link} size="sm" to="/customers" tone="secondary">
              Back to customers
            </Button>
            <Button as={Link} size="sm" to={`/customers/${customer.id}/edit`} tone="secondary">
              Edit customer
            </Button>
            <Button disabled={isDeleting} onClick={handleDelete} size="sm" tone="destructive">
              {isDeleting ? 'Deleting...' : 'Delete customer'}
            </Button>
          </>
        }
        description="View this customer's profile details, contact information, addresses, and family members."
        eyebrow="Customer detail"
        title={customer.name}
      >
        <div className="flex flex-wrap gap-3">
          <StatusBadge tone="info">{customer.nicNumber}</StatusBadge>
          <StatusBadge tone="neutral">{customer.mobileNumbers.length} mobile numbers</StatusBadge>
          <StatusBadge tone="neutral">{customer.addresses.length} addresses</StatusBadge>
          <StatusBadge tone="neutral">{customer.familyMembers.length} family members</StatusBadge>
        </div>
      </PageSection>

      {errorMessage ? (
        <Alert title="Delete failed" message={errorMessage} tone="error" />
      ) : null}

      <PageSection
        description="These are the main personal details saved for this customer."
        title="Profile details"
      >
        <div className="grid gap-4 md:grid-cols-3">
          <DetailField label="Full name" value={customer.name} />
          <DetailField label="NIC number" value={customer.nicNumber} />
          <DetailField label="Date of birth" value={formatDate(customer.dateOfBirth)} />
        </div>
      </PageSection>

      <PageSection
        description="All saved mobile numbers are shown here."
        title="Mobile numbers"
      >
        {customer.mobileNumbers.length === 0 ? (
          <QuietEmptyState message="No mobile numbers are stored for this customer." />
        ) : (
          <div className="grid gap-3 md:grid-cols-2">
            {customer.mobileNumbers.map((mobileNumber, index) => (
              <DetailCard
                key={`${mobileNumber}-${index}`}
                label={`Mobile number ${index + 1}`}
                value={mobileNumber}
              />
            ))}
          </div>
        )}
      </PageSection>

      <PageSection
        description="Saved addresses are shown with their city and country."
        title="Addresses"
      >
        {customer.addresses.length === 0 ? (
          <QuietEmptyState message="No addresses are stored for this customer." />
        ) : (
          <div className="grid gap-4 lg:grid-cols-2">
            {customer.addresses.map((address, index) => (
              <div
                key={address.id ?? `address-${index}`}
                className="space-y-4 rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] p-4"
              >
                <div className="flex items-center justify-between gap-3">
                  <p className="text-sm font-semibold text-[var(--color-ink)]">Address {index + 1}</p>
                  <StatusBadge tone="neutral">{address.countryName}</StatusBadge>
                </div>

                <div className="grid gap-3 sm:grid-cols-2">
                  <DetailCard label="Address line 1" value={toDisplayValue(address.addressLine1)} />
                  <DetailCard label="Address line 2" value={toDisplayValue(address.addressLine2)} />
                  <DetailCard label="City" value={address.cityName} />
                  <DetailCard label="Country" value={address.countryName} />
                </div>
              </div>
            ))}
          </div>
        )}
      </PageSection>

      <PageSection
        description="Linked family members remain navigable so related customer profiles can be inspected directly."
        title="Family members"
      >
        {customer.familyMembers.length === 0 ? (
          <QuietEmptyState message="No family members are linked to this customer." />
        ) : (
          <div className="grid gap-4 lg:grid-cols-2">
            {customer.familyMembers.map((familyMember) => (
              <div
                key={familyMember.id}
                className="rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] p-4"
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 space-y-1">
                    <p className="font-semibold text-[var(--color-ink)]">{familyMember.name}</p>
                    <p className="text-sm text-[var(--color-ink-muted)]">{familyMember.nicNumber}</p>
                  </div>
                  <Button as={Link} size="sm" to={`/customers/${familyMember.id}`} tone="secondary">
                    View profile
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </PageSection>
    </div>
  )
}

function DetailField({ label, value }) {
  return (
    <div className="rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] px-4 py-4">
      <p className="text-sm font-medium text-[var(--color-ink-muted)]">{label}</p>
      <p className="mt-2 text-base font-semibold text-[var(--color-ink)]">{value}</p>
    </div>
  )
}

function DetailCard({ label, value }) {
  return (
    <div className="rounded-[20px] border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-4">
      <p className="text-sm font-medium text-[var(--color-ink-muted)]">{label}</p>
      <p className="mt-2 text-sm leading-6 text-[var(--color-ink)]">{value}</p>
    </div>
  )
}

function QuietEmptyState({ message }) {
  return (
    <EmptyState
      message={message}
      title="Nothing to show"
    />
  )
}

function toDisplayValue(value) {
  return value && value.trim() ? value : 'Not provided'
}
