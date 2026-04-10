import { useState, useMemo, useEffect, useDeferredValue } from 'react'
import { Link } from 'react-router-dom'
import { Search } from 'lucide-react'
import { getDoctors, getChambers, getFacilities } from '../../services/api'
import {
  SPECIALIZATIONS,
  MOCK_STATES,
  doctorLicensedForSpecialtyInState,
  doctorHasAnyLicenseInState,
} from '../../data/constants'
import PageShell from '../../components/PageShell'

function doctorMatchesLocation(doc, state, facilityId, chambers, facilities) {
  if (!state && !facilityId) return true
  const doctorsChambers = chambers.filter((ch) => ch.doctorId === doc.id)
  return doctorsChambers.some((chamber) => {
    const fac = facilities.find((f) => f.id === (chamber.facilityId || chamber.clinicId))
    return (
      (!state || fac?.state === state) && (!facilityId || fac?.id === facilityId)
    )
  })
}

export default function BrowseDoctors() {
  const [selectedSpec, setSelectedSpec] = useState(null)
  const [selectedState, setSelectedState] = useState('')
  const [selectedFacilityId, setSelectedFacilityId] = useState('')
  const [doctorSearch, setDoctorSearch] = useState('')

  const deferredSearch = useDeferredValue(doctorSearch)
  const searchActive = doctorSearch !== deferredSearch

  const [currentPage, setCurrentPage] = useState(1)
  const limit = 5
  const offset = (currentPage - 1) * limit

  const doctors = getDoctors()
  const facilities = getFacilities()
  const chambers = getChambers()

  const filteredByBase = useMemo(() => {
    const q = deferredSearch.trim().toLowerCase()
    return doctors.filter((doc) => {
      const matchesName = !q || doc.name?.toLowerCase().includes(q)
      const matchesLocation = doctorMatchesLocation(doc, selectedState, selectedFacilityId, chambers, facilities)
      const licensedInRegion =
        !selectedState || doctorHasAnyLicenseInState(doc, selectedState)
      return matchesName && matchesLocation && licensedInRegion
    })
  }, [doctors, chambers, facilities, deferredSearch, selectedState, selectedFacilityId])

  const specializationSidebar = useMemo(() => {
    const rows = SPECIALIZATIONS.map((s) => ({
      ...s,
      count: filteredByBase.filter((d) => {
        if (!d.specializationIds?.includes(s.id)) return false
        if (selectedState && !doctorLicensedForSpecialtyInState(d, s.id, selectedState)) return false
        return true
      }).length,
    }))
      .filter((row) => row.count > 0)
      .sort((a, b) => b.count - a.count || a.name.localeCompare(b.name))
    return rows
  }, [filteredByBase, selectedState])

  useEffect(() => {
    if (!selectedSpec) return
    const stillValid = specializationSidebar.some((s) => s.id === selectedSpec)
    if (!stillValid) setSelectedSpec(null)
  }, [selectedSpec, specializationSidebar])

  const filteredDoctors = useMemo(() => {
    let list = filteredByBase
    if (selectedSpec) {
      list = list.filter((d) => {
        if (!d.specializationIds?.includes(selectedSpec)) return false
        if (selectedState && !doctorLicensedForSpecialtyInState(d, selectedSpec, selectedState)) return false
        return true
      })
    }
    return list
  }, [filteredByBase, selectedSpec, selectedState])

  const totalRecords = filteredDoctors.length
  const totalPages = Math.ceil(totalRecords / limit) || 1

  const currentRecords = useMemo(
    () => filteredDoctors.slice(offset, offset + limit),
    [filteredDoctors, offset, limit],
  )

  useEffect(() => {
    setCurrentPage(1)
  }, [selectedSpec, selectedState, selectedFacilityId, doctorSearch])

  useEffect(() => {
    if (currentPage > totalPages) setCurrentPage(totalPages)
  }, [currentPage, totalPages])

  const facilitiesInState = selectedState
    ? facilities.filter((f) => f.state === selectedState)
    : facilities

  return (
    <PageShell
      eyebrow="Clinical search"
      title="Find your specialist"
      subtitle="Filters apply instantly—region and specialty respect each doctor’s state-specific licenses."
      heroSize="compact"
      tone="rose"
      actions={
        <div className="hidden sm:flex items-center justify-center w-14 h-14 rounded-2xl bg-white/10 backdrop-blur-md ring-1 ring-white/20">
          <Search className="w-7 h-7 text-white opacity-95" />
        </div>
      }
    >
      <div className="bg-white border border-slate-100 rounded-3xl p-3 shadow-sm ring-1 ring-slate-100 mb-12 flex flex-col lg:flex-row items-stretch gap-3">
        <div className="flex-1 flex items-center px-4 py-3 bg-slate-50 rounded-2xl border border-slate-100">
          <select
            value={selectedState}
            onChange={(e) => {
              setSelectedState(e.target.value)
              setSelectedFacilityId('')
            }}
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
        <div className="flex-[1.5] flex items-center px-4 py-3 bg-slate-50 rounded-2xl border border-slate-100">
          <select
            value={selectedFacilityId}
            onChange={(e) => setSelectedFacilityId(e.target.value)}
            className="w-full bg-transparent border-none focus:ring-0 text-slate-900 font-bold text-sm"
          >
            <option value="">All facilities</option>
            {facilitiesInState.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </select>
        </div>
        <div
          className={`flex-1 flex items-center gap-3 px-4 py-3 bg-slate-50 rounded-2xl border border-slate-100 transition-opacity ${searchActive ? 'opacity-70' : ''}`}
        >
          <Search className="w-4 h-4 text-slate-400 shrink-0" aria-hidden />
          <input
            type="search"
            placeholder="Search by doctor name…"
            value={doctorSearch}
            onChange={(e) => setDoctorSearch(e.target.value)}
            className="w-full bg-transparent border-none focus:ring-0 text-slate-700 font-bold text-sm placeholder-slate-400"
            autoComplete="off"
          />
        </div>
      </div>

      <div className="flex flex-col lg:flex-row gap-12">
        <aside className="lg:w-72 shrink-0">
          <h2 className="text-xs font-black text-slate-400 uppercase tracking-[0.2em] mb-6 px-2">
            Specialization
          </h2>
          <p className="text-[10px] text-slate-400 font-medium mb-4 px-2 leading-relaxed">
            Counts reflect doctors licensed for that specialty in your selected region when a region is chosen.
          </p>
          <div className="flex flex-wrap lg:flex-col gap-2">
            <button
              type="button"
              onClick={() => setSelectedSpec(null)}
              className={`text-left px-5 py-3 rounded-xl text-sm font-bold transition-all w-full lg:w-auto ${
                !selectedSpec
                  ? 'bg-rose-100 text-rose-800 ring-1 ring-rose-200 shadow-sm shadow-rose-50'
                  : 'text-slate-500 hover:bg-slate-100'
              }`}
            >
              All specialties
              <span className="block text-[10px] font-black text-slate-400 mt-0.5">
                {filteredByBase.length} doctor{filteredByBase.length !== 1 ? 's' : ''}
              </span>
            </button>
            {specializationSidebar.map((s) => (
              <button
                key={s.id}
                type="button"
                onClick={() => setSelectedSpec(s.id)}
                className={`text-left px-5 py-3 rounded-xl text-sm font-bold transition-all w-full lg:w-auto ${
                  selectedSpec === s.id
                    ? 'bg-rose-100 text-rose-800 ring-1 ring-rose-200 shadow-sm shadow-rose-50'
                    : 'text-slate-500 hover:bg-slate-100'
                }`}
              >
                <span className="flex items-center justify-between gap-2">
                  <span>{s.name}</span>
                  <span className="text-[10px] font-black tabular-nums text-rose-600/90 shrink-0">{s.count}</span>
                </span>
              </button>
            ))}
          </div>
        </aside>

        <div className="flex-1">
          <div className="flex items-center justify-between mb-8 border-b border-slate-100 pb-4">
            <h2 className="text-xl font-bold text-slate-900">Available specialists</h2>
            <div className="text-right">
              <span className="block text-[10px] font-black text-slate-300 uppercase tracking-widest leading-none mb-1">
                Total found
              </span>
              <span className="text-sm font-bold text-slate-600">{totalRecords} results</span>
            </div>
          </div>

          <div className="space-y-4 min-h-[400px]">
            {currentRecords.map((doc) => (
              <Link
                key={doc.id}
                to={`/patient/doctors/${doc.id}`}
                className="group bg-white border border-slate-100 p-6 rounded-3xl hover:border-rose-200 transition-all flex items-center justify-between shadow-sm hover:shadow-xl hover:shadow-rose-50/50"
              >
                <div className="flex gap-6 items-center">
                  <div className="w-16 h-16 rounded-2xl bg-slate-50 border border-slate-100 flex items-center justify-center text-slate-300 text-2xl font-black group-hover:bg-rose-50 group-hover:text-rose-700 transition-colors">
                    {doc.name?.charAt(0)}
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 text-lg group-hover:text-rose-800 leading-tight">
                      {doc.name}
                    </h3>
                    <p className="text-rose-600 text-[10px] font-black uppercase tracking-widest mt-1">
                      {doc.qualification}
                    </p>
                  </div>
                </div>
                <div className="hidden sm:block">
                  <span className="text-xs font-bold text-slate-400 group-hover:text-rose-700 transition-colors uppercase tracking-widest">
                    Select slot →
                  </span>
                </div>
              </Link>
            ))}
            {totalRecords === 0 && (
              <div className="py-20 text-center text-slate-400 font-bold uppercase tracking-widest bg-slate-50 rounded-3xl border-2 border-dashed border-slate-200">
                No matching doctors found
              </div>
            )}
          </div>

          {totalRecords > limit && (
            <div className="mt-12 flex flex-col items-center gap-4 border-t border-slate-50 pt-8">
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage((p) => p - 1)}
                  className="p-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 disabled:opacity-20 transition-all"
                >
                  <svg className="w-5 h-5 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M15 19l-7-7 7-7" />
                  </svg>
                </button>

                <div className="px-5 py-2.5 bg-white border border-slate-200 rounded-2xl shadow-sm text-xs font-bold text-slate-700">
                  Page <span className="text-rose-700">{currentPage}</span> of {totalPages}
                </div>

                <button
                  type="button"
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage((p) => p + 1)}
                  className="p-2.5 rounded-xl border border-slate-200 hover:bg-slate-50 disabled:opacity-20 transition-all"
                >
                  <svg className="w-5 h-5 text-slate-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2.5" d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </PageShell>
  )
}
