export function createRequestId(prefix = 'frontend') {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return `${prefix}-${crypto.randomUUID()}`
  }

  // Fallback keeps local development usable in runtimes where randomUUID is unavailable.
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`
}
