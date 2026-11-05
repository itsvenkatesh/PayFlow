import { useEffect, useState } from 'react'
import { ChevronLeft, ChevronRight, RotateCcw, Search } from 'lucide-react'
import { getBankAccounts, getTransactions } from '../services/transactionService.js'

const INITIAL_FILTERS = {
  bankAccountNumbers: [],
  directions: [],
  types: [],
  statuses: [],
  fromDate: '',
  toDate: '',
}

const FILTER_GROUPS = [
  { name: 'directions', label: 'Direction', options: ['SENT', 'RECEIVED'] },
  { name: 'types', label: 'Type', options: ['TRANSFER', 'DEPOSIT', 'WITHDRAW'] },
  { name: 'statuses', label: 'Status', options: ['SUCCESS', 'FAILED', 'CANCELED'] },
]

const inputClassName =
  'mt-1.5 w-full rounded-lg border border-[#dfe7e0] bg-white px-3 py-2.5 text-xs text-[#344138] outline-none focus:border-[#68a17a] focus:ring-2 focus:ring-[#68a17a]/15'

function displayError(error) {
  return error instanceof Error ? error.message : 'Unable to load transactions.'
}

function formatDate(value) {
  const date = new Date(value)
  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleString(undefined, {
        dateStyle: 'medium',
        timeStyle: 'short',
      })
}

