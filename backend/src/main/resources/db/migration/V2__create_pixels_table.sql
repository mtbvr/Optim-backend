CREATE TABLE pixels
(
    x          INT          NOT NULL,
    y          INT          NOT NULL,
    color      VARCHAR(7)   NOT NULL,
    updated_by UUID         NOT NULL REFERENCES users (id),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    PRIMARY KEY (x, y)
);
