CREATE TABLE OPPFOLGING_DATA
(
    AKTOERID        VARCHAR(20) NOT NULL,
    VEILEDERID      VARCHAR(20),
    OPPFOLGING      boolean DEFAULT false,
    NY_FOR_VEILEDER boolean DEFAULT false,
    MANUELL         boolean DEFAULT false,
    STARTDATO       TIMESTAMP,

    PRIMARY KEY (AKTOERID)
);

CREATE TABLE OPPFOLGING_DATA
(
    AKTOERID        VARCHAR(20) NOT NULL,
    VEILEDERID      VARCHAR(20),
    OPPFOLGING      boolean DEFAULT false,
    NY_FOR_VEILEDER boolean DEFAULT false,
    MANUELL         boolean DEFAULT false,
    STARTDATO       TIMESTAMP,

    PRIMARY KEY (AKTOERID)
);

CREATE UNIQUE INDEX OPPFOLGING_DATA_IDX ON OPPFOLGING_DATA(VEILEDERID);

CREATE VIEW BRUKER AS
    SELECT AKTOERID, OPPFOLGING, NY_FOR_VEILEDER, VEILEDERID
    FROM OPPFOLGING_DATA;