function formatAmount(value) {
  const amount = Number(value)
  return Number.isFinite(amount)
    ? new Intl.NumberFormat(undefined, {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(amount)
    : String(value)
}

function TransactionFilters({ accounts, filters, onChange }) {
  function toggleValue(field, value) {
    const currentValues = filters[field]
    const nextValues = currentValues.includes(value)
      ? currentValues.filter((item) => item !== value)
      : [...currentValues, value]

    onChange({ ...filters, [field]: nextValues })
  }

  return (
    <div className="grid gap-5 border-b border-[#edf0ed] px-5 py-5 sm:px-6 lg:grid-cols-2 xl:grid-cols-4">
      <label className="block text-xs font-semibold text-[#46544b]">
        Bank accounts
        <select
          className={inputClassName}
          onChange={(event) =>
            onChange({
              ...filters,
              bankAccountNumbers: event.target.value ? [event.target.value] : [],
            })
          }
          value={filters.bankAccountNumbers[0] ?? ''}
        >
          <option value="">All bank accounts</option>
          {accounts.map((account) => (
            <option key={account.id} value={account.bankAccountNumber}>
              {account.bankName} · {account.bankAccountNumber}
            </option>
          ))}
        </select>
      </label>

      {FILTER_GROUPS.map(({ name, label, options }) => (
        <fieldset className="m-0 min-w-0 border-0 p-0" key={name}>
          <legend className="mb-2 text-xs font-semibold text-[#46544b]">{label}</legend>
          <div className="flex flex-wrap gap-x-4 gap-y-2">
            {options.map((option) => (
              <label
                className="inline-flex items-center gap-2 text-xs text-[#66736b]"
                key={option}
              >
                <input
                  checked={filters[name].includes(option)}
                  className="size-3.5 accent-[#286c45]"
                  onChange={() => toggleValue(name, option)}
                  type="checkbox"
                />
                {option.charAt(0) + option.slice(1).toLowerCase()}
              </label>
            ))}
          </div>
        </fieldset>
      ))}

      <label className="block text-xs font-semibold text-[#46544b]">
        From date
        <input
          className={inputClassName}
          max={filters.toDate || undefined}
          onChange={(event) => onChange({ ...filters, fromDate: event.target.value })}
          type="date"
          value={filters.fromDate}
        />
      </label>
      <label className="block text-xs font-semibold text-[#46544b]">
        To date
        <input
          className={inputClassName}
          min={filters.fromDate || undefined}
          onChange={(event) => onChange({ ...filters, toDate: event.target.value })}
          type="date"
          value={filters.toDate}
        />
      </label>
    </div>
  )
}

function TransactionsPanel() {
  const [filters, setFilters] = useState(INITIAL_FILTERS)
  const [appliedFilters, setAppliedFilters] = useState(INITIAL_FILTERS)
  const [accounts, setAccounts] = useState([])
  const [transactionsPage, setTransactionsPage] = useState(null)
  const [pageNumber, setPageNumber] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [accountsError, setAccountsError] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    let isCurrent = true

    getBankAccounts()
      .then((result) => {
        if (!Array.isArray(result)) {
          throw new Error('The bank accounts response was not a list.')
        }
        if (isCurrent) setAccounts(result)
      })
      .catch((requestError) => {
        if (isCurrent) setAccountsError(displayError(requestError))
      })

    return () => {
      isCurrent = false
    }
  }, [])

  useEffect(() => {
    let isCurrent = true

    async function loadTransactions() {
      setIsLoading(true)
      setError('')

      try {
        const result = await getTransactions({
          ...appliedFilters,
          page: pageNumber,
          size: 10,
          sort: 'createdAt,desc',
        })

        if (!result || !Array.isArray(result.content)) {
          throw new Error('The transactions response did not include a content list.')
        }
        if (isCurrent) setTransactionsPage(result)
      } catch (requestError) {
        if (isCurrent) {
          setTransactionsPage(null)
          setError(displayError(requestError))
        }
      } finally {
        if (isCurrent) setIsLoading(false)
      }
    }

    loadTransactions()

    return () => {
      isCurrent = false
    }
  }, [appliedFilters, pageNumber])

  function handleApplyFilters(event) {
    event.preventDefault()
    setPageNumber(0)
    setAppliedFilters({ ...filters })
  }

  function handleResetFilters() {
    setFilters(INITIAL_FILTERS)
    setAppliedFilters(INITIAL_FILTERS)
    setPageNumber(0)
  }

  const transactions = transactionsPage?.content ?? []
  const totalPages = transactionsPage?.totalPages ?? 0
  const totalElements = transactionsPage?.totalElements ?? 0

  return (
    <div className="overflow-hidden rounded-2xl border border-[#e7ece8] bg-white shadow-[0_2px_10px_rgba(31,51,38,0.025)]">
      <form onSubmit={handleApplyFilters}>
        <TransactionFilters accounts={accounts} filters={filters} onChange={setFilters} />
        {accountsError && (
          <p className="mx-5 mt-4 rounded-lg border border-[#f1d2cf] bg-[#fff7f6] px-3 py-2 text-xs text-[#9d3933] sm:mx-6" role="alert">
            Could not load bank accounts for filtering: {accountsError}
          </p>
        )}
        <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-4 sm:px-6">
          <div className="flex items-center gap-2 text-xs text-[#7a867e]">
            <Search size={15} />
            Filter transactions
          </div>
          <div className="flex gap-2">
            <button
              className="inline-flex items-center gap-2 rounded-lg border border-[#dfe7e0] px-3 py-2 text-xs font-medium text-[#58665d] hover:bg-[#f8faf8]"
              onClick={handleResetFilters}
              type="button"
            >
              <RotateCcw size={14} />
              Reset
            </button>
            <button
              className="rounded-lg bg-[#286c45] px-4 py-2 text-xs font-semibold text-white hover:bg-[#205b39] disabled:cursor-wait disabled:opacity-70"
              disabled={isLoading}
              type="submit"
            >
              Apply filters
            </button>
          </div>
        </div>
      </form>

      {error && (
        <p className="mx-5 mb-4 rounded-lg border border-[#f1d2cf] bg-[#fff7f6] px-3 py-2.5 text-xs text-[#9d3933] sm:mx-6" role="alert">
          {error}
        </p>
      )}

      <div className="overflow-x-auto border-t border-[#edf0ed]">
        <table className="w-full min-w-[760px] border-collapse text-left">
          <thead className="bg-[#f8faf8] text-[10px] font-semibold uppercase tracking-[0.8px] text-[#849087]">
            <tr>
              <th className="px-5 py-3.5 sm:px-6">Transaction</th>
              <th className="px-4 py-3.5">Direction</th>
              <th className="px-4 py-3.5">Accounts</th>
              <th className="px-4 py-3.5">Date</th>
              <th className="px-4 py-3.5 text-right">Amount</th>
              <th className="px-5 py-3.5 sm:px-6">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-[#edf0ed]">
            {isLoading ? (
              <tr>
                <td className="px-6 py-12 text-center text-sm text-[#7a867e]" colSpan={6}>
                  Loading transactions…
                </td>
              </tr>
            ) : error ? null : transactions.length === 0 ? (
              <tr>
                <td className="px-6 py-12 text-center text-sm text-[#7a867e]" colSpan={6}>
                  No transactions match these filters.
                </td>
              </tr>
            ) : (
              transactions.map((transaction) => (
                <tr className="text-xs text-[#58665d] hover:bg-[#fbfcfb]" key={transaction.transactionId}>
                  <td className="px-5 py-4 sm:px-6">
                    <p className="m-0 font-semibold text-[#344138]">{transaction.type}</p>
                    <p className="mb-0 mt-1 text-[10px] text-[#929b95]">
                      {transaction.transactionId}
                    </p>
                  </td>
                  <td className="px-4 py-4">
                    {transaction.direction === 'SENT' ? 'Sent' : 'Received'}
                  </td>
                  <td className="px-4 py-4">
                    <p className="m-0">From {transaction.sourceBankAccountNumber || '—'}</p>
                    <p className="mb-0 mt-1">To {transaction.destinationBankAccountNumber || '—'}</p>
                  </td>
                  <td className="px-4 py-4 whitespace-nowrap">{formatDate(transaction.createdAt)}</td>
                  <td className="px-4 py-4 text-right font-semibold text-[#29372f]">
                    {transaction.direction === 'SENT' ? '−' : '+'}
                    {formatAmount(transaction.amount)}
                  </td>
                  <td className="px-5 py-4 sm:px-6">
                    <span
                      className={`inline-flex rounded-full px-2.5 py-1 text-[10px] font-semibold ${
                        transaction.status === 'SUCCESS'
                          ? 'bg-[#eaf5ed] text-[#34764a]'
                          : transaction.status === 'FAILED'
                            ? 'bg-[#fff0ef] text-[#a1433c]'
                            : 'bg-[#f2f3f2] text-[#6f7972]'
                      }`}
                    >
                      {transaction.status}
                    </span>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3 border-t border-[#edf0ed] px-5 py-4 sm:px-6">
        <p className="m-0 text-xs text-[#7a867e]">
          {isLoading
            ? 'Loading results…'
            : `${totalElements} transaction${totalElements === 1 ? '' : 's'} found`}
        </p>
        <div className="flex items-center gap-3">
          <span className="text-xs text-[#7a867e]">
            Page {totalPages === 0 ? 0 : pageNumber + 1} of {totalPages}
          </span>
          <button
            aria-label="Previous page"
            className="grid size-8 place-items-center rounded-lg border border-[#dfe7e0] text-[#58665d] hover:bg-[#f8faf8] disabled:cursor-not-allowed disabled:opacity-40"
            disabled={isLoading || pageNumber === 0}
            onClick={() => setPageNumber((current) => Math.max(0, current - 1))}
            type="button"
          >
            <ChevronLeft size={16} />
          </button>
          <button
            aria-label="Next page"
            className="grid size-8 place-items-center rounded-lg border border-[#dfe7e0] text-[#58665d] hover:bg-[#f8faf8] disabled:cursor-not-allowed disabled:opacity-40"
            disabled={isLoading || totalPages === 0 || pageNumber + 1 >= totalPages}
            onClick={() => setPageNumber((current) => current + 1)}
            type="button"
          >
            <ChevronRight size={16} />
          </button>
        </div>
      </div>
    </div>
  )
}

export default TransactionsPanel
