CREATE TABLE user_achievements
(
    user_id        UUID        NOT NULL REFERENCES users (id),
    achievement_id VARCHAR(30) NOT NULL,
    unlocked_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, achievement_id)
);
