import { SPECIALIZATIONS } from '../data/constants'

// In-memory store (replace with real API later)
let doctors = []
let chambers = []
let facilities = []
let appointments = []
let users = []

/** Ensure facilities have `rooms` with RM1… codes; migrate legacy roomsAvailable / facility-level specialties. */
export function normalizeFacility(facility) {
  if (!facility) return facility
  if (facility.rooms?.length) {
    const { specializationIds: _drop, ...rest } = facility
    return { ...rest, rooms: facility.rooms.map((r) => ({ ...r })) }
  }
  const n = Math.max(1, parseInt(facility.roomsAvailable, 10) || 1)
  const defaultSpecs =
    facility.specializationIds?.length > 0
      ? [...facility.specializationIds]
      : SPECIALIZATIONS.map((s) => s.id)
  const rooms = []
  for (let i = 0; i < n; i++) {
    rooms.push({
      id: `${facility.id}_rm${i + 1}`,
      code: `RM${i + 1}`,
      specializationIds: [...defaultSpecs],
    })
  }
  const { specializationIds: _si, roomsAvailable: _ra, ...rest } = facility
  return { ...rest, rooms }
}

function pruneInvalidChambersForFacility(facilityId) {
  const fac = facilities.find((f) => f.id === facilityId)
  if (!fac?.rooms) return
  const chamberIdsToRemove = []
  chambers.forEach((ch) => {
    if ((ch.facilityId || ch.clinicId) !== facilityId) return
    const room = fac.rooms.find((r) => r.id === ch.roomId)
    const doc = doctors.find((d) => d.id === ch.doctorId)
    if (!room || !doc) {
      chamberIdsToRemove.push(ch.id)
      return
    }
    const rs = room.specializationIds || []
    const ds = doc.specializationIds || []
    const overlap = ds.some((sid) => rs.includes(sid))
    if (!overlap) chamberIdsToRemove.push(ch.id)
  })
  chamberIdsToRemove.forEach((cid) => {
    deleteChamber(cid)
    appointments.forEach((a) => {
      if (a.chamberId === cid && a.status === 'confirmed') a.status = 'cancelled'
    })
  })
}

// Seed data is loaded from data/seed.js
export function initStore(seed) {
  doctors = (seed.doctors ?? []).map((d) => ({ ...d }))
  chambers = (seed.chambers ?? []).map((c) => ({ ...c }))
  facilities = (seed.facilities ?? seed.clinics ?? []).map(normalizeFacility)
  appointments = (seed.appointments ?? []).map((a) => ({ ...a }))
  users = (seed.users ?? []).map((u) => ({ ...u }))
}

// Users (for auth)
export function getUsers() {
  return [...users]
}

export function updateUserProfile(id, data) {
  const i = users.findIndex(u => u.id === id)
  if (i === -1) return null
  // We merge the old user data with the new data
  users[i] = { ...users[i], ...data }
  return users[i]
}

export function addUser({ name, email, password, role, ...rest }) {
  const id = `user_${Date.now()}`
  const u = { id, name, email, password, role, ...rest }
  users.push(u)
  return u
}

// Doctors
export function getDoctors() {
  return [...doctors]
}

export function getDoctorById(id) {
  return doctors.find(d => d.id === id)
}

export function getDoctorByUserId(userId) {
  return doctors.find(d => d.userId === userId)
}

export function addDoctor(data) {
  const id = `doc_${Date.now()}`
  doctors.push({ id, ...data })
  return { id, ...data }
}

export function updateDoctor(id, data) {
  const i = doctors.findIndex(d => d.id === id)
  if (i === -1) return null
  doctors[i] = { ...doctors[i], ...data }
  return doctors[i]
}

export function deleteDoctor(id) {
  const i = doctors.findIndex(d => d.id === id)
  if (i === -1) return false
  const chamberIds = chambers.filter(c => c.doctorId === id).map(c => c.id)
  chamberIds.forEach(cid => deleteChamber(cid))
  appointments.forEach(a => {
    if (a.doctorId === id && a.status === 'confirmed') a.status = 'cancelled'
  })
  doctors.splice(i, 1)
  return true
}

export function getDoctorsBySpecialization(specId) {
  return doctors.filter(d => d.specializationIds && d.specializationIds.includes(specId))
}

