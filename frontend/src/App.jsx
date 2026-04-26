import { Navigate, Route, Routes } from 'react-router-dom'
import { AppShell } from './components/layout/AppShell.jsx'
import { CustomerDetailPage } from './pages/CustomerDetailPage.jsx'
import { CustomerFormPage } from './pages/CustomerFormPage.jsx'
import { CustomerImportPage } from './pages/CustomerImportPage.jsx'
import { CustomerListPage } from './pages/CustomerListPage.jsx'
import { NotFoundPage } from './pages/NotFoundPage.jsx'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/customers" replace />} />
      <Route element={<AppShell />}>
        <Route path="/customers" element={<CustomerListPage />} />
        <Route path="/customers/new" element={<CustomerFormPage mode="create" />} />
        <Route path="/customers/:id" element={<CustomerDetailPage />} />
        <Route path="/customers/:id/edit" element={<CustomerFormPage mode="edit" />} />
        <Route path="/customers/import" element={<CustomerImportPage />} />
      </Route>
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default App
