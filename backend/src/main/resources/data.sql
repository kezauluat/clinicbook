INSERT INTO roles (name) VALUES ('PATIENT'), ('DOCTOR'), ('RECEPTIONIST'), ('ADMIN') ON CONFLICT (name) DO NOTHING;
CREATE UNIQUE INDEX IF NOT EXISTS ux_appointment_doctor_slot ON appointments (doctor_id, start_at) WHERE status <> 'CANCELLED';
