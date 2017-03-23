package no.nav.fo.exception;


/**
 * Unexpected number of results from database (typically more than one).
 */
public class UnexpectedRowCountException extends RuntimeException {
    public UnexpectedRowCountException(String s) {
        super(s);
    }
}
