//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-***REMOVED***.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.11.09 at 11:38:41 AM CET 
//


package no.nav.fo.loependeytelser;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the no.nav.melding.virksomhet.loependeytelser.v1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LoependeYtelser_QNAME = new QName("http://nav.no/melding/virksomhet/loependeYtelser/v1", "loependeYtelser");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: no.nav.melding.virksomhet.loependeytelser.v1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link LoependeYtelser }
     * 
     */
    public LoependeYtelser createLoependeYtelser() {
        return new LoependeYtelser();
    }

    /**
     * Create an instance of {@link Dagpengetellere }
     * 
     */
    public Dagpengetellere createDagpengetellere() {
        return new Dagpengetellere();
    }

    /**
     * Create an instance of {@link AAPtellere }
     * 
     */
    public AAPtellere createAAPtellere() {
        return new AAPtellere();
    }

    /**
     * Create an instance of {@link Rettighetstyper }
     * 
     */
    public Rettighetstyper createRettighetstyper() {
        return new Rettighetstyper();
    }

    /**
     * Create an instance of {@link LoependeVedtak }
     * 
     */
    public LoependeVedtak createLoependeVedtak() {
        return new LoependeVedtak();
    }

    /**
     * Create an instance of {@link Periode }
     * 
     */
    public Periode createPeriode() {
        return new Periode();
    }

    /**
     * Create an instance of {@link Sakstyper }
     * 
     */
    public Sakstyper createSakstyper() {
        return new Sakstyper();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoependeYtelser }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://nav.no/melding/virksomhet/loependeYtelser/v1", name = "loependeYtelser")
    public JAXBElement<LoependeYtelser> createLoependeYtelser(LoependeYtelser value) {
        return new JAXBElement<LoependeYtelser>(_LoependeYtelser_QNAME, LoependeYtelser.class, null, value);
    }

}
