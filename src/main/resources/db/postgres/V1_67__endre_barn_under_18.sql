DROP TABLE bruker_data_barn;

CREATE TABLE BRUKER_DATA_BARN
(
    BARN_IDENT          VARCHAR(30) NOT NULL PRIMARY KEY,
    BARN_FOEDSELSDATO   DATE        NOT NULL,
    BARN_DISKRESJONKODE VARCHAR(3)
);

CREATE TABLE FORELDREANSVAR
(
    FORESATT_IDENT VARCHAR(30) NOT NULL,
    BARN_IDENT     VARCHAR(30) NOT NULL,
    PRIMARY KEY (FORESATT_IDENT, BARN_IDENT)
);

ALTER TABLE FORELDREANSVAR
    ADD CONSTRAINT FK_FORESATT_IDENT FOREIGN KEY (FORESATT_IDENT) REFERENCES bruker_identer (ident)
        ON DELETE CASCADE
        ON UPDATE CASCADE;

ALTER TABLE FORELDREANSVAR
    ADD CONSTRAINT FK_BARN_IDENT FOREIGN KEY (BARN_IDENT) REFERENCES BRUKER_DATA_BARN (BARN_IDENT)
        ON UPDATE CASCADE;
