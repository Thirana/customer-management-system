import { Button } from '../ui/Button.jsx'
import { PageSection } from '../ui/PageSection.jsx'

export function CustomerImportUploadPanel({
  fileInputRef,
  isProcessing,
  isUploading,
  onFileChange,
  onReset,
  onSubmit,
  selectedFile,
}) {
  return (
    <PageSection
      description="Choose an `.xlsx` file to start the import and keep its latest progress on this page."
      title="Workbook upload"
    >
      <form className="space-y-6" onSubmit={onSubmit}>
        <div className="space-y-3">
          <label className="block text-sm font-medium text-[var(--color-ink)]" htmlFor="customer-import-file">
            Excel workbook
          </label>
          <input
            ref={fileInputRef}
            accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            className="block w-full cursor-pointer rounded-[24px] border border-[var(--color-border)] bg-[rgba(246,241,232,0.72)] px-4 py-4 text-sm text-[var(--color-ink)] file:mr-4 file:cursor-pointer file:rounded-full file:border-0 file:bg-[var(--color-primary)] file:px-4 file:py-2 file:text-sm file:font-medium file:text-white file:transition-colors hover:file:bg-[var(--color-primary-hover)]"
            disabled={isUploading || isProcessing}
            id="customer-import-file"
            onChange={onFileChange}
            type="file"
          />
          <p className="text-sm text-[var(--color-ink-muted)]">
            {selectedFile
              ? `${selectedFile.name} selected`
              : 'Select an Excel workbook to begin the import.'}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <Button disabled={isUploading || isProcessing} type="submit">
            {isUploading ? 'Uploading workbook...' : 'Start import'}
          </Button>
          <Button disabled={isUploading || isProcessing} onClick={onReset} tone="secondary" type="button">
            Clear selection
          </Button>
        </div>
      </form>
    </PageSection>
  )
}
