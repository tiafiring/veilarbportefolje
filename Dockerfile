FROM navikt/pus-nais-java-app
ADD jvmtop-0.8.0 /jvmtop
ENV JVM_OPTS=-Xmx2048\ -Xmx4098
ADD /target/veilarbportefolje /app
