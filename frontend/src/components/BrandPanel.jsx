import { Link } from 'react-router-dom'
import { ShieldCheck, WalletCards } from 'lucide-react'

export function PayFlowBrand() {
  return (
    <Link
      className="relative z-10 inline-flex w-fit items-center gap-2.5 text-[21px] font-semibold tracking-[-1.1px] text-inherit no-underline"
      to="/login"
      aria-label="PayFlow home"
    >
      <span className="grid size-[35px] place-items-center rounded-[11px] border border-white/15 bg-white/[0.07] text-[#b7eccb]">
        <WalletCards size={21} strokeWidth={2.2} />
      </span>
      <span>payflow</span>
    </Link>
  )
}

export default function BrandPanel() {
  return (
    <aside className="relative flex min-h-screen flex-col justify-between overflow-hidden bg-gradient-to-br from-[#15382f] via-[#102c27] to-[#0e2924] px-[9.2%] py-[38px] text-[#f8fafc] after:absolute after:-right-[190px] after:bottom-[13%] after:size-[470px] after:pointer-events-none after:rounded-full after:border after:border-[#c4eed4]/[0.09] after:shadow-[0_0_0_54px_rgba(196,238,212,0.03),0_0_0_108px_rgba(196,238,212,0.02)] after:content-['']">
      <PayFlowBrand />

      <div className="relative z-10 -mt-[25px] max-w-[480px]">
        <div className="flex items-center gap-[9px] text-[10px] font-bold tracking-[1.35px] text-[#8bc6a1]">
          <span className="size-[7px] rounded-full bg-[#86d2a0] shadow-[0_0_13px_rgba(134,210,160,0.7)]" />
          YOUR MONEY, IN MOTION
        </div>
        <h1 className="my-[25px] text-[clamp(48px,5.5vw,72px)] font-medium leading-[0.99] tracking-[-4.8px] text-[#f6f8f5]">
          Make every
          <br />
          move <span className="text-[#96d4a9]">count.</span>
        </h1>
        <p className="m-0 max-w-[375px] text-[15px] leading-[1.75] text-[#aebeb8]">
          A clearer way to manage your money, move it around, and stay in control.
        </p>
        <div className="mt-9 flex items-center gap-[9px] text-xs text-[#d1dfd7]">
          <ShieldCheck className="text-[#92d3a7]" size={18} />
          <span>Bank-level security. Always.</span>
        </div>
      </div>

      <div className="relative z-10 flex justify-between gap-3 text-[9px] font-semibold tracking-[1px] text-[#82968e]">
        <span>PAYMENTS, MADE SIMPLE</span>
        <span className="font-normal tracking-normal">© 2026 PayFlow</span>
      </div>
    </aside>
  )
}
