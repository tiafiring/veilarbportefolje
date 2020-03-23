package no.nav.pto.veilarbportefolje.registrering;

import io.vavr.control.Try;
import no.nav.arbeid.soker.registrering.ArbeidssokerRegistrertEvent;
import no.nav.pto.veilarbportefolje.database.BrukerRepository;
import no.nav.pto.veilarbportefolje.domene.AktoerId;
import no.nav.pto.veilarbportefolje.domene.Fnr;
import no.nav.pto.veilarbportefolje.registrering.domene.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Collections;

import static no.nav.pto.veilarbportefolje.config.LocalJndiContextConfig.setupInMemoryDatabase;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PopulerDataFraRegistreringTest {
    private JdbcTemplate jdbcTemplate = new JdbcTemplate(setupInMemoryDatabase());
    private RegistreringRepository registreringRepository = new RegistreringRepository(jdbcTemplate);
    private RegistreringService registreringService = new RegistreringService(registreringRepository);
    private BrukerRepository brukerRepository = mock(BrukerRepository.class);
    private VeilarbregistreringClient veilarbregistreringClient;
    private PopulerDataFraRegistrering populerDataFraRegistrering;



    @Before
    public void setUp() {
        System.setProperty("VEILARBREGISTRERING_URL", "thisUrlMustBeSetAtLeastToADummyValue");
        this.veilarbregistreringClient = mock(VeilarbregistreringClient.class);

        when(brukerRepository.hentAlleBrukereUnderOppfolgingRegistrering(anyInt(), anyInt())).thenReturn(Collections.singletonList(
                new HentRegistreringDTO(AktoerId.of("123456789"), Fnr.of("12346789101"))));

        this.populerDataFraRegistrering = new PopulerDataFraRegistrering(registreringService, brukerRepository, veilarbregistreringClient);
        jdbcTemplate.execute("truncate table BRUKER_REGISTRERING");

    }


    @Test
    public void skallHanteraAttOppfolgingsBrukerenIkkeHarRegistrertSig() {
        when(veilarbregistreringClient.hentRegistrering(any(Fnr.class))).thenReturn(Try.success(null));
        AktoerId aktoerId = AktoerId.of("123456789");
        populerDataFraRegistrering.populerMedBrukerRegistrering(0,1);

        assertThat(registreringRepository.hentBrukerRegistrering(aktoerId)).isEqualTo(null);

    }

    @Test
    public void skallHanteraAttOppfolgingsBrukerenIkkeHarSituasjon() {
        OrdinaerBrukerRegistrering ordinaerBrukerRegistrering = new OrdinaerBrukerRegistrering();
        ordinaerBrukerRegistrering.setBesvarelse(null);
        BrukerRegistreringWrapper brukerRegistreringWrapper = new BrukerRegistreringWrapper().setRegistrering(ordinaerBrukerRegistrering);

        when(veilarbregistreringClient.hentRegistrering(any(Fnr.class))).thenReturn(Try.success(brukerRegistreringWrapper));
        AktoerId aktoerId = AktoerId.of("123456789");
        populerDataFraRegistrering.populerMedBrukerRegistrering(0,1);

        assertThat(registreringRepository.hentBrukerRegistrering(aktoerId)).isEqualTo(ArbeidssokerRegistrertEvent.newBuilder().setAktorid("123456789").build());
    }

    @Test
    public void skallSetteInBrukereSomHarSituasjon() {
        OrdinaerBrukerRegistrering ordinaerBrukerRegistrering = new OrdinaerBrukerRegistrering();
        Besvarelse besvarelse = new Besvarelse();
        besvarelse.setDinSituasjon(DinSituasjonSvar.ER_PERMITTERT);

        ordinaerBrukerRegistrering.setBesvarelse(besvarelse);
        BrukerRegistreringWrapper brukerRegistreringWrapper = new BrukerRegistreringWrapper().setRegistrering(ordinaerBrukerRegistrering);

        when(veilarbregistreringClient.hentRegistrering(any(Fnr.class))).thenReturn(Try.success(brukerRegistreringWrapper));
        AktoerId aktoerId = AktoerId.of("123456789");
        populerDataFraRegistrering.populerMedBrukerRegistrering(0,1);

        assertThat(registreringRepository.hentBrukerRegistrering(aktoerId).getBrukersSituasjon()).isEqualTo(DinSituasjonSvar.ER_PERMITTERT.name());
    }
}