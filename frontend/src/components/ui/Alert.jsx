export function Alert({ title, message, tone = 'info' }) {
  return (
    <div className={`alert alert-${tone}`} role="status">
      <strong>{title}</strong>
      <p>{message}</p>
    </div>
  )
}
