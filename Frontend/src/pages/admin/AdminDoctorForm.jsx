import { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { Stethoscope, Plus, Trash2 } from 'lucide-react'
import { getDoctorById, addDoctor, updateDoctor } from '../../services/api'
import { SPECIALIZATIONS, MOCK_STATES } from '../../data/constants'
import PageShell, { PageBackLink } from '../../components/PageShell'

function licenseRowsFromDoctor(doctor) {
  if (doctor?.licenseBySpecialty && typeof doctor.licenseBySpecialty === 'object') {
    const rows = Object.entries(doctor.licenseBySpecialty).map(([specialtyId, states]) => ({
      specialtyId,
      states: Array.isArray(states) ? [...states] : [],
    }))
    if (rows.length) return rows
  }
  if (doctor?.licensedStates?.length && doctor?.specializationIds?.length) {
    return doctor.specializationIds.map((specialtyId) => ({
      specialtyId,
      states: [...doctor.licensedStates],
    }))
  }
  return [{ specialtyId: '', states: [] }]
}

export default function AdminDoctorForm() {
  const { doctorId } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(doctorId)
  const doctor = isEdit ? getDoctorById(doctorId) : null

  const [name, setName] = useState('')
  const [qualification, setQualification] = useState('')
  const [bio, setBio] = useState('')
  const [licenseRows, setLicenseRows] = useState([{ specialtyId: '', states: [] }])
  const [error, setError] = useState('')

  useEffect(() => {
    if (doctor) {
      setName(doctor.name || '')
      setQualification(doctor.qualification || '')
      setBio(doctor.bio || '')
      setLicenseRows(licenseRowsFromDoctor(doctor))
    }
  }, [doctor])

  const addLicenseRow = () => {
    setLicenseRows((rows) => [...rows, { specialtyId: '', states: [] }])
  }

  const removeLicenseRow = (index) => {
    setLicenseRows((rows) => (rows.length <= 1 ? rows : rows.filter((_, i) => i !== index)))
  }

  const setRowSpecialty = (index, specialtyId) => {
    setLicenseRows((rows) =>
      rows.map((r, i) => (i === index ? { ...r, specialtyId } : r)),
    )
  }

  const toggleRowState = (index, state) => {
    setLicenseRows((rows) =>
      rows.map((r, i) => {
        if (i !== index) return r
        const has = r.states.includes(state)
        return {
          ...r,
          states: has ? r.states.filter((s) => s !== state) : [...r.states, state],
        }
      }),
    )
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    setError('')
    if (!name?.trim()) {
      setError('Name is required')
      return
    }
    const validRows = licenseRows.filter((r) => r.specialtyId)
    if (validRows.length === 0) {
      setError('Add at least one specialty with licensed states.')
      return
    }
    for (const r of validRows) {
      if (r.states.length === 0) {
        setError(`Select at least one state for each specialty (${SPECIALIZATIONS.find((s) => s.id === r.specialtyId)?.name || 'specialty'}).`)
        return
      }
    }
    const licenseBySpecialty = {}
    validRows.forEach((r) => {
      licenseBySpecialty[r.specialtyId] = [...r.states]
    })
    const specializationIds = [...new Set(validRows.map((r) => r.specialtyId))]
    const payload = {
      name: name.trim(),
      qualification: qualification.trim(),
      bio: bio.trim(),
      specializationIds,
      licenseBySpecialty,
    }
    if (isEdit) {
      updateDoctor(doctorId, payload)
    } else {
      addDoctor({ ...payload, userId: `user_doc_${Date.now()}` })
    }
    navigate('/admin/doctors')
  }

  const usedSpecs = licenseRows.map((r) => r.specialtyId).filter(Boolean)

  return (
    <PageShell
      eyebrow="Directory"
      title={isEdit ? 'Update doctor' : 'Add doctor'}
      subtitle="Each specialty lists the states where this provider may practice that discipline."
      heroSize="compact"
      contentMax="max-w-2xl"
      tone="blue"
      actions={
        <div className="hidden sm:flex items-center justify-center w-14 h-14 rounded-2xl bg-white/10 backdrop-blur-md ring-1 ring-white/20">
          <Stethoscope className="w-7 h-7 text-white opacity-95" />
        </div>
      }
    >
      <PageBackLink to="/admin/doctors">Manage doctors</PageBackLink>

      <form onSubmit={handleSubmit} className="card p-8 md:p-10 space-y-8 rounded-[2rem]">
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Full name</label>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            className="input-field"
            placeholder="Dr. Jane Doe"
            required
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Qualification</label>
          <input
            type="text"
            value={qualification}
            onChange={(e) => setQualification(e.target.value)}
            className="input-field"
            placeholder="MD, MBBS"
          />
        </div>

        <div className="space-y-4">
          <div className="flex items-center justify-between gap-3">
            <label className="block text-sm font-medium text-slate-700">Specialty licenses</label>
            <button type="button" onClick={addLicenseRow} className="text-xs font-bold text-blue-700 uppercase tracking-widest inline-flex items-center gap-1">
              <Plus className="w-4 h-4" />
              Add specialty
            </button>
          </div>
          <p className="text-xs text-slate-500 font-medium">
            For each specialty, choose where the doctor is licensed to practice that specialty (e.g. cardiology in Virginia but not Maryland).
          </p>

          <ul className="space-y-6">
            {licenseRows.map((row, idx) => (
              <li key={idx} className="rounded-2xl border border-slate-100 bg-slate-50/60 p-5 space-y-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div className="flex-1 min-w-[12rem]">
                    <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Specialty</label>
                    <select
                      value={row.specialtyId}
                      onChange={(e) => setRowSpecialty(idx, e.target.value)}
                      className="input-field text-sm font-bold"
                    >
                      <option value="">Select specialty</option>
                      {SPECIALIZATIONS.map((s) => (
                        <option
                          key={s.id}
                          value={s.id}
                          disabled={usedSpecs.includes(s.id) && row.specialtyId !== s.id}
                        >
                          {s.name}
                        </option>
                      ))}
                    </select>
                  </div>
                  {licenseRows.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeLicenseRow(idx)}
                      className="text-red-600 p-2 rounded-xl hover:bg-red-50"
                      aria-label="Remove row"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  )}
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-400 uppercase tracking-widest mb-2">Licensed states for this specialty</label>
                  <div className="flex flex-wrap gap-2">
                    {MOCK_STATES.map((st) => (
                      <button
                        key={st}
                        type="button"
                        onClick={() => toggleRowState(idx, st)}
                        className={`px-3 py-2 rounded-xl text-xs font-bold transition-all ${
                          row.states.includes(st)
                            ? 'bg-blue-600 text-white shadow-md shadow-blue-900/20'
                            : 'bg-white text-slate-500 border border-slate-100 hover:border-slate-300'
                        }`}
                      >
                        {st}
                      </button>
                    ))}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Bio</label>
          <textarea value={bio} onChange={(e) => setBio(e.target.value)} className="input-field min-h-[80px]" placeholder="Short bio" />
        </div>
        {error && <p className="text-red-600 text-sm">{error}</p>}
        <div className="flex gap-3">
          <button type="submit" className="btn-primary">
            {isEdit ? 'Save changes' : 'Add doctor'}
          </button>
          <Link to="/admin/doctors" className="btn-secondary">
            Cancel
          </Link>
        </div>
      </form>
    </PageShell>
  )
}
