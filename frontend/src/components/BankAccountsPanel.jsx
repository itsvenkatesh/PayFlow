import { useCallback, useEffect, useState } from 'react'
import {
  ArrowDownLeft,
  ArrowUpRight,
  Building2,
  Check,
  Eye,
  EyeOff,
  LoaderCircle,
  Plus,
  RefreshCw,
  Wallet,
} from 'lucide-react'
import {
  createBankAccount,
  depositToBankAccount,
  getBankAccountBalance,
  getBankAccounts,
  withdrawFromBankAccount,
} from '../services/bankAccountService.js'

const fieldClassName =
  'mt-1.5 w-full rounded-lg border border-[#dfe7e0] bg-white px-3 py-2.5 text-sm text-[#344138] outline-none focus:border-[#68a17a] focus:ring-2 focus:ring-[#68a17a]/15'

function formatCurrency(value, currency) {
  const amount = Number(value)
  if (!Number.isFinite(amount)) return String(value ?? '—')

  try {
    return new Intl.NumberFormat(undefined, {
      style: 'currency',
      currency,
    }).format(amount)
  } catch {
    return `${currency ?? ''} ${amount.toFixed(2)}`.trim()
  }
}

function maskAccountNumber(value) {
  return `•••• ${String(value ?? '').slice(-4)}`
}

