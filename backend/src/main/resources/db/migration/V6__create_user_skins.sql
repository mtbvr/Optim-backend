ALTER TABLE users ADD COLUMN selected_skin VARCHAR(20);

CREATE TABLE user_skins
(
    user_id      UUID         NOT NULL REFERENCES users (id),
    skin_id      VARCHAR(20)  NOT NULL,
    purchased_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, skin_id)
);
