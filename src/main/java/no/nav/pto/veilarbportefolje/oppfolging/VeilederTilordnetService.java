package no.nav.pto.veilarbportefolje.oppfolging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import no.nav.common.types.identer.AktorId;
import no.nav.pto.veilarbportefolje.arbeidsliste.ArbeidslisteService;
import no.nav.pto.veilarbportefolje.domene.value.VeilederId;
import no.nav.pto.veilarbportefolje.kafka.KafkaCommonConsumerService;
import no.nav.pto.veilarbportefolje.opensearch.OpensearchIndexerV2;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class VeilederTilordnetService extends KafkaCommonConsumerService<VeilederTilordnetDTO> {
    private final OppfolgingService oppfolgingService;
    private final OppfolgingRepositoryV2 oppfolgingRepositoryV2;
    private final ArbeidslisteService arbeidslisteService;
    private final OpensearchIndexerV2 opensearchIndexerV2;

    @Override
    public void behandleKafkaMeldingLogikk(VeilederTilordnetDTO dto) {
        final AktorId aktoerId = dto.getAktorId();
        final VeilederId veilederId = dto.getVeilederId();

        tilordneVeileder(aktoerId, veilederId);
    }

    public void tilordneVeileder(AktorId aktoerId, VeilederId veilederId) {
        oppfolgingRepositoryV2.settVeileder(aktoerId, veilederId);

        kastErrorHvisBrukerSkalVaereUnderOppfolging(aktoerId, veilederId);
        opensearchIndexerV2.oppdaterVeileder(aktoerId, veilederId);
        log.info("Oppdatert bruker: {}, til veileder med id: {}", aktoerId, veilederId);

        final boolean harByttetNavKontor = arbeidslisteService.brukerHarByttetNavKontor(aktoerId);
        if (harByttetNavKontor) {
            arbeidslisteService.slettArbeidsliste(aktoerId);
        }
    }

    private void kastErrorHvisBrukerSkalVaereUnderOppfolging(AktorId aktorId, VeilederId veilederId) {
        if (hentVeileder(aktorId).equals(veilederId)) {
            return;
        }
        boolean erUnderOppfolgingIVeilarboppfolging = oppfolgingService.hentUnderOppfolging(aktorId);
        if (erUnderOppfolgingIVeilarboppfolging) {
            throw new IllegalStateException("Fikk 'veileder melding' på bruker som enda ikke er under oppfølging i veilarbportefolje");
        }
    }

    private VeilederId hentVeileder(AktorId aktoerId) {
        return oppfolgingRepositoryV2.hentVeilederForBruker(aktoerId)
                .orElse(VeilederId.of(null));
    }
}
