import { PageSection } from '../ui/PageSection.jsx'
import { StatusBadge } from '../ui/StatusBadge.jsx'

export function CustomerFormHeader({
  cityCount,
  isEdit,
  mobileNumberCount,
  selectedFamilyCount,
}) {
  return (
    <PageSection
      description={
        isEdit
          ? 'Update customer profile details, contact records, addresses, and family links in one place.'
          : 'Create a customer profile with identity fields, repeatable contact entries, address records, and linked family members.'
      }
      eyebrow={isEdit ? 'Edit customer' : 'Create customer'}
      title={isEdit ? 'Update customer profile' : 'Create customer profile'}
    >
      <div className="flex flex-wrap gap-3">
        <StatusBadge tone="info">{cityCount} city options loaded</StatusBadge>
        <StatusBadge tone="neutral">{selectedFamilyCount} family links selected</StatusBadge>
        <StatusBadge tone="neutral">{mobileNumberCount} mobile numbers entered</StatusBadge>
      </div>
    </PageSection>
  )
}
