import { initStore } from '../services/api'
import { SPECIALIZATIONS } from './constants'

const allSpecIds = SPECIALIZATIONS.map((s) => s.id)

function roomsForFacility(facId, count, specializationIds = allSpecIds) {
  return Array.from({ length: count }, (_, i) => ({
    id: `${facId}_rm${i + 1}`,
    code: `RM${i + 1}`,
    specializationIds: [...specializationIds],
  }))
}

const users = [
  { id: 'user_1', firstName: 'John', lastName: 'Patient', name: 'John Patient', mobileNumber: '123-456-7890', email: 'patient@test.com', password: '123456', role: 'patient', creditCardNumber: '4111222233334444', cardholderName: 'John Patient', cvv: '123', expirationDate: '12/25', billingZip: '22202' },
  { id: 'user_2', firstName: 'Sarah', lastName: 'Smith', name: 'Sarah Smith', email: 'doctor@test.com', password: '123456', role: 'doctor' },
  { id: 'admin_1', firstName: 'WeCureIT', lastName: 'Admin', name: 'WeCureIT Admin', email: 'admin@wecureit.com', password: 'wecureIT', role: 'admin' },
  { id: 'user_d2', firstName: 'James', lastName: 'Wilson', name: 'James Wilson', email: 'jwilson@test.com', password: '123456', role: 'doctor' },
  { id: 'user_d3', firstName: 'Emily', lastName: 'Davis', name: 'Emily Davis', email: 'edavis@test.com', password: '123456', role: 'doctor' },
  { id: 'user_d4', firstName: 'Michael', lastName: 'Brown', name: 'Michael Brown', email: 'mbrown@test.com', password: '123456', role: 'doctor' },
]

/** licenseBySpecialty: specialty id → states where that specialty may be practiced */
const doctors = [
  {
    id: 'doc_1',
    userId: 'user_2',
    name: 'Dr. Sarah Smith',
    qualification: 'MD, MBBS',
    specializationIds: ['spec_1', 'spec_2'],
    licenseBySpecialty: {
      spec_1: ['Virginia', 'District of Columbia'],
      spec_2: ['Virginia', 'District of Columbia'],
    },
    bio: 'Experienced cardiologist and general physician.',
  },
  {
    id: 'doc_2',
    userId: 'user_d2',
    name: 'Dr. James Wilson',
    qualification: 'MS Ortho',
    specializationIds: ['spec_3'],
    licenseBySpecialty: {
      spec_3: ['Maryland', 'Virginia'],
    },
    bio: 'Orthopedic specialist with 15+ years experience.',
  },
  {
    id: 'doc_3',
    userId: 'user_d3',
    name: 'Dr. Emily Davis',
    qualification: 'MD Pediatrics',
    specializationIds: ['spec_4', 'spec_2'],
    licenseBySpecialty: {
      spec_4: ['Virginia', 'Maryland', 'District of Columbia'],
      spec_2: ['Virginia', 'Maryland'],
    },
    bio: 'Caring pediatrician and family doctor.',
  },
  {
    id: 'doc_4',
    userId: 'user_d4',
    name: 'Dr. Michael Brown',
    qualification: 'MD Dermatology',
    specializationIds: ['spec_5'],
    licenseBySpecialty: {
      spec_5: ['District of Columbia'],
    },
    bio: 'Skin and allergy specialist.',
  },
]

