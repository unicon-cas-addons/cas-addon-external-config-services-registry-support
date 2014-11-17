package net.unicon.cas.addon.serviceregistry.externalconfig;

/**
 * Runtime exception indicating any non-recoverable errors raised during parsing of external resources holding registered services representations.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
public class ExternalConfigServiceRegistryException extends RuntimeException {

    public ExternalConfigServiceRegistryException(Throwable cause) {
        super(cause);
    }
}
