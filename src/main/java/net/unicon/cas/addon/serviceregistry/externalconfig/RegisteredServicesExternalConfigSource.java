package net.unicon.cas.addon.serviceregistry.externalconfig;

import net.unicon.cas.addon.spring.resource.ResourceChangeDetectingEventNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;

/**
 * A component that wraps {@link org.springframework.core.io.Resource} representing external config representation of
 * collection CAS' {@link org.jasig.cas.services.RegisteredService}s.
 *
 * This class implements dynamic reloading of the contents of the externally changed resource by implementing
 * application listener that reacts to {@link net.unicon.cas.addon.spring.resource.ResourceChangeDetectingEventNotifier
 * .ResourceChangedEvent}s and then delegates the unmarshalling of the external config contents to the configured
 * instance of a {@code RegisteredServicesExternalConfigUnmarshaller}.
 *
 * @author Dmitriy Kopylenko
 */
public class RegisteredServicesExternalConfigSource implements ApplicationListener<ResourceChangeDetectingEventNotifier
        .ResourceChangedEvent> {

    protected final Resource servicesConfigSource;

    private RegisteredServicesExternalConfigUnmarshaller<Resource> servicesUnmarshaller;

    protected final static Logger logger = LoggerFactory.getLogger(RegisteredServicesExternalConfigSource.class);

    public RegisteredServicesExternalConfigSource(final Resource servicesConfigSource,
                                                  final RegisteredServicesExternalConfigUnmarshaller<Resource> servicesUnmarshaller) {

        this.servicesConfigSource = servicesConfigSource;
        this.servicesUnmarshaller = servicesUnmarshaller;
    }

    @Override
    public void onApplicationEvent(ResourceChangeDetectingEventNotifier.ResourceChangedEvent resourceChangedEvent) {
        try {
            if (!resourceChangedEvent.getResourceUri().equals(this.servicesConfigSource.getURI())) {
                //Not our resource. Just get out of here.
                return;
            }
        }
        catch (final Throwable e) {
            logger.error("An exception is caught while trying to access JSON resource: ", e);
            return;
        }
        logger.debug("Received change event for external registered services resource {}. Reloading services...",
                resourceChangedEvent.getResourceUri());
        this.servicesUnmarshaller.unmarshalRegisteredServicesFrom(this.servicesConfigSource);
    }
}
