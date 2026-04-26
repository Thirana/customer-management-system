import { useEffect, useRef, useState } from 'react'
import { CustomerImportFormatPanel } from '../components/customer-import/CustomerImportFormatPanel.jsx'
import { CustomerImportHero } from '../components/customer-import/CustomerImportHero.jsx'
import { CustomerImportProgressPanel } from '../components/customer-import/CustomerImportProgressPanel.jsx'
import { CustomerImportUploadPanel } from '../components/customer-import/CustomerImportUploadPanel.jsx'
import { FORMAT_ROWS, POLL_INTERVAL_MS } from '../components/customer-import/customerImportConfig.js'
import {
  isTerminalStatus,
  validateWorkbookFile,
} from '../components/customer-import/customerImportUtils.js'
import { Alert } from '../components/ui/Alert.jsx'
import { getImportStatus, uploadCustomerImport } from '../features/customers/customer-api.js'

export function CustomerImportPage() {
  const fileInputRef = useRef(null)
  const [selectedFile, setSelectedFile] = useState(null)
  const [importStatus, setImportStatus] = useState(null)
  const [errorMessage, setErrorMessage] = useState('')
  const [isUploading, setIsUploading] = useState(false)

  const isProcessing = Boolean(importStatus) && !isTerminalStatus(importStatus.status)

  useEffect(() => {
    if (!importStatus?.jobId || isTerminalStatus(importStatus.status)) {
      return undefined
    }

    let cancelled = false

    // Keep polling state in the route so every progress subview stays driven by one backend snapshot.
    const intervalId = window.setInterval(async () => {
      try {
        const nextStatus = await getImportStatus(importStatus.jobId)
        if (cancelled) {
          return
        }

        setImportStatus(nextStatus)
      } catch (error) {
        if (cancelled) {
          return
        }

        setErrorMessage(error.message || 'Import status could not be refreshed.')
      }
    }, POLL_INTERVAL_MS)

    return () => {
      cancelled = true
      window.clearInterval(intervalId)
    }
  }, [importStatus])

  async function handleSubmit(event) {
    event.preventDefault()

    const validationMessage = validateWorkbookFile(selectedFile)
    if (validationMessage) {
      setErrorMessage(validationMessage)
      return
    }

    setIsUploading(true)
    setErrorMessage('')

    try {
      const status = await uploadCustomerImport(selectedFile)
      setImportStatus(status)
    } catch (error) {
      setErrorMessage(error.message || 'The workbook could not be uploaded.')
    } finally {
      setIsUploading(false)
    }
  }

  function handleFileChange(event) {
    const nextFile = event.target.files?.[0] ?? null
    setSelectedFile(nextFile)
    setImportStatus(null)
    setErrorMessage(nextFile ? validateWorkbookFile(nextFile) : '')
  }

  function handleReset() {
    setSelectedFile(null)
    setImportStatus(null)
    setErrorMessage('')
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  return (
    <div className="space-y-6">
      <CustomerImportHero />

      {errorMessage ? (
        <Alert title="Import action failed" message={errorMessage} tone="error" />
      ) : null}

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.15fr)_minmax(0,0.85fr)]">
        <CustomerImportUploadPanel
          fileInputRef={fileInputRef}
          isProcessing={isProcessing}
          isUploading={isUploading}
          onFileChange={handleFileChange}
          onReset={handleReset}
          onSubmit={handleSubmit}
          selectedFile={selectedFile}
        />

        <CustomerImportFormatPanel formatRows={FORMAT_ROWS} />
      </div>

      <CustomerImportProgressPanel importStatus={importStatus} onReset={handleReset} />
    </div>
  )
}
