package net.unicon.cas.addon.serviceregistry.externalconfig;

/**
 * Strategy interface intended for behavior parametrization by external config service registries implementations
 * to unmarshal external config representations sourced from parametrized type <i>T</i> into
 * CAS' canonical {@link org.jasig.cas.services.RegisteredService} model state held internally by those service registries
 * implementations.
 *
 * A common strategy is for service registry implementation to implement this interface.
 *
 * This API is Java 8 Lambda-ready by means of conforming to a <a href=http://docs.oracle.com/javase/specs/jls/se8/html/jls-9
 * .html#jls-9.8>Functional Interface</a> design.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
public interface RegisteredServicesExternalConfigUnmarshaller<T> {

    void unmarshalRegisteredServicesFrom(T source);
}
