import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowRight, Eye, EyeOff, LockKeyhole, ShieldCheck } from 'lucide-react'
import BrandPanel, { PayFlowBrand } from '../components/BrandPanel.jsx'
import { getCurrentUser, login, signup } from '../services/authService.js'

const fieldClassName = 'mb-[19px] block text-xs font-semibold text-[#27332c]'
const inputClassName =
  'h-[47px] w-full rounded-lg border border-[#e0e6e1] bg-white/80 px-[13px] text-[13px] font-normal text-[#1e2d24] outline-none transition-[border-color,box-shadow] duration-150 placeholder:text-[#a2aaa5] focus:border-[#66a47c] focus:shadow-[0_0_0_3px_rgba(76,143,96,0.12)]'
const focusRingClassName =
  'focus-visible:outline focus-visible:outline-[3px] focus-visible:outline-offset-2 focus-visible:outline-[#408b58]/35'

function AuthPage({ mode, onAuthenticated }) {
  const navigate = useNavigate()
  const isSignup = mode === 'signup'
  const [showPassword, setShowPassword] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [message, setMessage] = useState(null)

  async function handleSubmit(event) {
    event.preventDefault()
    setIsSubmitting(true)
    setMessage(null)

    const formData = new FormData(event.currentTarget)

    try {
      if (isSignup) {
        await signup({
          name: formData.get('name'),
          email: formData.get('email'),
          password: formData.get('password'),
        })
        setMessage({
          type: 'success',
          text: 'Your account is ready. Sign in to start using PayFlow.',
        })
        return
      }

      await login({
        email: formData.get('email'),
        password: formData.get('password'),
      })

      const currentUser = await getCurrentUser()
      if (!currentUser) {
        throw new Error('Sign-in succeeded, but your session could not be verified.')
      }

      onAuthenticated(currentUser)
      navigate('/', { replace: true })
    } catch (error) {
      setMessage({
        type: 'error',
        text:
          error instanceof Error
            ? error.message
            : 'Something went wrong. Please try again.',
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="grid min-h-screen grid-cols-[minmax(380px,0.92fr)_minmax(480px,1.08fr)] bg-white max-[760px]:block">
      <BrandPanel />

      <section className="flex min-w-0 items-center justify-center bg-gradient-to-br from-[#f4f9f5] via-white to-white px-10 py-10 max-[760px]:min-h-screen max-[760px]:flex-col max-[760px]:items-start max-[760px]:px-6 max-[760px]:py-[25px] max-[420px]:px-5">
        <div className="mx-auto hidden w-full max-w-[440px] max-[760px]:block">
          <PayFlowBrand />
        </div>

        <div className="my-auto w-full max-w-[392px] max-[760px]:my-auto">
          <div className="mb-[13px] flex items-center gap-[9px] text-[10px] font-bold tracking-[1.35px] text-[#38815a]">
            {isSignup ? 'GET STARTED' : 'WELCOME BACK'}
          </div>
          <h2 className="m-0 text-[30px] font-semibold leading-[1.2] tracking-[-1.3px] text-[#17231d] max-[420px]:text-[27px]">
            {isSignup ? 'Create your account' : 'Sign in to PayFlow'}
          </h2>
          <p className="mb-[30px] mt-[10px] text-[13px] leading-[1.6] text-[#727c76]">
            {isSignup
              ? 'Create an account and take control of your money.'
              : 'Enter your details to access your account.'}
          </p>

          <form onSubmit={handleSubmit}>
            {isSignup && (
              <label className={fieldClassName}>
                <span className="mb-2 block">Full name</span>
                <input
                  autoComplete="name"
                  className={inputClassName}
                  name="name"
                  placeholder="e.g. Venkatesh"
                  required
                />
              </label>
            )}

            <label className={fieldClassName}>
              <span className="mb-2 block">Email address</span>
              <input
                autoComplete="email"
                className={inputClassName}
                name="email"
                placeholder="you@example.com"
                required
                type="email"
              />
            </label>

            <div className={fieldClassName}>
              <label className="mb-2 block" htmlFor="auth-password">Password</label>
              <span className="relative block">
                <LockKeyhole className="absolute left-[13px] top-[15px] text-[#929b95]" size={17} />
                <input
                  autoComplete={isSignup ? 'new-password' : 'current-password'}
                  className={`${inputClassName} pr-11 pl-[39px]`}
                  id="auth-password"
                  minLength={isSignup ? 8 : undefined}
                  name="password"
                  placeholder={isSignup ? 'At least 8 characters' : 'Enter your password'}
                  required
                  type={showPassword ? 'text' : 'password'}
                />
                <button
                  aria-label={showPassword ? 'Hide password' : 'Show password'}
                  className={`absolute right-0 top-0 grid h-[47px] w-[43px] place-items-center border-0 bg-transparent text-[#78847d] ${focusRingClassName}`}
                  onClick={() => setShowPassword((visible) => !visible)}
                  type="button"
                >
                  {showPassword ? <EyeOff size={17} /> : <Eye size={17} />}
                </button>
              </span>
            </div>

            {message && (
              <p
                className={`my-[1px] mb-4 rounded-[7px] border px-3 py-[11px] text-xs leading-[1.5] ${
                  message.type === 'success'
                    ? 'border-[#d4e9d9] bg-[#f3faf4] text-[#286b3d]'
                    : 'border-[#f1d2cf] bg-[#fff7f6] text-[#9d3933]'
                }`}
                role="status"
              >
                {message.text}
              </p>
            )}

            <button
              className={`mt-[6px] flex h-12 w-full items-center justify-center gap-[9px] rounded-lg border-0 bg-[#286c45] text-[13px] font-semibold text-white shadow-[0_5px_12px_rgba(37,101,65,0.13)] transition-[background-color,transform] duration-150 hover:-translate-y-px hover:bg-[#205b39] disabled:cursor-wait disabled:opacity-70 ${focusRingClassName}`}
              disabled={isSubmitting}
              type="submit"
            >
              {isSubmitting
                ? 'Please wait…'
                : isSignup
                  ? 'Create account'
                  : 'Sign in'}
              {!isSubmitting && <ArrowRight size={17} />}
            </button>
          </form>

          <p className="mt-[23px] text-center text-xs text-[#747d77]">
            {isSignup ? 'Already have an account?' : 'New to PayFlow?'}{' '}
            <Link
              className={`font-semibold text-[#287147] hover:underline hover:underline-offset-[3px] ${focusRingClassName}`}
              to={isSignup ? '/login' : '/signup'}
            >
              {isSignup ? 'Sign in' : 'Create an account'}
            </Link>
          </p>

          <div className="mt-8 flex items-center justify-center gap-[7px] text-[10px] text-[#929b95]">
            <ShieldCheck className="text-[#699777]" size={15} />
            Your information is encrypted and secure
          </div>
        </div>
      </section>
    </main>
  )
}

export default AuthPage
