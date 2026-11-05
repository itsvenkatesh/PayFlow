import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ArrowUpRight,
  Banknote,
  CreditCard,
  LogOut,
  ReceiptText,
  UserRound,
  WalletCards,
} from 'lucide-react'
import TransactionsPanel from '../components/TransactionsPanel.jsx'
import PaymentsPanel from '../components/PaymentsPanel.jsx'
import BankAccountsPanel from '../components/BankAccountsPanel.jsx'
import { logout } from '../services/authService.js'

const sections = [
  {
    id: 'transactions',
    title: 'Transactions',
    description: 'Review your payment activity and transaction history.',
    emptyMessage: 'Your transactions will appear here when you start using PayFlow.',
    icon: ReceiptText,
    iconClass: 'bg-[#f5effb] text-[#8055a4]',
  },
  {
    id: 'payments',
    title: 'Payments',
    description: 'Send and manage payments from one place.',
    emptyMessage: 'Your payments will appear here after you make a transfer.',
    icon: ArrowUpRight,
    iconClass: 'bg-[#e9f5ed] text-[#287147]',
  },
  {
    id: 'bank-accounts',
    title: 'Bank accounts',
    description: 'View and organize your linked bank accounts.',
    emptyMessage: 'Your linked bank accounts will appear here.',
    icon: CreditCard,
    iconClass: 'bg-[#edf2fb] text-[#526da5]',
  },
]

