ALTER TABLE pixels ADD COLUMN team VARCHAR(10);

-- Les pixels existants adoptent l'equipe (et donc la couleur fixe) de leur poseur : le jeu
-- passe d'une palette libre a une couleur par equipe, la grille actuelle est donc recoloree.
UPDATE pixels
SET team = u.team,
    color = CASE WHEN u.team = 'RED' THEN '#E50000' ELSE '#0083C7' END
FROM users u
WHERE pixels.updated_by = u.id;

ALTER TABLE pixels ALTER COLUMN team SET NOT NULL;
