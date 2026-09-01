ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(50);

UPDATE users
   SET username = LOWER(phone)
 WHERE username IS NULL
   AND phone !~ '^(0[0-9]{9}|\+84[0-9]{9})$';

UPDATE users
   SET username = 'admin', phone = '0900000000'
 WHERE UPPER(role) = 'ADMIN'
   AND LOWER(phone) = 'admin'
   AND NOT EXISTS (
       SELECT 1 FROM users other
        WHERE other.phone = '0900000000'
          AND other.id <> users.id
   );

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username_lower
    ON users (LOWER(username))
 WHERE username IS NOT NULL;