function HomePage({ user, onLoggedOut }) {
  const navigate = useNavigate()
  const [activeSectionId, setActiveSectionId] = useState('transactions')
  const [profileOpen, setProfileOpen] = useState(false)
  const [isLoggingOut, setIsLoggingOut] = useState(false)
  const [logoutError, setLogoutError] = useState(null)
  const email = user?.email ?? 'PayFlow member'
  const initials = email === 'PayFlow member' ? 'P' : email.charAt(0).toUpperCase()
  const activeSection =
    sections.find((section) => section.id === activeSectionId) ?? sections[0]
  const ActiveIcon = activeSection.icon

  async function handleLogout() {
    setIsLoggingOut(true)
    setLogoutError(null)

    try {
      await logout()
      onLoggedOut()
      navigate('/login', { replace: true })
    } catch (error) {
      setLogoutError(
        error instanceof Error ? error.message : 'Unable to sign out. Please try again.',
      )
    } finally {
      setIsLoggingOut(false)
    }
  }

  return (
    <div className="min-h-screen bg-[#f6f8f6] text-[#17231d]">
      <aside className="fixed inset-y-0 left-0 z-20 hidden w-[250px] flex-col border-r border-[#e8ece8] bg-white px-5 py-6 md:flex">
        <Link
          className="mb-10 flex w-fit items-center gap-2.5 text-[21px] font-semibold tracking-[-1.1px] text-[#17382b] no-underline"
          to="/"
        >
          <span className="grid size-[35px] place-items-center rounded-[11px] bg-[#edf6ef] text-[#286c45]">
            <WalletCards size={21} strokeWidth={2.2} />
          </span>
          payflow
        </Link>

        <p className="mb-3 px-3 text-[10px] font-semibold tracking-[1.2px] text-[#9aa49d]">
          WORKSPACE
        </p>
        <nav className="space-y-1" aria-label="Main navigation">
          {sections.map(({ id, title, icon: Icon }) => (
            <button
              aria-current={activeSectionId === id ? 'page' : undefined}
              className={`flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-left text-[13px] transition-colors ${
                activeSectionId === id
                  ? 'bg-[#eef6ef] font-semibold text-[#286c45]'
                  : 'font-medium text-[#66736b] hover:bg-[#f5f7f5] hover:text-[#286c45]'
              }`}
              key={id}
              onClick={() => setActiveSectionId(id)}
              type="button"
            >
              <Icon size={17} />
              {title}
            </button>
          ))}
        </nav>

        <div className="mt-auto rounded-xl border border-[#e8eee9] bg-[#f8faf8] p-4">
          <div className="mb-2 flex size-8 items-center justify-center rounded-lg bg-white text-[#43805a] shadow-sm">
            <Banknote size={17} />
          </div>
          <p className="mb-1 text-xs font-semibold text-[#29372f]">Money, made simple</p>
          <p className="m-0 text-[11px] leading-5 text-[#7a867e]">
            Your financial activity, all in one place.
          </p>
        </div>
      </aside>

      <div className="min-h-screen md:pl-[250px]">
        <header className="sticky top-0 z-10 flex h-[72px] items-center justify-end border-b border-[#e8ece8] bg-white/95 px-5 backdrop-blur sm:px-8">
          <div className="relative">
            <button
              aria-expanded={profileOpen}
              aria-label="Open profile menu"
              className="flex items-center gap-2.5 rounded-full p-1 pr-2 text-left hover:bg-[#f5f7f5]"
              onClick={() => setProfileOpen((open) => !open)}
              type="button"
            >
              <span className="grid size-9 place-items-center rounded-full bg-[#e7f2e9] text-sm font-semibold text-[#286c45]">
                {initials}
              </span>
              <span className="hidden max-w-[180px] truncate text-xs font-medium text-[#45534a] sm:block">
                {email}
              </span>
              <UserRound className="text-[#7c8980]" size={16} />
            </button>
            {profileOpen && (
              <div className="absolute right-0 top-12 w-64 rounded-xl border border-[#e8ece8] bg-white p-3 shadow-xl">
                <div className="flex items-center gap-3 border-b border-[#edf0ed] px-2 pb-3 pt-1">
                  <span className="grid size-9 shrink-0 place-items-center rounded-full bg-[#e7f2e9] text-sm font-semibold text-[#286c45]">
                    {initials}
                  </span>
                  <div className="min-w-0">
                    <p className="m-0 text-xs font-semibold text-[#29372f]">Your profile</p>
                    <p className="m-0 truncate text-[11px] text-[#7a867e]">{email}</p>
                  </div>
                </div>
                <button
                  className="mt-2 flex w-full items-center gap-2 rounded-lg px-2 py-2 text-left text-xs font-medium text-[#9d3933] hover:bg-[#fff6f5] disabled:cursor-wait disabled:opacity-60"
                  disabled={isLoggingOut}
                  onClick={handleLogout}
                  type="button"
                >
                  <LogOut size={15} />
                  {isLoggingOut ? 'Signing out…' : 'Sign out'}
                </button>
                {logoutError && (
                  <p className="mb-0 mt-2 px-2 text-xs text-[#9d3933]" role="alert">
                    {logoutError}
                  </p>
                )}
              </div>
            )}
          </div>
        </header>

        <main className="mx-auto max-w-[1180px] px-5 py-9 sm:px-8 sm:py-11">
          <div className="mb-8 flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
            <div>
              <p className="mb-2 text-[10px] font-bold tracking-[1.3px] text-[#39815a]">
                YOUR PAYFLOW
              </p>
              <h1 className="m-0 text-[28px] font-semibold tracking-[-1.2px] text-[#17231d] sm:text-[32px]">
                {activeSection.title}
              </h1>
              <p className="mb-0 mt-2 text-sm text-[#748078]">
                {activeSection.description}
              </p>
            </div>
            <div className="flex items-center gap-2 self-start rounded-full border border-[#dceade] bg-white px-3 py-2 text-[11px] font-medium text-[#477653] sm:self-auto">
              <span className="size-1.5 rounded-full bg-[#55a66c]" />
              Account secured
            </div>
          </div>

          {activeSectionId === 'transactions' ? (
            <TransactionsPanel />
          ) : activeSectionId === 'payments' ? (
            <PaymentsPanel />
          ) : activeSectionId === 'bank-accounts' ? (
            <BankAccountsPanel />
          ) : (
            <section
              aria-label={activeSection.title}
              className="rounded-2xl border border-[#e7ece8] bg-white p-6 shadow-[0_2px_10px_rgba(31,51,38,0.025)] sm:p-8"
            >
              <div className="flex min-h-[250px] flex-col items-center justify-center text-center">
                <span className={`mb-4 grid size-12 place-items-center rounded-xl ${activeSection.iconClass}`}>
                  <ActiveIcon size={21} />
                </span>
                <h2 className="mb-2 text-base font-semibold text-[#28372e]">
                  {activeSection.title}
                </h2>
                <p className="m-0 max-w-md text-sm leading-6 text-[#7a867e]">
                  {activeSection.emptyMessage}
                </p>
              </div>
            </section>
          )}
        </main>
      </div>
    </div>
  )
}

export default HomePage
