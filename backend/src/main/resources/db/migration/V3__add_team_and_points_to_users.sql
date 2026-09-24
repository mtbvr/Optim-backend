ALTER TABLE users ADD COLUMN team VARCHAR(10) NOT NULL DEFAULT 'RED';
ALTER TABLE users ADD COLUMN points INT NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN bomb_charges INT NOT NULL DEFAULT 0;

-- Repartit les comptes existants en alternance RED/BLUE (par ordre de creation) pour
-- demarrer avec des equipes equilibrees.
WITH ranked AS (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn FROM users
)
UPDATE users
SET team = CASE WHEN ranked.rn % 2 = 0 THEN 'BLUE' ELSE 'RED' END
FROM ranked
WHERE users.id = ranked.id;
