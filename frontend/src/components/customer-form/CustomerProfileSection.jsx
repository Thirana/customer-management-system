import { FormField } from '../common/FormField.jsx'
import { PageSection } from '../ui/PageSection.jsx'
import { inputClasses } from './customerFormUtils.js'

export function CustomerProfileSection({ form, updateField, validationErrors }) {
  return (
    <PageSection
      description="These core fields map directly to the backend customer identity contract."
      title="Profile details"
    >
      <div className="grid gap-4 md:grid-cols-2">
        <FormField error={validationErrors.name} label="Full name" required>
          <input
            className={inputClasses(Boolean(validationErrors.name))}
            placeholder="Thirana Embuldeniya"
            type="text"
            value={form.name}
            onChange={(event) => updateField('name', event.target.value)}
          />
        </FormField>

        <FormField error={validationErrors.nicNumber} label="NIC number" required>
          <input
            className={inputClasses(Boolean(validationErrors.nicNumber))}
            placeholder="NIC-001"
            type="text"
            value={form.nicNumber}
            onChange={(event) => updateField('nicNumber', event.target.value)}
          />
        </FormField>

        <FormField error={validationErrors.dateOfBirth} label="Date of birth" required>
          <input
            className={inputClasses(Boolean(validationErrors.dateOfBirth))}
            type="date"
            value={form.dateOfBirth}
            onChange={(event) => updateField('dateOfBirth', event.target.value)}
          />
        </FormField>
      </div>
    </PageSection>
  )
}
