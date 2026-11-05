import { useEffect, useState } from 'react'
import { ArrowRight, CheckCircle2, LoaderCircle, ShieldCheck } from 'lucide-react'
import { getBankAccounts, transferPayment } from '../services/transactionService.js'

const fieldClassName =
  'mt-1.5 w-full rounded-lg border border-[#dfe7e0] bg-white px-3 py-2.5 text-sm text-[#344138] outline-none focus:border-[#68a17a] focus:ring-2 focus:ring-[#68a17a]/15'

function formatCurrency(value, currency) {
  const amount = Number(value)
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency,
  }).format(amount)
}

function displayError(error) {
  return error instanceof Error ? error.message : 'Unable to send payment. Please try again.'
}

function maskedAccountNumber(accountNumber) {
  return `•••• ${String(accountNumber).slice(-4)}`
}

function PaymentsPanel() {
  const [accounts, setAccounts] = useState([])
  const [selectedAccountNumber, setSelectedAccountNumber] = useState('')
  const [recipientEmail, setRecipientEmail] = useState('')
  const [amount, setAmount] = useState('')
  const [isLoadingAccounts, setIsLoadingAccounts] = useState(true)
  const [isSending, setIsSending] = useState(false)
  const [isReviewing, setIsReviewing] = useState(false)
  const [error, setError] = useState('')
  const [idempotencyKey, setIdempotencyKey] = useState('')
  const [completedTransfer, setCompletedTransfer] = useState(null)

  useEffect(() => {
    let isCurrent = true

    getBankAccounts()
      .then((result) => {
        if (!Array.isArray(result)) {
          throw new Error('The bank accounts response was not a list.')
        }
        if (isCurrent) {
          const activeAccounts = result.filter((account) => account.status === 'ACTIVE')
          setAccounts(activeAccounts)
          setSelectedAccountNumber(activeAccounts[0]?.bankAccountNumber ?? '')
        }
      })
      .catch((requestError) => {
        if (isCurrent) setError(displayError(requestError))
      })
      .finally(() => {
        if (isCurrent) setIsLoadingAccounts(false)
      })

    return () => {
      isCurrent = false
    }
  }, [])

  const selectedAccount = accounts.find(
    (account) => account.bankAccountNumber === selectedAccountNumber,
  )

  function handleReview(event) {
    event.preventDefault()
    setError('')

    if (!selectedAccount) {
      setError('Choose an active account to pay from.')
      return
    }

    if (Number(amount) > Number(selectedAccount.balance)) {
      setError('The payment amount is greater than your available balance.')
      return
    }

    setIdempotencyKey(crypto.randomUUID())
    setIsReviewing(true)
  }

  async function handleConfirmPayment() {
    if (!selectedAccount || !idempotencyKey) return

    setIsSending(true)
    setError('')

    try {
      const result = await transferPayment(
        {
          sourceBankAccountNumber: selectedAccount.bankAccountNumber,
          recipientEmail: recipientEmail.trim(),
          amount: Number(amount),
          currency: selectedAccount.currency,
        },
        idempotencyKey,
      )
      setCompletedTransfer(result)
      setIsReviewing(false)
    } catch (requestError) {
      setError(displayError(requestError))
    } finally {
      setIsSending(false)
    }
  }

  function handleEditPayment() {
    setError('')
    setIsReviewing(false)
    setIdempotencyKey('')
  }

  function handleNewPayment() {
    setCompletedTransfer(null)
    setRecipientEmail('')
    setAmount('')
    setError('')
    setIsReviewing(false)
    setIdempotencyKey('')
  }

  if (completedTransfer) {
    return (
      <section className="rounded-2xl border border-[#e7ece8] bg-white p-6 shadow-[0_2px_10px_rgba(31,51,38,0.025)] sm:p-8">
        <div className="mx-auto flex max-w-lg flex-col items-center py-8 text-center">
          <span className="mb-4 grid size-14 place-items-center rounded-full bg-[#e9f5ed] text-[#287147]">
            <CheckCircle2 size={28} />
          </span>
          <h2 className="m-0 text-xl font-semibold text-[#28372e]">Payment sent</h2>
          <p className="mb-0 mt-2 text-sm text-[#7a867e]">
            {formatCurrency(completedTransfer.amount, completedTransfer.currency)} sent to{' '}
            {recipientEmail}.
          </p>
          <p className="mb-0 mt-3 text-xs text-[#929b95]">
            Transaction ID: {completedTransfer.transactionId}
          </p>
          <button
            className="mt-7 rounded-lg bg-[#286c45] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#205b39]"
            onClick={handleNewPayment}
            type="button"
          >
            Make another payment
          </button>
        </div>
      </section>
    )
  }

  if (isLoadingAccounts) {
    return (
      <section className="grid min-h-[250px] place-items-center rounded-2xl border border-[#e7ece8] bg-white text-sm text-[#7a867e]">
        Loading your accounts…
      </section>
    )
  }

  if (!accounts.length) {
    return (
      <section className="rounded-2xl border border-[#e7ece8] bg-white p-8 text-center shadow-[0_2px_10px_rgba(31,51,38,0.025)]">
        <h2 className="m-0 text-base font-semibold text-[#28372e]">No active bank accounts</h2>
        <p className="mb-0 mt-2 text-sm text-[#7a867e]">
          Link an active PayFlow account before sending a payment.
        </p>
        {error && (
          <p className="mb-0 mt-4 text-sm text-[#9d3933]" role="alert">
            {error}
          </p>
        )}
      </section>
    )
  }

  return (
    <section className="overflow-hidden rounded-2xl border border-[#e7ece8] bg-white shadow-[0_2px_10px_rgba(31,51,38,0.025)]">
      <div className="border-b border-[#edf0ed] px-5 py-5 sm:px-7">
        <div className="flex items-center gap-3">
          <span className="grid size-10 place-items-center rounded-xl bg-[#e9f5ed] text-[#287147]">
            <ArrowRight size={19} />
          </span>
          <div>
            <h2 className="m-0 text-base font-semibold text-[#28372e]">
              {isReviewing ? 'Review payment' : 'Send a payment'}
            </h2>
            <p className="mb-0 mt-1 text-xs text-[#7a867e]">
              {isReviewing
                ? 'Check the details before confirming.'
                : 'Send money securely to another PayFlow member.'}
            </p>
          </div>
        </div>
      </div>

      {error && (
        <p
          className="mx-5 mt-5 rounded-lg border border-[#f1d2cf] bg-[#fff7f6] px-3 py-2.5 text-sm text-[#9d3933] sm:mx-7"
          role="alert"
        >
          {error}
        </p>
      )}

      {isReviewing ? (
        <div className="space-y-5 px-5 py-6 sm:px-7 sm:py-7">
          <dl className="m-0 divide-y divide-[#edf0ed] rounded-xl border border-[#edf0ed] px-4">
            <div className="flex justify-between gap-4 py-3.5 text-sm">
              <dt className="text-[#7a867e]">To</dt>
              <dd className="m-0 break-all text-right font-medium text-[#344138]">{recipientEmail}</dd>
            </div>
            <div className="flex justify-between gap-4 py-3.5 text-sm">
              <dt className="text-[#7a867e]">From</dt>
              <dd className="m-0 text-right font-medium text-[#344138]">
                {selectedAccount.bankName} · {maskedAccountNumber(selectedAccount.bankAccountNumber)}
              </dd>
            </div>
            <div className="flex justify-between gap-4 py-3.5 text-sm">
              <dt className="text-[#7a867e]">Amount</dt>
              <dd className="m-0 text-right font-semibold text-[#28372e]">
                {formatCurrency(amount, selectedAccount.currency)}
              </dd>
            </div>
          </dl>
          <div className="flex flex-col-reverse justify-end gap-3 sm:flex-row">
            <button
              className="rounded-lg border border-[#dfe7e0] px-4 py-2.5 text-sm font-semibold text-[#58665d] hover:bg-[#f8faf8] disabled:opacity-60"
              disabled={isSending}
              onClick={handleEditPayment}
              type="button"
            >
              Edit details
            </button>
            <button
              className="inline-flex items-center justify-center gap-2 rounded-lg bg-[#286c45] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#205b39] disabled:cursor-wait disabled:opacity-70"
              disabled={isSending}
              onClick={handleConfirmPayment}
              type="button"
            >
              {isSending && <LoaderCircle className="animate-spin" size={16} />}
              {isSending ? 'Sending…' : 'Confirm and send'}
            </button>
          </div>
        </div>
      ) : (
        <form className="grid gap-5 px-5 py-6 sm:grid-cols-2 sm:px-7 sm:py-7" onSubmit={handleReview}>
          <label className="block text-xs font-semibold text-[#46544b] sm:col-span-2">
            Recipient email
            <input
              autoComplete="email"
              className={fieldClassName}
              onChange={(event) => setRecipientEmail(event.target.value)}
              placeholder="name@example.com"
              required
              type="email"
              value={recipientEmail}
            />
            <span className="mt-1.5 block font-normal text-[#929b95]">
              The recipient needs exactly one active PayFlow account in the same currency.
            </span>
          </label>

          <label className="block text-xs font-semibold text-[#46544b] sm:col-span-2">
            Pay from
            <select
              className={fieldClassName}
              onChange={(event) => setSelectedAccountNumber(event.target.value)}
              required
              value={selectedAccountNumber}
            >
              {accounts.map((account) => (
                <option key={account.id} value={account.bankAccountNumber}>
                  {account.bankName} · {maskedAccountNumber(account.bankAccountNumber)} ·{' '}
                  {formatCurrency(account.balance, account.currency)}
                </option>
              ))}
            </select>
          </label>

          <label className="block text-xs font-semibold text-[#46544b]">
            Amount ({selectedAccount?.currency})
            <input
              className={fieldClassName}
              min="0.01"
              onChange={(event) => setAmount(event.target.value)}
              required
              step="0.01"
              type="number"
              value={amount}
            />
          </label>

          <div className="flex items-end text-xs text-[#7a867e]">
            <p className="m-0 pb-2.5">
              Available balance:{' '}
              <span className="font-semibold text-[#46544b]">
                {selectedAccount
                  ? formatCurrency(selectedAccount.balance, selectedAccount.currency)
                  : '—'}
              </span>
            </p>
          </div>

          <div className="flex items-center gap-2 text-[11px] text-[#7a867e] sm:col-span-2">
            <ShieldCheck className="shrink-0 text-[#43805a]" size={15} />
            You’ll review the recipient and amount before the transfer is sent.
          </div>

          <div className="flex justify-end sm:col-span-2">
            <button
              className="inline-flex items-center gap-2 rounded-lg bg-[#286c45] px-4 py-2.5 text-sm font-semibold text-white hover:bg-[#205b39] disabled:cursor-not-allowed disabled:opacity-50"
              disabled={!selectedAccount || !recipientEmail || !amount}
              type="submit"
            >
              Review payment
              <ArrowRight size={16} />
            </button>
          </div>
        </form>
      )}
    </section>
  )
}

export default PaymentsPanel
