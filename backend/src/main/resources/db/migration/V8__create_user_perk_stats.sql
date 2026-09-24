CREATE TABLE user_perk_stats
(
    user_id        UUID        NOT NULL REFERENCES users (id),
    perk_type      VARCHAR(20) NOT NULL,
    purchase_count INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, perk_type)
);