function BankAccountsPanel() {
  const [accounts, setAccounts] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [visibleBalances, setVisibleBalances] = useState(true)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [bankName, setBankName] = useState('')
  const [currency, setCurrency] = useState('USD')
  const [isCreating, setIsCreating] = useState(false)
  const [accountAction, setAccountAction] = useState(null)
  const [actionAmount, setActionAmount] = useState('')
  const [isSubmittingAction, setIsSubmittingAction] = useState(false)
  const [refreshingAccount, setRefreshingAccount] = useState('')

  useEffect(() => {
    let isCurrent = true

    getBankAccounts()
      .then((result) => {
        if (!Array.isArray(result)) {
          throw new Error('The bank accounts response was not a list.')
        }
        if (isCurrent) {
          setAccounts(result)
          setError('')
        }
      })
      .catch((requestError) => {
        if (isCurrent) {
          setError(
            requestError instanceof Error
              ? requestError.message
              : 'Unable to load your bank accounts.',
          )
        }
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false)
      })

    return () => {
      isCurrent = false
    }
  }, [])

  const loadAccounts = useCallback(async () => {
    try {
      const result = await getBankAccounts()
      if (!Array.isArray(result)) {
        throw new Error('The bank accounts response was not a list.')
      }
      setError('')
      setAccounts(result)
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Unable to load your bank accounts.',
      )
    } finally {
      setIsLoading(false)
      setIsRefreshing(false)
    }
  }, [])

  async function handleRefresh() {
    setIsRefreshing(true)
    setError('')
    await loadAccounts()
  }

  async function handleRefreshBalance(account) {
    setRefreshingAccount(account.bankAccountNumber)
    setError('')
    setNotice('')

    try {
      const result = await getBankAccountBalance(account.bankAccountNumber)
      setAccounts((currentAccounts) =>
        currentAccounts.map((currentAccount) =>
          currentAccount.bankAccountNumber === result.bankAccountNumber
            ? { ...currentAccount, balance: result.balance }
            : currentAccount,
        ),
      )
      setNotice(`Balance updated for ${account.bankName}.`)
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Unable to refresh this balance.',
      )
    } finally {
      setRefreshingAccount('')
    }
  }

  async function handleCreateAccount(event) {
    event.preventDefault()
    setIsCreating(true)
    setError('')
    setNotice('')

    try {
      const account = await createBankAccount({ bankName: bankName.trim(), currency })
      setAccounts((currentAccounts) => [account, ...currentAccounts])
      setBankName('')
      setCurrency('USD')
      setShowCreateForm(false)
      setNotice('Your new bank account is ready.')
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : 'Unable to create the bank account.',
      )
    } finally {
      setIsCreating(false)
    }
  }

  function startAccountAction(account, type) {
    setError('')
    setNotice('')
    setActionAmount('')
    setAccountAction({ account, type })
  }

  async function handleAccountAction(event) {
    event.preventDefault()
    if (!accountAction) return

    const { account, type } = accountAction
    const amount = Number(actionAmount)
    if (!Number.isFinite(amount) || amount <= 0) {
      setError('Enter an amount greater than zero.')
      return
    }
    if (type === 'withdraw' && amount > Number(account.balance)) {
      setError('The withdrawal amount is greater than the available balance.')
      return
    }

    setIsSubmittingAction(true)
    setError('')
    setNotice('')

    try {
      if (type === 'deposit') {
        await depositToBankAccount(account.bankAccountNumber, amount)
      } else {
        await withdrawFromBankAccount(account.bankAccountNumber, amount)
      }
      await loadAccounts()
      setNotice(
        type === 'deposit'
          ? `Funds added to ${account.bankName}.`
          : `Withdrawal completed from ${account.bankName}.`,
      )
      setAccountAction(null)
      setActionAmount('')
    } catch (requestError) {
      setError(
        requestError instanceof Error
          ? requestError.message
          : `Unable to ${type === 'deposit' ? 'add funds' : 'withdraw funds'}.`,
      )
    } finally {
      setIsSubmittingAction(false)
    }
  }

  const activeAccounts = accounts.filter((account) => account.status === 'ACTIVE')
  const totalBalanceByCurrency = activeAccounts.reduce((totals, account) => {
    const accountCurrency = account.currency || '—'
    totals[accountCurrency] = (totals[accountCurrency] || 0) + Number(account.balance || 0)
    return totals
  }, {})

  return (
    <div className="space-y-5">
      <section className="rounded-2xl border border-[#e7ece8] bg-white p-5 shadow-[0_2px_10px_rgba(31,51,38,0.025)] sm:p-6">
        <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
          <div>
            <p className="mb-1 text-[10px] font-bold tracking-[1px] text-[#39815a]">
              ACCOUNT OVERVIEW
            </p>
            <h2 className="m-0 text-lg font-semibold text-[#28372e]">Your balances</h2>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              aria-label={visibleBalances ? 'Hide balances' : 'Show balances'}
              className="inline-flex items-center gap-2 rounded-lg border border-[#dfe7e0] px-3 py-2 text-xs font-medium text-[#58665d] hover:bg-[#f8faf8]"
              onClick={() => setVisibleBalances((visible) => !visible)}
              type="button"
            >
              {visibleBalances ? <EyeOff size={15} /> : <Eye size={15} />}
              {visibleBalances ? 'Hide balances' : 'Show balances'}
            </button>
            <button
              className="inline-flex items-center gap-2 rounded-lg border border-[#dfe7e0] px-3 py-2 text-xs font-medium text-[#58665d] hover:bg-[#f8faf8] disabled:opacity-60"
              disabled={isRefreshing || isLoading}
              onClick={handleRefresh}
              type="button"
            >
              <RefreshCw className={isRefreshing ? 'animate-spin' : ''} size={15} />
              Refresh accounts
            </button>
            <button
              className="inline-flex items-center gap-2 rounded-lg bg-[#286c45] px-3 py-2 text-xs font-semibold text-white hover:bg-[#205b39]"
              onClick={() => {
                setError('')
                setNotice('')
                setShowCreateForm((show) => !show)
              }}
              type="button"
            >
              <Plus size={15} />
              New account
            </button>
          </div>
        </div>

        <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {Object.entries(totalBalanceByCurrency).length === 0 ? (
            <div className="rounded-xl bg-[#f8faf8] p-4 sm:col-span-2 lg:col-span-3">
              <p className="m-0 text-sm font-medium text-[#46544b]">No active account balances yet</p>
              <p className="mb-0 mt-1 text-xs text-[#7a867e]">
                Create an account to see its balance here.
              </p>
            </div>
          ) : (
            Object.entries(totalBalanceByCurrency).map(([accountCurrency, total]) => (
              <div className="rounded-xl bg-[#f5f8f5] p-4" key={accountCurrency}>
                <p className="m-0 text-xs font-medium text-[#7a867e]">
                  Total available · {accountCurrency}
                </p>
                <p className="mb-0 mt-2 text-xl font-semibold tracking-tight text-[#23372b]">
                  {visibleBalances ? formatCurrency(total, accountCurrency) : '••••••'}
                </p>
              </div>
            ))
          )}
        </div>
      </section>

      {error && (
        <p
          className="m-0 rounded-lg border border-[#f1d2cf] bg-[#fff7f6] px-4 py-3 text-sm text-[#9d3933]"
          role="alert"
        >
          {error}
        </p>
      )}
      {notice && (
        <p className="m-0 flex items-center gap-2 rounded-lg border border-[#dceade] bg-[#f4faf5] px-4 py-3 text-sm text-[#34764a]" role="status">
          <Check size={16} />
          {notice}
        </p>
      )}

      {showCreateForm && (
        <section className="rounded-2xl border border-[#e7ece8] bg-white p-5 shadow-[0_2px_10px_rgba(31,51,38,0.025)] sm:p-6">
          <h2 className="m-0 text-base font-semibold text-[#28372e]">Create a bank account</h2>
          <p className="mb-0 mt-1 text-xs text-[#7a867e]">
            New accounts start with a zero balance.
          </p>
          <form className="mt-5 grid gap-4 sm:grid-cols-2" onSubmit={handleCreateAccount}>
            <label className="block text-xs font-semibold text-[#46544b]">
              Bank name
              <input
                autoComplete="organization"
                className={fieldClassName}
                maxLength={100}
                onChange={(event) => setBankName(event.target.value)}
                placeholder="e.g. PayFlow Bank"
                required
                value={bankName}
              />
            </label>
            <label className="block text-xs font-semibold text-[#46544b]">
              Currency
              <select
                className={fieldClassName}
                onChange={(event) => setCurrency(event.target.value)}
                value={currency}
              >
                {['USD', 'EUR', 'GBP', 'INR', 'CAD', 'AUD'].map((option) => (
                  <option key={option} value={option}>
                    {option}
                  </option>
                ))}
              </select>
            </label>
            <div className="flex justify-end gap-2 sm:col-span-2">
              <button
                className="rounded-lg border border-[#dfe7e0] px-4 py-2.5 text-sm font-semibold text-[#58665d] hover:bg-[#f8faf8]"
                onClick={() => setShowCreateForm(false)}
                type="button"
              >
                Cancel
              </button>
              <button
                className="inline-flex items-center gap-2 rounded-lg bg-[#286c45] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#205b39] disabled:cursor-wait disabled:opacity-60"
                disabled={isCreating || !bankName.trim()}
                type="submit"
              >
                {isCreating && <LoaderCircle className="animate-spin" size={16} />}
                {isCreating ? 'Creating…' : 'Create account'}
              </button>
            </div>
          </form>
        </section>
      )}

      {accountAction && (
        <section className="rounded-2xl border border-[#dceade] bg-white p-5 shadow-[0_2px_10px_rgba(31,51,38,0.025)] sm:p-6">
          <h2 className="m-0 text-base font-semibold text-[#28372e]">
            {accountAction.type === 'deposit' ? 'Add funds' : 'Withdraw funds'}
          </h2>
          <p className="mb-0 mt-1 text-xs text-[#7a867e]">
            {accountAction.account.bankName} ·{' '}
            {maskAccountNumber(accountAction.account.bankAccountNumber)} ·{' '}
            {accountAction.account.currency}
          </p>
          <form className="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end" onSubmit={handleAccountAction}>
            <label className="block flex-1 text-xs font-semibold text-[#46544b]">
              Amount ({accountAction.account.currency})
              <input
                autoFocus
                className={fieldClassName}
                min="0.01"
                onChange={(event) => setActionAmount(event.target.value)}
                required
                step="0.01"
                type="number"
                value={actionAmount}
              />
            </label>
            <div className="flex justify-end gap-2">
              <button
                className="rounded-lg border border-[#dfe7e0] px-4 py-2.5 text-sm font-semibold text-[#58665d] hover:bg-[#f8faf8]"
                disabled={isSubmittingAction}
                onClick={() => setAccountAction(null)}
                type="button"
              >
                Cancel
              </button>
              <button
                className="inline-flex items-center gap-2 rounded-lg bg-[#286c45] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#205b39] disabled:cursor-wait disabled:opacity-60"
                disabled={isSubmittingAction || !actionAmount}
                type="submit"
              >
                {isSubmittingAction && <LoaderCircle className="animate-spin" size={16} />}
                {isSubmittingAction
                  ? 'Processing…'
                  : accountAction.type === 'deposit'
                    ? 'Add funds'
                    : 'Withdraw'}
              </button>
            </div>
          </form>
          <p className="mb-0 mt-3 text-[11px] leading-5 text-[#929b95]">
            This updates your PayFlow account balance and transaction history.
          </p>
        </section>
      )}

      <section className="overflow-hidden rounded-2xl border border-[#e7ece8] bg-white shadow-[0_2px_10px_rgba(31,51,38,0.025)]">
        <div className="flex items-center justify-between border-b border-[#edf0ed] px-5 py-4 sm:px-6">
          <div>
            <h2 className="m-0 text-base font-semibold text-[#28372e]">Your bank accounts</h2>
            <p className="mb-0 mt-1 text-xs text-[#7a867e]">
              {accounts.length} account{accounts.length === 1 ? '' : 's'} linked
            </p>
          </div>
          <Wallet className="text-[#43805a]" size={19} />
        </div>

        {isLoading ? (
          <div className="grid min-h-40 place-items-center text-sm text-[#7a867e]">
            Loading your bank accounts…
          </div>
        ) : accounts.length === 0 ? (
          <div className="flex min-h-52 flex-col items-center justify-center px-6 text-center">
            <span className="mb-3 grid size-11 place-items-center rounded-xl bg-[#edf6ef] text-[#43805a]">
              <Building2 size={20} />
            </span>
            <h3 className="m-0 text-sm font-semibold text-[#344138]">No bank accounts yet</h3>
            <p className="mb-4 mt-1 max-w-sm text-xs leading-5 text-[#7a867e]">
              Create an account to keep your PayFlow balances and transactions organized.
            </p>
            <button
              className="rounded-lg bg-[#286c45] px-4 py-2 text-xs font-semibold text-white hover:bg-[#205b39]"
              onClick={() => setShowCreateForm(true)}
              type="button"
            >
              Create your first account
            </button>
          </div>
        ) : (
          <div className="grid gap-4 p-4 sm:p-5 lg:grid-cols-2">
            {accounts.map((account) => {
              const isActive = account.status === 'ACTIVE'
              const isRefreshingThisAccount = refreshingAccount === account.bankAccountNumber
              return (
                <article
                  className="rounded-xl border border-[#e8eee9] bg-white p-4 sm:p-5"
                  key={account.id ?? account.bankAccountNumber}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex min-w-0 items-center gap-3">
                      <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-[#edf6ef] text-[#43805a]">
                        <Building2 size={18} />
                      </span>
                      <div className="min-w-0">
                        <h3 className="m-0 truncate text-sm font-semibold text-[#344138]">
                          {account.bankName}
                        </h3>
                        <p className="mb-0 mt-1 text-xs text-[#7a867e]">
                          {maskAccountNumber(account.bankAccountNumber)}
                        </p>
                      </div>
                    </div>
                    <span
                      className={`shrink-0 rounded-full px-2.5 py-1 text-[10px] font-semibold ${
                        isActive
                          ? 'bg-[#eaf5ed] text-[#34764a]'
                          : 'bg-[#f2f3f2] text-[#6f7972]'
                      }`}
                    >
                      {account.status || 'UNKNOWN'}
                    </span>
                  </div>

                  <div className="mt-5 border-t border-[#edf0ed] pt-4">
                    <p className="m-0 text-xs text-[#7a867e]">Available balance</p>
                    <div className="mt-1 flex items-center justify-between gap-3">
                      <p className="m-0 text-2xl font-semibold tracking-tight text-[#23372b]">
                        {visibleBalances
                          ? formatCurrency(account.balance, account.currency)
                          : '••••••'}
                      </p>
                      <button
                        aria-label={`Refresh balance for ${account.bankName}`}
                        className="grid size-9 shrink-0 place-items-center rounded-lg border border-[#dfe7e0] text-[#58665d] hover:bg-[#f8faf8] disabled:opacity-50"
                        disabled={isRefreshingThisAccount}
                        onClick={() => handleRefreshBalance(account)}
                        type="button"
                      >
                        <RefreshCw
                          className={isRefreshingThisAccount ? 'animate-spin' : ''}
                          size={15}
                        />
                      </button>
                    </div>
                    <p className="mb-0 mt-1 text-[11px] text-[#929b95]">{account.currency}</p>
                  </div>

                  <div className="mt-4 grid grid-cols-2 gap-2">
                    <button
                      className="inline-flex items-center justify-center gap-2 rounded-lg border border-[#dfe7e0] px-3 py-2.5 text-xs font-semibold text-[#34764a] hover:bg-[#f4faf5] disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={!isActive}
                      onClick={() => startAccountAction(account, 'deposit')}
                      type="button"
                    >
                      <ArrowDownLeft size={15} />
                      Add funds
                    </button>
                    <button
                      className="inline-flex items-center justify-center gap-2 rounded-lg border border-[#dfe7e0] px-3 py-2.5 text-xs font-semibold text-[#58665d] hover:bg-[#f8faf8] disabled:cursor-not-allowed disabled:opacity-50"
                      disabled={!isActive || Number(account.balance) <= 0}
                      onClick={() => startAccountAction(account, 'withdraw')}
                      type="button"
                    >
                      <ArrowUpRight size={15} />
                      Withdraw
                    </button>
                  </div>
                </article>
              )
            })}
          </div>
        )}
      </section>
    </div>
  )
}

export default BankAccountsPanel