const facilities = [
  { id: 'fac_va1', name: 'WeCureIT Arlington', address: '123 Medical Tower, Wilson Blvd', state: 'Virginia', city: 'Arlington', zipCode: '22201', openTime: '08:00', closeTime: '18:00', equipment: [{ type: 'X-Ray', count: 2 }, { type: 'EKG', count: 4 }], rooms: roomsForFacility('fac_va1', 8) },
  { id: 'fac_va2', name: 'WeCureIT Fairfax', address: '456 Health Plaza, Main St', state: 'Virginia', city: 'Fairfax', zipCode: '22030', openTime: '07:00', closeTime: '20:00', equipment: [{ type: 'MRI', count: 1 }, { type: 'CT Scanner', count: 1 }, { type: 'Ultrasound', count: 2 }], rooms: roomsForFacility('fac_va2', 12) },
  { id: 'fac_va3', name: 'WeCureIT Richmond', address: '789 Broad St', state: 'Virginia', city: 'Richmond', zipCode: '23219', openTime: '08:00', closeTime: '17:00', equipment: [{ type: 'X-Ray', count: 1 }, { type: 'EKG', count: 3 }], rooms: roomsForFacility('fac_va3', 6) },
  { id: 'fac_md1', name: 'WeCureIT Bethesda', address: '100 Wisconsin Ave', state: 'Maryland', city: 'Bethesda', zipCode: '20814', openTime: '07:30', closeTime: '19:00', equipment: [{ type: 'MRI', count: 1 }, { type: 'X-Ray', count: 2 }, { type: 'Ultrasound', count: 2 }], rooms: roomsForFacility('fac_md1', 10) },
  { id: 'fac_md2', name: 'WeCureIT Silver Spring', address: '200 Colesville Rd', state: 'Maryland', city: 'Silver Spring', zipCode: '20910', openTime: '08:00', closeTime: '18:00', equipment: [{ type: 'CT Scanner', count: 1 }, { type: 'EKG', count: 4 }], rooms: roomsForFacility('fac_md2', 7) },
  { id: 'fac_dc1', name: 'WeCureIT DC Downtown', address: '500 15th St NW', state: 'District of Columbia', city: 'Washington', zipCode: '20004', openTime: '08:00', closeTime: '20:00', equipment: [{ type: 'MRI', count: 2 }, { type: 'CT Scanner', count: 1 }, { type: 'X-Ray', count: 3 }, { type: 'Ultrasound', count: 3 }], rooms: roomsForFacility('fac_dc1', 15) },
  { id: 'fac_dc2', name: 'WeCureIT Georgetown', address: '3300 M St NW', state: 'District of Columbia', city: 'Washington', zipCode: '20007', openTime: '09:00', closeTime: '17:00', equipment: [{ type: 'X-Ray', count: 1 }, { type: 'EKG', count: 2 }], rooms: roomsForFacility('fac_dc2', 5) },
]

const chambers = [
  { id: 'ch_1', doctorId: 'doc_1', facilityId: 'fac_va1', clinicId: 'fac_va1', roomId: 'fac_va1_rm1', slotDurationMinutes: 30, availableDays: ['mon', 'wed', 'fri'], slotStart: '09:00', slotEnd: '13:00', consultationCharge: 120 },
  { id: 'ch_2', doctorId: 'doc_1', facilityId: 'fac_dc1', clinicId: 'fac_dc1', roomId: 'fac_dc1_rm1', slotDurationMinutes: 30, availableDays: ['tue', 'thu'], slotStart: '10:00', slotEnd: '14:00', consultationCharge: 100 },
  { id: 'ch_3', doctorId: 'doc_2', facilityId: 'fac_md1', clinicId: 'fac_md1', roomId: 'fac_md1_rm1', slotDurationMinutes: 30, availableDays: ['mon', 'tue', 'wed', 'thu', 'fri'], slotStart: '09:00', slotEnd: '13:00', consultationCharge: 150 },
  { id: 'ch_4', doctorId: 'doc_3', facilityId: 'fac_dc1', clinicId: 'fac_dc1', roomId: 'fac_dc1_rm2', slotDurationMinutes: 30, availableDays: ['mon', 'wed', 'fri'], slotStart: '08:00', slotEnd: '12:00', consultationCharge: 90 },
]

const appointments = [
  {
    id: 'apt_1',
    patientId: 'user_1',
    doctorId: 'doc_1',
    chamberId: 'ch_1',
    date: new Date().toISOString().slice(0, 10),
    slotTime: '10:00',
    status: 'confirmed',
    patientName: 'John Patient',
    doctorName: 'Dr. Sarah Smith',
    bookedDurationMinutes: 30,
    charge: 120,
    roomId: 'fac_va1_rm1',
    roomCode: 'RM1',
  },
]

initStore({ users, doctors, chambers, facilities, appointments })

export { users, doctors, chambers, facilities, appointments }
