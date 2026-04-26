import { FormField } from '../common/FormField.jsx'
import { Button } from '../ui/Button.jsx'
import { PageSection } from '../ui/PageSection.jsx'
import { SelectMenu } from '../ui/SelectMenu.jsx'
import { hasAddressContent, inputClasses } from './customerFormUtils.js'

export function CustomerAddressesSection({
  addAddress,
  addresses,
  cityOptions,
  countryOptions,
  removeAddress,
  updateAddress,
  validationErrors,
}) {
  return (
    <PageSection
      actions={
        <Button size="sm" tone="secondary" onClick={addAddress}>
          Add address
        </Button>
      }
      description="Each address row supports two free-text lines plus a required city selection when the row is used."
      title="Addresses"
    >
      <div className="space-y-4">
        {addresses.map((address, index) => (
          <div
            key={`address-${index}`}
            className="space-y-4 rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] p-4"
          >
            <div className="flex items-start justify-between gap-3">
              <div>
                <p className="text-sm font-semibold text-[var(--color-ink)]">Address {index + 1}</p>
                <p className="mt-1 text-sm text-[var(--color-ink-muted)]">
                  Keep the city aligned to the backend master data list.
                </p>
              </div>

              <Button size="sm" tone="secondary" onClick={() => removeAddress(index)}>
                Remove
              </Button>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <FormField
                error={validationErrors[`addresses[${index}].countryId`]}
                label="Country"
                required={hasAddressContent(address)}
              >
                <SelectMenu
                  options={countryOptions}
                  placeholder="Select a country"
                  value={address.countryId}
                  onChange={(nextValue) => updateAddress(index, 'countryId', String(nextValue))}
                />
              </FormField>

              <FormField
                error={validationErrors[`addresses[${index}].addressLine1`]}
                label="Address line 1"
              >
                <input
                  className={inputClasses(Boolean(validationErrors[`addresses[${index}].addressLine1`]))}
                  placeholder="12 Main Street"
                  type="text"
                  value={address.addressLine1}
                  onChange={(event) => updateAddress(index, 'addressLine1', event.target.value)}
                />
              </FormField>

              <FormField
                error={validationErrors[`addresses[${index}].addressLine2`]}
                label="Address line 2"
              >
                <input
                  className={inputClasses(Boolean(validationErrors[`addresses[${index}].addressLine2`]))}
                  placeholder="Apartment, district, or landmark"
                  type="text"
                  value={address.addressLine2}
                  onChange={(event) => updateAddress(index, 'addressLine2', event.target.value)}
                />
              </FormField>

              <FormField
                error={validationErrors[`addresses[${index}].cityId`]}
                label="City"
                required={hasAddressContent(address)}
              >
                <SelectMenu
                  disabled={!address.countryId}
                  options={cityOptions.filter((city) => city.countryId === address.countryId)}
                  placeholder={address.countryId ? 'Select a city' : 'Select country first'}
                  value={address.cityId}
                  onChange={(nextValue) => updateAddress(index, 'cityId', String(nextValue))}
                />
              </FormField>
            </div>
          </div>
        ))}
      </div>
    </PageSection>
  )
}
