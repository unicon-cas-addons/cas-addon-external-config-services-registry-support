package net.unicon.cas.addon.serviceregistry.externalconfig;

import net.unicon.cas.addon.spring.resource.ResourceChangeDetectingEventNotifier;
import org.jasig.cas.services.ReloadableServicesManager;
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
 * instance of a {@code RegisteredServicesExternalConfigUnmarshaller} as well as calls the {@code reload} method on the configured
 * {@link org.jasig.cas.services.ReloadableServicesManager}.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
public class RegisteredServicesExternalConfigSource implements ApplicationListener<ResourceChangeDetectingEventNotifier
        .ResourceChangedEvent> {

    protected final Resource servicesConfigSource;

    private RegisteredServicesExternalConfigUnmarshaller<Resource> servicesUnmarshaller;

    private ReloadableServicesManager reloadableServicesManager;

    private final Object mutex = new Object();

    protected final static Logger logger = LoggerFactory.getLogger(RegisteredServicesExternalConfigSource.class);

    public RegisteredServicesExternalConfigSource(final Resource servicesConfigSource,
                                                  final RegisteredServicesExternalConfigUnmarshaller<Resource> servicesUnmarshaller,
                                                  final ReloadableServicesManager reloadableServicesManager) {

        this.servicesConfigSource = servicesConfigSource;
        this.servicesUnmarshaller = servicesUnmarshaller;
        this.reloadableServicesManager = reloadableServicesManager;
        this.servicesUnmarshaller.unmarshalRegisteredServicesFrom(this.servicesConfigSource);
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
            logger.error("An exception is caught while trying to access external registered services resource: ", e);
            return;
        }
        logger.debug("Received changed event for external registered services resource {}. Reloading services...",
                resourceChangedEvent.getResourceUri());
        synchronized (this.mutex) {
            this.servicesUnmarshaller.unmarshalRegisteredServicesFrom(this.servicesConfigSource);
            this.reloadableServicesManager.reload();
        }
    }
}
