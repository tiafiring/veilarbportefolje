
CREATE TABLE DIALOG
(
    AKTOERID        VARCHAR(20) NOT NULL,
    VENTER_PA_BRUKER        TIMESTAMP,
    VENTER_PA_NAV           TIMESTAMP,
    SIST_OPPDATERT          TIMESTAMP,

    PRIMARY KEY (AKTOERID)
);