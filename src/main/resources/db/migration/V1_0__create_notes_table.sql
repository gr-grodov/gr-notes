CREATE TABLE notes (
    id                        UUID              PRIMARY KEY DEFAULT uuidv7(),
    user_sub                  VARCHAR(255)      NOT NULL,
    title                     VARCHAR(255)      NOT NULL,
    content                   TEXT              NOT NULL DEFAULT '',
    created_at                TIMESTAMPTZ       NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ       NOT NULL DEFAULT now()
);

CREATE INDEX idx_notes_user_sub ON notes (user_sub);