// Facilities (operating hours, rooms, equipment – admin managed)
export function getFacilities() {
  return [...facilities]
}

export function getFacilityById(id) {
  return facilities.find(f => f.id === id)
}

export function getFacilitiesByState(state) {
  return facilities.filter(f => f.state === state)
}

export function addFacility(data) {
  const id = `fac_${Date.now()}`
  const { rooms: incomingRooms, ...rest } = data
  const rawRooms = incomingRooms || []
  const rooms =
    rawRooms.length > 0
      ? rawRooms.map((r, i) => ({
          id: `${id}_rm${i + 1}`,
          code: (r.code || `RM${i + 1}`).toString().trim(),
          specializationIds: [...(r.specializationIds || [])],
        }))
      : undefined
  const normalized = normalizeFacility({ ...rest, id, ...(rooms ? { rooms } : {}) })
  facilities.push(normalized)
  return normalized
}

export function updateFacility(id, data) {
  const i = facilities.findIndex((f) => f.id === id)
  if (i === -1) return null
  const merged = { ...facilities[i], ...data }
  const next = normalizeFacility(merged)
  const prevRoomIds = new Set((facilities[i].rooms || []).map((r) => r.id))
  const nextRoomIds = new Set((next.rooms || []).map((r) => r.id))
  prevRoomIds.forEach((rid) => {
    if (!nextRoomIds.has(rid)) {
      const orphan = chambers.filter(
        (c) => (c.facilityId || c.clinicId) === id && c.roomId === rid,
      )
      orphan.forEach((c) => {
        deleteChamber(c.id)
        appointments.forEach((a) => {
          if (a.chamberId === c.id && a.status === 'confirmed') a.status = 'cancelled'
        })
      })
    }
  })
  facilities[i] = next
  pruneInvalidChambersForFacility(id)
  return facilities[i]
}

export function deleteFacility(id) {
  const i = facilities.findIndex(f => f.id === id)
  if (i === -1) return false
  getChambersByFacilityId(id).forEach((c) => deleteChamber(c.id))
  facilities.splice(i, 1)
  return true
}

export function getChambersByFacilityId(facId) {
  return chambers.filter(c => (c.facilityId || c.clinicId) === facId)
}

// Legacy aliases
export function getClinics() {
  return [...facilities]
}

export function getClinicById(id) {
  return getFacilityById(id)
}

export function getClinicsByState(state) {
  return getFacilitiesByState(state)
}

// Chambers
export function getChambers() {
  return [...chambers]
}

export function getChambersByDoctorId(doctorId) {
  return chambers.filter(c => c.doctorId === doctorId)
}

export function getChambersByState(state) {
  return chambers.filter(c => {
    const fac = getFacilityById(c.facilityId || c.clinicId)
    return fac && fac.state === state
  })
}

export function getChambersByStateAndCity(state, city) {
  return chambers.filter(c => {
    const fac = getFacilityById(c.facilityId || c.clinicId)
    return fac && fac.state === state && fac.city === city
  })
}

export function getChambersByStateCityZip(state, city, zipCode) {
  return chambers.filter(c => {
    const fac = getFacilityById(c.facilityId || c.clinicId)
    return fac && fac.state === state && fac.city === city && (!zipCode || fac.zipCode === zipCode)
  })
}

export function getChamberById(id) {
  return chambers.find(c => c.id === id)
}

export function addChamber(data) {
  const id = `chamber_${Date.now()}`
  chambers.push({ id, ...data })
  return { id, ...data }
}

export function updateChamber(id, data) {
  const i = chambers.findIndex(c => c.id === id)
  if (i === -1) return null
  chambers[i] = { ...chambers[i], ...data }
  return chambers[i]
}

export function deleteChamber(id) {
  const i = chambers.findIndex(c => c.id === id)
  if (i === -1) return false
  chambers.splice(i, 1)
  return true
}

// Appointments
export function getAppointments() {
  return [...appointments]
}

export function getAppointmentsByPatientId(patientId) {
  return appointments.filter(a => a.patientId === patientId)
}

export function getAppointmentsByDoctorId(doctorId) {
  return appointments.filter(a => a.doctorId === doctorId)
}

export function getAppointmentsByChamberId(chamberId) {
  return appointments.filter(a => a.chamberId === chamberId)
}

