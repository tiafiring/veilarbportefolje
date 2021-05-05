CREATE TABLE VEDTAKSTATUS
(
    AKTOERID               VARCHAR(20) NOT NULL,
    VEDTAKID               VARCHAR(20) NOT NULL,
    VEDTAKSTATUS           VARCHAR(40),
    INNSATSGRUPPE          VARCHAR(40),
    HOVEDMAL               VARCHAR(30),
    ANSVARLIG_VEILDERIDENT VARCHAR(20),
    ANSVARLIG_VEILDERNAVN  VARCHAR(60),
    ENDRET_TIDSPUNKT       TIMESTAMP,
    PRIMARY KEY (AKTOERID)
);

DROP VIEW BRUKER;
CREATE VIEW BRUKER AS
SELECT OD.AKTOERID,
       OD.OPPFOLGING,
       OD.STARTDATO,
       OD.NY_FOR_VEILEDER,
       OD.VEILEDERID,
       OD.MANUELL,
       OBA.FODSELSNR,
       OBA.FORNAVN,
       OBA.ETTERNAVN,
       OBA.NAV_KONTOR,
       OBA.ISERV_FRA_DATO,
       OBA.FORMIDLINGSGRUPPEKODE,
       OBA.KVALIFISERINGSGRUPPEKODE,
       OBA.RETTIGHETSGRUPPEKODE,
       OBA.HOVEDMAALKODE,
       OBA.SIKKERHETSTILTAK_TYPE_KODE,
       OBA.DISKRESJONSKODE,
       OBA.HAR_OPPFOLGINGSSAK,
       OBA.SPERRET_ANSATT,
       OBA.ER_DOED,
       D.VENTER_PA_BRUKER,
       D.VENTER_PA_NAV,
       V.VEDTAKSTATUS,
       V.ANSVARLIG_VEILDERNAVN as VEDTAKSTATUS_ANSVARLIG_VEILDERNAVN,
       V.ENDRET_TIDSPUNKT      as VEDTAKSTATUS_ENDRET_TIDSPUNKT
FROM OPPFOLGING_DATA OD
         LEFT JOIN OPPFOLGINGSBRUKER_ARENA OBA ON OBA.AKTOERID = OD.AKTOERID
         LEFT JOIN DIALOG D ON D.AKTOERID = OD.AKTOERID
         LEFT JOIN VEDTAKSTATUS V on V.AKTOERID = OD.AKTOERID;
