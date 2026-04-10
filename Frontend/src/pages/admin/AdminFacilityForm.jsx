import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Building2, Plus, Trash2 } from 'lucide-react'
import { getFacilityById, addFacility, updateFacility } from '../../services/api'
import { SPECIALIZATIONS, MOCK_STATES } from '../../data/constants'
import PageShell, { PageBackLink } from '../../components/PageShell'

function emptyRoom(index = 1) {
  return {
    id: `pending_rm_${Date.now()}_${index}`,
    code: `RM${index}`,
    specializationIds: [],
  }
}

export default function AdminFacilityForm() {
  const { facilityId } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(facilityId)

  const [form, setForm] = useState({
    name: '',
    address: '',
    state: 'Virginia',
    city: '',
    zipCode: '',
    openTime: '08:00',
    closeTime: '18:00',
    equipment: [],
    rooms: [],
  })

  useEffect(() => {
    if (isEdit) {
      const data = getFacilityById(facilityId)
      if (data) {
        const rooms = (data.rooms || []).map((r) => ({
          ...r,
          specializationIds: [...(r.specializationIds || [])],
        }))
        setForm({
          name: data.name || '',
          address: data.address || '',
          state: data.state || 'Virginia',
          city: data.city || '',
          zipCode: data.zipCode || '',
          openTime: data.openTime || '08:00',
          closeTime: data.closeTime || '18:00',
          equipment: data.equipment || [],
          rooms: rooms.length ? rooms : [emptyRoom(1)],
        })
      }
    } else {
      setForm((f) => ({
        ...f,
        rooms: [emptyRoom(1)],
      }))
    }
  }, [facilityId, isEdit])

  const toggleRoomSpec = (roomIndex, specId) => {
    setForm((f) => {
      const rooms = f.rooms.map((r, i) => {
        if (i !== roomIndex) return r
        const ids = r.specializationIds.includes(specId)
          ? r.specializationIds.filter((x) => x !== specId)
          : [...r.specializationIds, specId]
        return { ...r, specializationIds: ids }
      })
      return { ...f, rooms }
    })
  }

  const setRoomCode = (roomIndex, code) => {
    setForm((f) => ({
      ...f,
      rooms: f.rooms.map((r, i) => (i === roomIndex ? { ...r, code: code.trim() || `RM${i + 1}` } : r)),
    }))
  }

  const addRoom = () => {
    setForm((f) => {
      const next = f.rooms.length + 1
      return { ...f, rooms: [...f.rooms, emptyRoom(next)] }
    })
  }

  const removeRoom = (roomIndex) => {
    setForm((f) => ({
      ...f,
      rooms: f.rooms.length <= 1 ? f.rooms : f.rooms.filter((_, i) => i !== roomIndex),
    }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (isEdit) {
      const rooms = form.rooms.map((r, i) => ({
        id: /^[a-z0-9_]+_rm\d+$/i.test(String(r.id || '')) ? r.id : `${facilityId}_rm${i + 1}`,
        code: (r.code || `RM${i + 1}`).trim(),
        specializationIds: [...(r.specializationIds || [])],
      }))
      updateFacility(facilityId, { ...form, rooms })
    } else {
      const rooms = form.rooms.map((r, i) => ({
        code: (r.code || `RM${i + 1}`).trim(),
        specializationIds: [...(r.specializationIds || [])],
      }))
      addFacility({ ...form, rooms })
    }
    navigate('/admin/facilities')
  }

  return (
    <PageShell
      eyebrow="Facilities"
      title={isEdit ? 'Edit facility' : 'Add facility'}
      subtitle="Locations, hours, and rooms—each room lists which specialties may be seen there. Removing a room or changing its specialties updates schedules immediately."
      heroSize="compact"
      contentMax="max-w-3xl"
      tone="blue"
      actions={
        <div className="hidden sm:flex items-center justify-center w-14 h-14 rounded-2xl bg-white/10 backdrop-blur-md ring-1 ring-white/20">
          <Building2 className="w-7 h-7 text-white opacity-95" />
        </div>
      }
    >
      <PageBackLink to="/admin/facilities">Facility list</PageBackLink>

      <form onSubmit={handleSubmit} className="card p-8 md:p-10 space-y-8 rounded-[2rem]">
        <div className="grid sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Facility name</label>
            <input
              type="text"
              value={form.name}
              onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
              className="input-field"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">State</label>
            <select
              value={form.state}
              onChange={(e) => setForm((f) => ({ ...f, state: e.target.value }))}
              className="input-field"
            >
              {MOCK_STATES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-slate-700 mb-1">Street address</label>
          <input
            type="text"
            value={form.address}
            onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))}
            className="input-field"
            required
          />
        </div>

        <div className="grid sm:grid-cols-3 gap-4">
          <div className="sm:col-span-2">
            <label className="block text-sm font-medium text-slate-700 mb-1">City</label>
            <input
              type="text"
              value={form.city}
              onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))}
              className="input-field"
              required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">ZIP</label>
            <input
              type="text"
              value={form.zipCode}
              onChange={(e) => setForm((f) => ({ ...f, zipCode: e.target.value }))}
              className="input-field"
              required
            />
          </div>
        </div>

        <div className="grid sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Open</label>
            <input
              type="time"
              value={form.openTime}
              onChange={(e) => setForm((f) => ({ ...f, openTime: e.target.value }))}
              className="input-field"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Close</label>
            <input
              type="time"
              value={form.closeTime}
              onChange={(e) => setForm((f) => ({ ...f, closeTime: e.target.value }))}
              className="input-field"
            />
          </div>
        </div>

        <div className="pt-4 border-t border-slate-100">
          <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-900">Rooms</h3>
              <p className="text-xs text-slate-500 font-medium mt-1">
                Use codes like RM1, RM2. Supported specialties apply per room, not to the whole building.
              </p>
            </div>
            <button type="button" onClick={addRoom} className="btn-secondary text-sm inline-flex items-center gap-2">
              <Plus className="w-4 h-4" />
              Add room
            </button>
          </div>

          <ul className="space-y-6">
            {form.rooms.map((room, ri) => (
              <li
                key={room.id || ri}
                className="rounded-2xl border border-slate-100 bg-slate-50/50 p-5 space-y-4"
              >
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <label className="text-xs font-bold text-slate-500 uppercase tracking-widest">Room code</label>
                    <input
                      type="text"
                      value={room.code}
                      onChange={(e) => setRoomCode(ri, e.target.value)}
                      className="input-field w-28 text-sm font-bold"
                      placeholder="RM1"
                      required
                    />
                  </div>
                  {form.rooms.length > 1 && (
                    <button
                      type="button"
                      onClick={() => removeRoom(ri)}
                      className="text-red-600 text-xs font-bold uppercase tracking-widest inline-flex items-center gap-1 hover:text-red-700"
                    >
                      <Trash2 className="w-4 h-4" />
                      Remove
                    </button>
                  )}
                </div>
                <div>
                  <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mb-2">Supported specialties</p>
                  <div className="flex flex-wrap gap-2">
                    {SPECIALIZATIONS.map((s) => (
                      <button
                        key={s.id}
                        type="button"
                        onClick={() => toggleRoomSpec(ri, s.id)}
                        className={`px-3 py-2 rounded-xl text-xs font-bold transition-all border ${
                          room.specializationIds.includes(s.id)
                            ? 'bg-blue-600 text-white border-blue-600 shadow-md shadow-blue-900/20'
                            : 'bg-white text-slate-400 border-slate-100 hover:border-slate-300'
                        }`}
                      >
                        {s.name}
                      </button>
                    ))}
                  </div>
                </div>
              </li>
            ))}
          </ul>
        </div>

        <div className="flex gap-3 pt-4 border-t border-slate-100">
          <button type="submit" className="btn-primary px-6 py-2.5 rounded-xl text-sm font-bold">
            {isEdit ? 'Save changes' : 'Create facility'}
          </button>
          <button
            type="button"
            onClick={() => navigate('/admin/facilities')}
            className="px-6 py-2.5 bg-white text-slate-400 border border-slate-100 rounded-xl font-bold text-sm hover:bg-slate-50 transition-all"
          >
            Cancel
          </button>
        </div>
      </form>
    </PageShell>
  )
}
