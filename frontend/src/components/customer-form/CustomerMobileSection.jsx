import { FormField } from '../common/FormField.jsx'
import { Button } from '../ui/Button.jsx'
import { PageSection } from '../ui/PageSection.jsx'
import { inputClasses } from './customerFormUtils.js'

export function CustomerMobileSection({
  addMobileNumber,
  mobileNumbers,
  removeMobileNumber,
  updateMobileNumber,
  validationErrors,
}) {
  return (
    <PageSection
      actions={
        <Button size="sm" tone="secondary" onClick={addMobileNumber}>
          Add mobile number
        </Button>
      }
      description="Blank rows are ignored when the payload is submitted."
      title="Mobile numbers"
    >
      <div className="space-y-4">
        {mobileNumbers.map((mobileNumber, index) => (
          <div
            key={`mobile-${index}`}
            className="flex flex-col gap-3 rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] p-4 sm:flex-row sm:items-end"
          >
            <FormField
              className="flex-1"
              error={validationErrors[`mobileNumbers[${index}]`]}
              label={`Mobile number ${index + 1}`}
            >
              <input
                className={inputClasses(Boolean(validationErrors[`mobileNumbers[${index}]`]))}
                placeholder="0714594040"
                type="text"
                value={mobileNumber}
                onChange={(event) => updateMobileNumber(index, event.target.value)}
              />
            </FormField>

            <Button size="sm" tone="secondary" onClick={() => removeMobileNumber(index)}>
              Remove
            </Button>
          </div>
        ))}
      </div>
    </PageSection>
  )
}