export function createAppointment(data) {
  const id = `apt_${Date.now()}`
  appointments.push({ id, status: 'confirmed', ...data })
  return { id, status: 'confirmed', ...data }
}

export function setAppointmentActualDuration(id, actualDurationMinutes) {
  const a = appointments.find(x => x.id === id)
  if (!a) return null
  a.actualDurationMinutes = actualDurationMinutes
  a.status = 'completed'
  a.fine = calculateOvertimeFine(a)
  return a
}

// Doctor notes (clinical progress notes) attached to a single appointment/visit.
// Stored in-memory for now; can be replaced by real API later.
export function setAppointmentDoctorNotes(id, doctorNotes) {
  const a = appointments.find(x => x.id === id)
  if (!a) return null
  a.doctorNotes = doctorNotes
  a.doctorNotesUpdatedAt = new Date().toISOString()
  return a
}

// Returns all previous appointments for a given patient & doctor that already have notes.
export function getDoctorNotesHistory(doctorId, patientId) {
  return appointments
    .filter(
      a =>
        a.doctorId === doctorId &&
        a.patientId === patientId &&
        typeof a.doctorNotes === 'string' &&
        a.doctorNotes.trim().length > 0,
    )
    .sort((a, b) => {
      const aTime = new Date(`${a.date}T${a.slotTime || '00:00'}:00`).getTime()
      const bTime = new Date(`${b.date}T${b.slotTime || '00:00'}:00`).getTime()
      return (bTime || 0) - (aTime || 0)
    })
}

// Fine: overtime over booked (up to 60 min) + any time over max 1 hr. Per 30 min = 50% of charge. No double-count.
function calculateOvertimeFine(apt) {
  const booked = apt.bookedDurationMinutes || 30
  const actual = apt.actualDurationMinutes
  if (actual == null || actual <= booked) return 0
  const charge = apt.charge || 0
  const maxVisit = 60
  const per30Charge = charge * 0.5
  let fine = 0
  const overBooked = Math.min(actual, maxVisit) - booked
  if (overBooked > 0) fine += Math.ceil(overBooked / 30) * per30Charge
  if (actual > maxVisit) fine += Math.ceil((actual - maxVisit) / 30) * per30Charge
  return Math.round(fine * 100) / 100
}

export function getAppointmentById(id) {
  return appointments.find(a => a.id === id)
}

/**
 * @param {string} id
 * @param {{ lateCancellation?: boolean }} [options] — when true, marks policy fee as applied (no dollar amount stored for patient UI)
 */
export function cancelAppointment(id, options = {}) {
  const a = appointments.find(x => x.id === id)
  if (!a) return null
  a.status = 'cancelled'
  if (options.lateCancellation) {
    a.cancellationFeeApplied = true
  }
  return a
}

/**
 * Booked times for the same physical room on a date (all doctors sharing that room).
 */
export function getBookedSlotsForChamberDate(chamberId, dateStr) {
  const ch = chambers.find((c) => c.id === chamberId)
  if (!ch) return []
  const facId = ch.facilityId || ch.clinicId
  const roomId = ch.roomId
  const relevantChamberIds = roomId
    ? chambers
        .filter(
          (c) => (c.facilityId || c.clinicId) === facId && c.roomId === roomId,
        )
        .map((c) => c.id)
    : [chamberId]
  return appointments
    .filter(
      (a) =>
        relevantChamberIds.includes(a.chamberId) &&
        a.date === dateStr &&
        a.status !== 'cancelled',
    )
    .map((a) => a.slotTime)
}

/** All visits between one doctor and one patient, newest first (notes may be empty). */
export function getPatientVisitHistoryForDoctor(doctorId, patientId) {
  return appointments
    .filter((a) => a.doctorId === doctorId && a.patientId === patientId)
    .sort((a, b) => {
      const aTime = new Date(`${a.date}T${a.slotTime || '00:00'}:00`).getTime()
      const bTime = new Date(`${b.date}T${b.slotTime || '00:00'}:00`).getTime()
      return (bTime || 0) - (aTime || 0)
    })
}

export function getRoomForChamber(chamber) {
  if (!chamber) return null
  const fac = getFacilityById(chamber.facilityId || chamber.clinicId)
  if (!fac?.rooms || !chamber.roomId) return null
  return fac.rooms.find((r) => r.id === chamber.roomId) || null
}
