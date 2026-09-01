ALTER TABLE bus ALTER COLUMN maintenance_interval_km SET DEFAULT 10000;
UPDATE bus SET maintenance_interval_km = 10000 WHERE maintenance_interval_km = 1000;

UPDATE bus
SET maintenance_alert_level = CASE
    WHEN current_mileage - last_maintenance_mileage >= maintenance_interval_km THEN 100
    WHEN current_mileage - last_maintenance_mileage >= maintenance_interval_km * 0.8 THEN 80
    ELSE 0
END;
