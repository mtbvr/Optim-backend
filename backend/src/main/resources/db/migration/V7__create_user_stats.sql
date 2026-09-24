CREATE TABLE user_stats
(
    user_id                   UUID PRIMARY KEY REFERENCES users (id),
    pixels_placed             INT NOT NULL DEFAULT 0,
    captures_made             INT NOT NULL DEFAULT 0,
    combos_triggered          INT NOT NULL DEFAULT 0,
    bombs_used                INT NOT NULL DEFAULT 0,
    team_pool_contributions   INT NOT NULL DEFAULT 0
);
