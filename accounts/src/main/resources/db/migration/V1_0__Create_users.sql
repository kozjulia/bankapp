CREATE TABLE users
(
    id        BIGSERIAL    NOT NULL,
    login     VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    name      VARCHAR(255) NOT NULL,
    birthdate DATE         NOT NULL,
    CONSTRAINT users_pk PRIMARY KEY (id)
);