CREATE TABLE SISTE_ENDRING (
    AKTOERID VARCHAR(20) NOT NULL,
    SISTE_ENDRING_KATEGORI VARCHAR(45),
    SISTE_ENDRING_TIDSPUNKT TIMESTAMP,
    PRIMARY KEY (AKTOERID));

DROP VIEW VW_PORTEFOLJE_INFO;

CREATE VIEW VW_PORTEFOLJE_INFO AS
(
SELECT MAP.AKTOERID                      AS AKTOERID,
       OB.PERSON_ID                      AS PERSON_ID,
       OB.FODSELSNR                      AS FODSELSNR,
       OB.FORNAVN                        AS FORNAVN,
       OB.ETTERNAVN                      AS ETTERNAVN,
       OB.NAV_KONTOR                     AS NAV_KONTOR,
       OB.FORMIDLINGSGRUPPEKODE          AS FORMIDLINGSGRUPPEKODE,
       OB.ISERV_FRA_DATO                 AS ISERV_FRA_DATO,
       OB.KVALIFISERINGSGRUPPEKODE       AS KVALIFISERINGSGRUPPEKODE,
       OB.RETTIGHETSGRUPPEKODE           AS RETTIGHETSGRUPPEKODE,
       OB.HOVEDMAALKODE                  AS HOVEDMAALKODE,
       OB.SIKKERHETSTILTAK_TYPE_KODE     AS SIKKERHETSTILTAK_TYPE_KODE,
       OB.FR_KODE                        AS FR_KODE,
       OB.SPERRET_ANSATT                 AS SPERRET_ANSATT,
       OB.ER_DOED                        AS ER_DOED,
       OB.DOED_FRA_DATO                  AS DOED_FRA_DATO,
       OD.VEILEDERIDENT                  AS VEILEDERIDENT,
       OD.NY_FOR_VEILEDER                AS NY_FOR_VEILEDER,
       OD.OPPFOLGING                     AS OPPFOLGING,
       DIA.VENTER_PA_BRUKER              AS VENTERPASVARFRABRUKER,
       DIA.VENTER_PA_NAV                 AS VENTERPASVARFRANAV,
       BD.YTELSE                         AS YTELSE,
       BD.UTLOPSDATO                     AS UTLOPSDATO,
       BD.UTLOPSDATOFASETT               AS UTLOPSDATOFASETT,
       BD.DAGPUTLOPUKE                   AS DAGPUTLOPUKE,
       BD.DAGPUTLOPUKEFASETT             AS DAGPUTLOPUKEFASETT,
       BD.PERMUTLOPUKE                   AS PERMUTLOPUKE,
       BD.PERMUTLOPUKEFASETT             AS PERMUTLOPUKEFASETT,
       BD.AAPMAXTIDUKE                   AS AAPMAXTIDUKE,
       BD.AAPMAXTIDUKEFASETT             AS AAPMAXTIDUKEFASETT,
       BD.AAPUNNTAKDAGERIGJEN            AS AAPUNNTAKDAGERIGJEN,
       BD.AAPUNNTAKUKERIGJENFASETT       AS AAPUNNTAKUKERIGJENFASETT,
       BD.NYESTEUTLOPTEAKTIVITET         AS NYESTEUTLOPTEAKTIVITET,
       BD.AKTIVITET_START                AS AKTIVITET_START,
       BD.NESTE_AKTIVITET_START          AS NESTE_AKTIVITET_START,
       BD.FORRIGE_AKTIVITET_START        AS FORRIGE_AKTIVITET_START,
       OB.TIDSSTEMPEL                    AS OPPDATERT_ARENA,
       OD.OPPDATERT_PORTEFOLJE           AS OPPDATERT_BRUKERDATA,
       DIA.OPPDATERT_PORTEFOLJE          AS OPPDATERT_DIALOG,
       MAP.OPPRETTET_TIDSPUNKT           AS MAPPING_OPPRETTET,
       OB.TIDSSTEMPEL                    AS TIDSSTEMPEL,
       OD.MANUELL                        AS MANUELL,
       OD.STARTDATO                      AS OPPFOLGING_STARTDATO,
       K.RESERVASJON                     AS RESERVERTIKRR,
       ARB.ARBEIDSLISTE_AKTIV            AS ARBEIDSLISTE_AKTIV,
       ARB.OVERSKRIFT                    AS ARBEIDSLISTE_OVERSKRIFT,
       ARB.KOMMENTAR                     AS ARBEIDSLISTE_KOMMENTAR,
       ARB.FRIST                         AS ARBEIDSLISTE_FRIST,
       ARB.SIST_ENDRET_AV_VEILEDERIDENT  AS ARBEIDSLISTE_ENDRET_AV,
       ARB.ENDRINGSTIDSPUNKT             AS ARBEIDSLISTE_ENDRET_TID,
       ARB.KATEGORI                      AS ARBEIDSLISTE_KATEGORI,
       VS.VEDTAK_STATUS_ENDRET_TIDSPUNKT AS VEDTAK_STATUS_ENDRET_TIDSPUNKT,
       VS.VEDTAKSTATUS                   AS VEDTAKSTATUS,
       BR.BRUKERS_SITUASJON              AS BRUKERS_SITUASJON,
       BR.UTDANNING                      AS UTDANNING,
       BR.UTDANNING_BESTATT              AS UTDANNING_BESTATT,
       BR.UTDANNING_GODKJENT             AS UTDANNING_GODKJENT,
       PF.PROFILERING_RESULTAT           AS PROFILERING_RESULTAT,
       CV.HAR_DELT_CV                    AS HAR_DELT_CV,
       SE.SISTE_ENDRING_KATEGORI         AS SISTE_ENDRING_KATEGORI,
       SE.SISTE_ENDRING_TIDSPUNKT        AS SISTE_ENDRING_TIDSPUNKT

FROM OPPFOLGINGSBRUKER OB
         LEFT JOIN AKTOERID_TO_PERSONID MAP ON MAP.PERSONID = OB.PERSON_ID AND MAP.GJELDENE = 1
         LEFT JOIN OPPFOLGING_DATA OD ON OD.AKTOERID = MAP.AKTOERID
         LEFT JOIN DIALOG DIA ON DIA.AKTOERID = MAP.AKTOERID
         LEFT JOIN BRUKER_REGISTRERING BR ON BR.AKTOERID = MAP.AKTOERID
         LEFT JOIN BRUKER_DATA BD ON BD.PERSONID = OB.PERSON_ID
         LEFT JOIN KRR K ON K.FODSELSNR = OB.FODSELSNR
         LEFT JOIN BRUKER_PROFILERING PF ON PF.AKTOERID = MAP.AKTOERID
         LEFT JOIN BRUKER_CV CV ON CV.AKTOERID = MAP.AKTOERID
         LEFT JOIN SISTE_ENDRING SE ON SE.AKTOERID = MAP.AKTOERID
         LEFT JOIN (SELECT * FROM VEDTAKSTATUS_DATA VS WHERE VEDTAKSTATUS != 'VEDTAK_SENDT') VS
                   ON VS.AKTOERID = MAP.AKTOERID
         LEFT JOIN (SELECT 'J' AS ARBEIDSLISTE_AKTIV, A.* FROM ARBEIDSLISTE A) ARB ON ARB.AKTOERID = MAP.AKTOERID
    );