CREATE TABLE accounts
(
    id       BIGSERIAL  NOT NULL,
    user_id  BIGSERIAL  NOT NULL REFERENCES users (id),
    currency VARCHAR(3) NOT NULL,
    value    DECIMAL    NOT NULL,
    exists   boolean    NOT NULL,
    CONSTRAINT accounts_pk PRIMARY KEY (id)
);