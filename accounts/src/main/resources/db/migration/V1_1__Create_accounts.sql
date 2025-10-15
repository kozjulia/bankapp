CREATE TABLE accounts
(
    id       BIGSERIAL  NOT NULL,
    user_id  BIGSERIAL  NOT NULL REFERENCES users (id),
    currency VARCHAR(3) NOT NULL,
    value    DECIMAL    NOT NULL DEFAULT 0,
    exists   boolean    NOT NULL DEFAULT true,
    CONSTRAINT accounts_pk PRIMARY KEY (id)
);