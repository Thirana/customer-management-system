import { FAMILY_MEMBER_SEARCH_MIN_LENGTH } from './customerFormConfig.js'
import { inputClasses } from './customerFormUtils.js'
import { Button } from '../ui/Button.jsx'
import { PageSection } from '../ui/PageSection.jsx'

export function CustomerFamilySection({
  familySearchQuery,
  onFamilySearchChange,
  onSelectFamilyMember,
  onRemoveFamilyMember,
  searchResults,
  searchStatus,
  selectedFamilyMembers,
  validationError,
}) {
  const trimmedQuery = familySearchQuery.trim()
  const showIdleHint = trimmedQuery.length < FAMILY_MEMBER_SEARCH_MIN_LENGTH

  return (
    <PageSection
      description="Search by customer name or NIC number to attach existing customers as family members without loading the entire registry."
      title="Family members"
    >
      <div className="space-y-5">
        <div>
          <label className="mb-2 block text-sm font-medium text-[var(--color-ink)]" htmlFor="family-member-search">
            Search existing customers
          </label>
          <input
            id="family-member-search"
            type="search"
            value={familySearchQuery}
            onChange={(event) => onFamilySearchChange(event.target.value)}
            className={inputClasses(Boolean(validationError))}
            placeholder="Search by name or NIC number"
          />
          <p className="mt-2 text-sm text-[var(--color-ink-muted)]">
            Enter at least {FAMILY_MEMBER_SEARCH_MIN_LENGTH} characters to search.
          </p>
          {validationError ? (
            <p className="mt-3 text-sm text-[var(--color-danger)]">{validationError}</p>
          ) : null}
        </div>

        <div className="rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] p-4">
          {showIdleHint ? (
            <p className="text-sm text-[var(--color-ink-muted)]">
              Search results will appear here once the query is long enough.
            </p>
          ) : searchStatus === 'loading' ? (
            <p className="text-sm text-[var(--color-ink-muted)]">Searching customer registry...</p>
          ) : searchStatus === 'error' ? (
            <p className="text-sm text-[var(--color-danger)]">
              Family-member lookup failed. Adjust the query and try again.
            </p>
          ) : searchResults.length === 0 ? (
            <p className="text-sm text-[var(--color-ink-muted)]">
              No matching customers were found for the current query.
            </p>
          ) : (
            <div className="space-y-3">
              {searchResults.map((customer) => (
                <div
                  key={customer.id}
                  className="flex flex-col gap-3 rounded-[22px] border border-[var(--color-border)] bg-[var(--color-surface)] px-4 py-4 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="min-w-0">
                    <p className="font-medium text-[var(--color-ink)]">{customer.name}</p>
                    <p className="mt-1 text-sm text-[var(--color-ink-muted)]">{customer.nicNumber}</p>
                  </div>
                  <Button size="sm" type="button" onClick={() => onSelectFamilyMember(customer)}>
                    Add family member
                  </Button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between gap-3">
            <h3 className="text-sm font-semibold text-[var(--color-ink)]">Selected family members</h3>
            <span className="text-sm text-[var(--color-ink-muted)]">
              {selectedFamilyMembers.length} selected
            </span>
          </div>

          {selectedFamilyMembers.length === 0 ? (
            <div className="rounded-[24px] border border-dashed border-[var(--color-border-strong)] bg-[rgba(246,241,232,0.72)] px-5 py-6 text-sm text-[var(--color-ink-muted)]">
              No family members have been selected yet.
            </div>
          ) : (
            <div className="grid gap-3 md:grid-cols-2">
              {selectedFamilyMembers.map((member) => (
                <div
                  key={member.id}
                  className="rounded-[22px] border border-[rgba(62,207,120,0.28)] bg-[var(--color-success-soft)] px-4 py-4"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <p className="font-medium text-[var(--color-ink)]">{member.name}</p>
                      <p className="mt-1 text-sm text-[var(--color-ink-muted)]">{member.nicNumber}</p>
                    </div>
                    <span className="mt-1 inline-flex size-2.5 shrink-0 rounded-full bg-[#3ecf78]" aria-hidden="true"></span>
                  </div>
                  <div className="mt-4">
                    <Button size="sm" tone="secondary" type="button" onClick={() => onRemoveFamilyMember(member.id)}>
                      Remove
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </PageSection>
  )
}
