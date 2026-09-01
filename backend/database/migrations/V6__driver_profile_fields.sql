-- Extended driver profile used by the admin driver form.
-- Nullable columns preserve compatibility with existing driver accounts.
ALTER TABLE users ADD COLUMN IF NOT EXISTS gender VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS citizen_id VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS citizen_id_issue_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS emergency_contact_name VARCHAR(150);
ALTER TABLE users ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(30);
ALTER TABLE users ADD COLUMN IF NOT EXISTS driver_license_class VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS driver_license_number VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS driver_license_issue_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS driver_license_expiry_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS driving_experience_years INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS driver_shift VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS driver_notes VARCHAR(1000);
