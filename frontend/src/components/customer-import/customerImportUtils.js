export function validateWorkbookFile(file) {
  if (!file) {
    return 'Select an `.xlsx` workbook before starting the import.'
  }

  const normalizedName = file.name.toLowerCase()
  if (!normalizedName.endsWith('.xlsx')) {
    return 'Only `.xlsx` workbooks are supported for bulk import.'
  }

  return ''
}

export function isTerminalStatus(status) {
  return status === 'COMPLETED' || status === 'FAILED'
}

export function formatStatusLabel(status) {
  if (!status) {
    return 'Unknown'
  }

  return status.charAt(0) + status.slice(1).toLowerCase()
}

export function statusTone(status) {
  if (status === 'COMPLETED') {
    return 'success'
  }

  if (status === 'FAILED') {
    return 'warning'
  }

  return 'info'
}
