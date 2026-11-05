import { useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import AuthPage from './pages/AuthPage.jsx'
import HomePage from './pages/HomePage.jsx'
import { getCurrentUser } from './services/authService.js'

function RequireAuth({ children, user }) {
  if (!user) {
    return <Navigate replace to="/login" />
  }

  return children
}

function GuestOnly({ children, user }) {
  if (user) {
    return <Navigate replace to="/" />
  }

  return children
}

function App() {
  const [user, setUser] = useState(null)
  const [isCheckingSession, setIsCheckingSession] = useState(true)
  const [sessionError, setSessionError] = useState(null)

  useEffect(() => {
    let isMounted = true

    getCurrentUser()
      .then((currentUser) => {
        if (isMounted) {
          setUser(currentUser)
          setSessionError(null)
        }
      })
      .catch((error) => {
        if (isMounted) {
          setSessionError(error.message)
        }
      })
      .finally(() => {
        if (isMounted) {
          setIsCheckingSession(false)
        }
      })

    return () => {
      isMounted = false
    }
  }, [])

  return (
    <BrowserRouter>
      {isCheckingSession ? (
        <main className="grid min-h-screen place-items-center bg-[#f6f8f6] px-6 text-sm text-[#66736b]">
          Checking your session…
        </main>
      ) : sessionError ? (
        <main className="grid min-h-screen place-items-center bg-[#f6f8f6] px-6">
          <section className="max-w-md rounded-2xl border border-[#e7ece8] bg-white p-8 text-center shadow-sm">
            <h1 className="m-0 text-lg font-semibold text-[#17231d]">
              Unable to verify your session
            </h1>
            <p className="mb-5 mt-2 text-sm text-[#66736b]" role="alert">
              {sessionError}
            </p>
            <button
              className="rounded-lg bg-[#286c45] px-4 py-2 text-sm font-semibold text-white hover:bg-[#205b39]"
              onClick={() => window.location.reload()}
              type="button"
            >
              Try again
            </button>
          </section>
        </main>
      ) : (
        <Routes>
          <Route
            path="/"
            element={
              <RequireAuth user={user}>
                <HomePage user={user} onLoggedOut={() => setUser(null)} />
              </RequireAuth>
            }
          />
          <Route
            path="/login"
            element={
              <GuestOnly user={user}>
                <AuthPage mode="login" onAuthenticated={setUser} />
              </GuestOnly>
            }
          />
          <Route
            path="/signup"
            element={
              <GuestOnly user={user}>
                <AuthPage mode="signup" onAuthenticated={setUser} />
              </GuestOnly>
            }
          />
          <Route path="*" element={<Navigate replace to="/" />} />
        </Routes>
      )}
    </BrowserRouter>
  )
}

export default App
