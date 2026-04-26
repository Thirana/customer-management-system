export function LoadingState({ label = 'Loading...' }) {
  return (
    <div className="loading-state" aria-live="polite">
      <span className="spinner" aria-hidden="true"></span>
      <span>{label}</span>
    </div>
  )
}
