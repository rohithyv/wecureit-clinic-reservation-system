import { useState } from 'react'
import { Link } from 'react-router-dom'
import { ArrowRight, Search } from 'lucide-react'
import { getDoctors, deleteDoctor } from '../../services/api'
import { formatDoctorLicenseSummary, SPECIALIZATIONS } from '../../data/constants'
import PageShell, { PageBackLink } from '../../components/PageShell'

export default function AdminDoctors() {
  const [refresh, setRefresh] = useState(0)
  const [query, setQuery] = useState('')
  const [specFilter, setSpecFilter] = useState('')
  const doctors = getDoctors()

  const q = query.trim().toLowerCase()
  const filtered = doctors.filter((doc) => {
    const hay = `${doc.name || ''} ${doc.qualification || ''} ${formatDoctorLicenseSummary(doc)}`.toLowerCase()
    const matchesText = !q || hay.includes(q)
    const matchesSpec = !specFilter || doc.specializationIds?.includes(specFilter)
    return matchesText && matchesSpec
  })

  const handleDeleteDoctor = (doctorId, name) => {
    if (!window.confirm(`Delete doctor "${name}"? This will remove all their chambers and cancel their appointments.`)) return
    deleteDoctor(doctorId)
    setRefresh((r) => r + 1)
  }

  return (
    <PageShell
      eyebrow="Directory"
      title="Manage doctors"
      subtitle="Search and filter the provider list. Availability is managed by each doctor from their own portal."
      heroSize="compact"
      contentMax="max-w-5xl"
      tone="blue"
      actions={
        <Link
          to="/admin/doctors/new"
          className="group inline-flex items-center gap-2 bg-white text-blue-700 text-xs font-black uppercase tracking-[0.12em] px-8 py-4 rounded-2xl shadow-xl shadow-blue-900/25 hover:bg-blue-50 transition-all"
        >
          Add doctor
          <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
        </Link>
      }
    >
      <PageBackLink to="/admin">Admin dashboard</PageBackLink>

      <div className="flex flex-col sm:flex-row gap-3 mb-10">
        <div className="flex-1 flex items-center gap-3 px-4 py-3 bg-white border border-slate-100 rounded-2xl shadow-sm">
          <Search className="w-5 h-5 text-slate-400 shrink-0" />
          <input
            type="search"
            placeholder="Search by name, credentials, or license…"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="w-full bg-transparent border-none focus:ring-0 text-sm font-bold text-slate-800 placeholder:text-slate-400"
          />
        </div>
        <select
          value={specFilter}
          onChange={(e) => setSpecFilter(e.target.value)}
          className="input-field sm:w-56 font-bold text-sm"
        >
          <option value="">All specialties</option>
          {SPECIALIZATIONS.map((s) => (
            <option key={s.id} value={s.id}>
              {s.name}
            </option>
          ))}
        </select>
      </div>

      <section>
        <h2 className="section-kicker mb-5 px-1">Doctors ({filtered.length})</h2>
        {filtered.length === 0 ? (
          <div className="card p-12 text-center text-slate-500 font-medium rounded-[2rem]">
            No doctors match your search.
          </div>
        ) : (
          <ul className="space-y-4">
            {filtered.map((doc) => (
              <li
                key={doc.id}
                className="card-interactive p-6 md:p-7 flex flex-wrap items-center justify-between gap-6 rounded-[1.75rem]"
              >
                <div>
                  <p className="font-medium text-slate-800">{doc.name}</p>
                  <p className="text-sm text-slate-600">{doc.qualification}</p>
                  <p className="text-xs text-slate-500 mt-2 leading-relaxed max-w-xl">
                    Licenses: {formatDoctorLicenseSummary(doc)}
                  </p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Link to={`/admin/doctors/${doc.id}/edit`} className="btn-secondary text-sm">
                    Edit
                  </Link>
                  <button
                    type="button"
                    onClick={() => handleDeleteDoctor(doc.id, doc.name)}
                    className="btn-secondary text-sm text-red-600 border-red-100 hover:bg-red-50"
                  >
                    Delete
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>
    </PageShell>
  )
}
