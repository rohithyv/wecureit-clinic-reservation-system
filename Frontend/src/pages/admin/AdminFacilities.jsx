import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Building2, Search } from 'lucide-react'
import { getFacilities, deleteFacility, getChambersByFacilityId } from '../../services/api'
import { MOCK_STATES } from '../../data/constants'
import PageShell, { PageBackLink } from '../../components/PageShell'

export default function AdminFacilities() {
  const [, setRefresh] = useState(0)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [selectedState, setSelectedState] = useState('')
  const facilities = getFacilities()
  const filteredFacilities = useMemo(() => {
    const q = query.trim().toLowerCase()
    return facilities.filter((fac) => {
      const matchesState = !selectedState || fac.state === selectedState
      const haystack = `${fac.name || ''} ${fac.address || ''} ${fac.city || ''} ${fac.zipCode || ''}`.toLowerCase()
      const matchesQuery = !q || haystack.includes(q)
      return matchesState && matchesQuery
    })
  }, [facilities, query, selectedState])

  const handleDelete = (id, name) => {
    const chambers = getChambersByFacilityId(id)
    if (chambers.length > 0) {
      setError(`Access denied: This facility is tied to ${chambers.length} active doctor schedules.`)
      return
    }
    if (!window.confirm(`Permanently remove "${name}"?`)) return
    deleteFacility(id)
    setRefresh((r) => r + 1)
  }

  return (
    <PageShell
      eyebrow="Admin"
      title="Facility management"
      subtitle="Manage physical locations and their medical speciality support."
      tone="blue"
      actions={
        <Link
          to="/admin/facilities/new"
          className="group inline-flex items-center gap-2 bg-white text-blue-700 text-xs font-black uppercase tracking-[0.12em] px-8 py-4 rounded-2xl shadow-xl shadow-blue-900/25 hover:bg-blue-50 transition-all hover:-translate-y-0.5 active:scale-95"
        >
          <Building2 className="w-4 h-4" />
          Add facility
        </Link>
      }
    >
      <PageBackLink to="/admin">Admin dashboard</PageBackLink>

      {error && (
        <div className="bg-red-50 border border-red-100 text-red-500 px-4 py-3 rounded-xl mb-6 text-xs font-bold">
          {error}
        </div>
      )}

      <div className="bg-white border border-slate-100 rounded-3xl p-3 shadow-sm ring-1 ring-slate-100 mb-8 flex flex-col lg:flex-row items-stretch gap-3">
        <div className="flex-1 flex items-center px-4 py-3 bg-slate-50 rounded-2xl border border-slate-100">
          <select
            value={selectedState}
            onChange={(e) => setSelectedState(e.target.value)}
            className="w-full bg-transparent border-none focus:ring-0 text-slate-700 font-bold text-sm"
          >
            <option value="">All regions</option>
            {MOCK_STATES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>
        <div className="flex-[1.5] flex items-center gap-3 px-4 py-3 bg-slate-50 rounded-2xl border border-slate-100">
          <Search className="w-4 h-4 text-slate-400 shrink-0" aria-hidden />
          <input
            type="search"
            placeholder="Search facilities by name, city, address..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="w-full bg-transparent border-none focus:ring-0 text-slate-700 font-bold text-sm placeholder-slate-400"
            autoComplete="off"
          />
        </div>
      </div>

      <div className="grid gap-3">
        {filteredFacilities.length === 0 ? (
          <div className="bg-white rounded-2xl border border-slate-100 p-12 text-center text-slate-400 text-sm italic card">
            No facilities match your search/filter.
          </div>
        ) : (
          filteredFacilities.map((fac) => (
            <div
              key={fac.id}
              className="bg-white rounded-2xl border border-slate-100 p-5 flex flex-wrap items-start justify-between gap-4 shadow-sm hover:border-blue-200 transition-colors card"
            >
              <div className="flex-1">
                <p className="font-bold text-slate-900">{fac.name}</p>
                <p className="text-xs text-slate-500 mb-3">
                  {fac.address}, {fac.city}, {fac.state}
                </p>

                <div className="flex flex-wrap gap-1.5 items-center">
                  <span className="text-[10px] font-black uppercase tracking-widest text-slate-400">
                    {(fac.rooms || []).length} room{(fac.rooms || []).length !== 1 ? 's' : ''}
                  </span>
                  {(fac.rooms || []).slice(0, 6).map((r) => (
                    <span
                      key={r.id}
                      className="px-2 py-0.5 bg-blue-50 text-blue-700 rounded text-[9px] font-black uppercase tracking-wider border border-blue-100/80"
                    >
                      {r.code || r.id}
                    </span>
                  ))}
                  {(fac.rooms || []).length > 6 && (
                    <span className="text-[10px] text-slate-400">+{(fac.rooms || []).length - 6}</span>
                  )}
                </div>
              </div>

              <div className="flex gap-2">
                <Link
                  to={`/admin/facilities/${fac.id}/edit`}
                  className="px-4 py-1.5 bg-slate-50 text-slate-600 rounded-lg text-xs font-bold border border-slate-100 hover:bg-white transition-all"
                >
                  Edit
                </Link>
                <button
                  type="button"
                  onClick={() => handleDelete(fac.id, fac.name)}
                  className="px-4 py-1.5 bg-white text-red-400 rounded-lg text-xs font-bold border border-red-50 hover:bg-red-50 hover:text-red-500 transition-all"
                >
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </PageShell>
  )
}
