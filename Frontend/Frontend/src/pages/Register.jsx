import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { ArrowLeft, UserPlus, Eye, EyeOff, CreditCard, Info, CheckCircle2 } from 'lucide-react'

export default function Register() {
  const navigate = useNavigate()
  const { register } = useAuth()
  
  // ---> CHANGED: Added all payment fields to initial state so they don't get lost
  const [formData, setFormData] = useState({
    firstName: '', lastName: '', email: '', phone: '', password: '', role: 'patient',
    cardholderName: '', creditCardNumber: '', expirationDate: '', cvv: '', billingZip: ''
  })
  const [error, setError] = useState('')
  const [visible, setVisible] = useState(false)

  const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const result = await register(formData)
    if (result.error) {
      setError(result.error)
    } else {
      navigate('/patient')
    }
  }

  return (
    <div className="min-h-screen bg-[#047857] flex items-center justify-center py-12 px-4 relative overflow-hidden">
      
      {/* Gradient Overlay */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,_var(--tw-gradient-stops))] from-emerald-500/40 via-[#047857] to-[#065F46]" />
      
      <div className="w-full max-w-5xl flex flex-col relative z-10">
        
        <button 
          onClick={() => navigate('/')}
          className="mb-6 flex items-center gap-2 text-white/70 hover:text-white transition-colors text-[10px] font-bold uppercase tracking-[0.2em] self-start"
        >
          <ArrowLeft size={14} /> Return to Main Site
        </button>

        <div className="bg-white rounded-[48px] shadow-[0_40px_80px_-15px_rgba(0,0,0,0.3)] flex flex-col lg:flex-row overflow-hidden border border-white/20">          
          {/* Left Panel: Deep Emerald */}
          <div className="lg:w-[40%] bg-[#065F46] p-12 text-white relative overflow-hidden flex flex-col justify-between">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_bottom_left,_var(--tw-gradient-stops))] from-emerald-400/30 via-[#065F46] to-[#064E3B]" />
            
            <div className="relative z-10">
              <div className="w-16 h-16 bg-white/10 backdrop-blur-xl rounded-2xl flex items-center justify-center mb-8">
                <UserPlus className="w-8 h-8 text-white" />
              </div>
              <h1 className="text-4xl font-bold tracking-tight mb-4">Join the family.</h1>
              <p className="text-emerald-50 text-sm leading-relaxed mb-10 opacity-80">
                Experience the next generation of healthcare management. Secure, fast, and patient-first.
              </p>
              
              <ul className="space-y-4">
                {['Direct Doctor Access', 'Real-time Booking', 'Secure Medical Records'].map((item) => (
                  <li key={item} className="flex items-center gap-3 text-xs font-medium text-emerald-50">
                    <CheckCircle2 size={16} className="text-emerald-300" /> {item}
                  </li>
                ))}
              </ul>
            </div>

            <div className="relative z-10 pt-12 border-t border-white/10">
              <p className="text-[10px] uppercase tracking-widest font-bold opacity-50 text-emerald-100">Trusted by 10k+ Patients</p>
            </div>
          </div>

          {/* Right Side: Form */}
          <div className="lg:w-[60%] p-10 lg:p-14 bg-white/95 backdrop-blur-sm">
            <div className="mb-10">
              <h2 className="text-2xl font-bold text-slate-800">Create Account</h2>
              <p className="text-slate-400 text-xs mt-1">Fill in your details to get started.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 ml-1">First Name</label>
                  <input name="firstName" type="text" placeholder="John" onChange={handleChange} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-bold text-slate-700 outline-none focus:ring-4 focus:ring-emerald-500/10 focus:border-[#047857] transition-all" required />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 ml-1">Last Name</label>
                  <input name="lastName" type="text" placeholder="Doe" onChange={handleChange} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-bold text-slate-700 outline-none focus:ring-4 focus:ring-emerald-500/10 focus:border-[#047857] transition-all" required />
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 ml-1">Email</label>
                  <input name="email" type="email" placeholder="john@example.com" onChange={handleChange} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-bold text-slate-700 outline-none focus:ring-4 focus:ring-emerald-500/10 focus:border-[#047857] transition-all" required />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 ml-1">Phone</label>
                  <input name="phone" type="tel" placeholder="+1 555-0123" onChange={handleChange} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-bold text-slate-700 outline-none focus:ring-4 focus:ring-emerald-500/10 focus:border-[#047857] transition-all" required />
                </div>
              </div>

              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-widest mb-1.5 ml-1">Password</label>
                <div className="relative">
                  <input name="password" type={visible ? "text" : "password"} placeholder="••••••••" onChange={handleChange} className="w-full bg-slate-50 border border-slate-200 rounded-xl px-4 py-3 text-sm font-bold text-slate-700 outline-none focus:ring-4 focus:ring-emerald-500/10 focus:border-[#047857] transition-all" required />
                  <button type="button" onClick={() => setVisible(!visible)} className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-300 hover:text-[#047857] transition-colors">
                    {visible ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              <div className="pt-6 border-t border-slate-100">
                <div className="flex items-center gap-2 mb-4">
                  <CreditCard size={14} className="text-slate-400" />
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Payment (Optional)</span>
                </div>
                
                <div className="bg-emerald-50/50 rounded-2xl p-4 mb-4 flex gap-3 border border-emerald-100/50">
                  <Info size={14} className="text-[#047857] shrink-0 mt-0.5" />
                  <p className="text-[10px] text-slate-500 leading-tight">Linking a card is required for booking. You can also do this later in Profile Settings.</p>
                </div>

                <div className="grid grid-cols-3 gap-3">
                  {/* ---> CHANGED: Added name, onChange, and the missing creditCardNumber input */}
                  <input name="cardholderName" onChange={handleChange} type="text" placeholder="Name on Card" className="col-span-3 bg-slate-50 border border-slate-100 rounded-xl px-4 py-2.5 text-sm font-bold text-slate-700 outline-none focus:bg-white transition-all" />
                  <input name="creditCardNumber" onChange={handleChange} type="text" placeholder="Card Number" className="col-span-3 bg-slate-50 border border-slate-100 rounded-xl px-4 py-2.5 text-sm font-bold text-slate-700 outline-none focus:bg-white transition-all" />
                  <input name="expirationDate" onChange={handleChange} type="text" placeholder="MM/YY" className="bg-slate-50 border border-slate-100 rounded-xl px-4 py-2.5 text-sm font-bold text-slate-700 outline-none focus:bg-white transition-all" />
                  <input name="cvv" onChange={handleChange} type="text" placeholder="CVV" className="bg-slate-50 border border-slate-100 rounded-xl px-4 py-2.5 text-sm font-bold text-slate-700 outline-none focus:bg-white transition-all" />
                  <input name="billingZip" onChange={handleChange} type="text" placeholder="Zip" className="bg-slate-50 border border-slate-100 rounded-xl px-4 py-2.5 text-sm font-bold text-slate-700 outline-none focus:bg-white transition-all" />
                </div>
              </div>

              {error && <div className="text-red-500 text-xs font-bold text-center bg-red-50 p-3 rounded-xl border border-red-100">{error}</div>}

              <div className="flex flex-col gap-4 pt-4">
                <button type="submit" className="w-full bg-[#047857] hover:bg-[#065F46] text-white font-bold py-4 rounded-2xl shadow-xl shadow-emerald-900/20 transition-all active:scale-[0.98]">
                  Register Account
                </button>
                <p className="text-center text-slate-500 text-sm">
                  Already have an account? <Link to="/login" className="text-[#047857] font-bold hover:underline">Sign in</Link>
                </p>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}