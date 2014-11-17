package net.unicon.cas.addon.serviceregistry.externalconfig;

import net.unicon.cas.addon.registeredservices.DefaultRegisteredServiceWithAttributes;
import net.unicon.cas.addon.registeredservices.RegexRegisteredServiceWithAttributes;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A base implementation for external config unmarshalling registries which wraps around CAS' in-memory service registry for im-memory storage
 * after external payload unmarshalling.
 *
 * Actual unmarshalling from external formats is deferred to concrete subclasses.
 *
 * This class is thread-safe.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
public abstract class AbstractExternalConfigLoadingServiceRegistry implements ServiceRegistryDao,
        RegisteredServicesExternalConfigUnmarshaller<Resource> {

    private final InMemoryServiceRegistryDaoImpl delegate = new InMemoryServiceRegistryDaoImpl();

    protected final Object mutex = new Object();

    protected static final Logger logger = LoggerFactory.getLogger(AbstractExternalConfigLoadingServiceRegistry.class);

    @Override
    public RegisteredService save(RegisteredService registeredService) {
        synchronized (this.mutex) {
            return this.delegate.save(registeredService);
        }
    }

    @Override
    public boolean delete(RegisteredService registeredService) {
        synchronized (this.mutex) {
            return this.delegate.delete(registeredService);
        }
    }

    @Override
    public List<RegisteredService> load() {
        synchronized (this.mutex) {
            return this.delegate.load();
        }
    }

    @Override
    public RegisteredService findServiceById(long id) {
        synchronized (this.mutex) {
            return this.delegate.findServiceById(id);
        }
    }

    public void setRegisteredServices(final List<RegisteredService> registeredServices) {
        synchronized (this.mutex) {
            this.delegate.setRegisteredServices(registeredServices);
        }
    }

    @Override
    public final void unmarshalRegisteredServicesFrom(final Resource externalServicesDefinitionSource) {
        logger.info("Unmarshalling registered services from {}", externalServicesDefinitionSource);
        try {
            final List<RegisteredService> resolvedServices = doUnmarshal(externalServicesDefinitionSource);
            if (resolvedServices != null) {
                synchronized (this.mutex) {
                    setRegisteredServices(resolvedServices);
                }
            }
        }
        catch (Throwable e) {
            logger.error("An error occurred during unmarshalling of registered services from resource {}", externalServicesDefinitionSource);
            throw new ExternalConfigServiceRegistryException(e);
        }
    }

    /**
     * Subclasses will take care of the actual mechanics of unmarshalling.
     *
     * @param externalServicesDefinitionSource the external resource holding a serialized representation of all the services.
     *
     * @return an un-marshalled representation of all the registered services or NULL if no services could be unmarshalled.
     */
    protected abstract List<RegisteredService> doUnmarshal(Resource externalServicesDefinitionSource);
}
