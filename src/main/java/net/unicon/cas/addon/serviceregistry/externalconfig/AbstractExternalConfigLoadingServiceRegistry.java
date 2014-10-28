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

    private static final String REGEX_PREFIX = "^";

    protected static final String SERVICES_KEY = "services";

    private static final String SERVICES_ID_KEY = "serviceId";

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
        final List<RegisteredService> resolvedServices = new ArrayList<RegisteredService>();

        try {
            final Map<String, List<Map<String, Object>>> m = unmarshalServiceRegistryResourceIntoMap(externalServicesDefinitionSource);
            if (m != null) {
                for (Map<String, Object> record : m.get(SERVICES_KEY)) {
                    final String svcId = ((String) record.get(SERVICES_ID_KEY));
                    final RegisteredService svc = getRegisteredServiceInstance(svcId);
                    if (svc != null) {
                        resolvedServices.add(convertFromMap(record, svc.getClass()));
                        logger.debug("Unmarshaled {}: {}", svc.getClass().getSimpleName(), record);
                    }
                }
                synchronized (this.mutex) {
                    setRegisteredServices(resolvedServices);
                }
            }
        }
        catch (final Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public abstract Map<String, List<Map<String, Object>>> unmarshalServiceRegistryResourceIntoMap(final Resource jsonResource) throws IOException;

    public abstract <T extends RegisteredService> T convertFromMap(Map<String, Object> record,
                                                                   Class<? extends RegisteredService> registeredServiceType);


    /**
     * Constructs an instance of {@link net.unicon.cas.addon.registeredservices.RegisteredServiceWithAttributes} based on the
     * syntax of the pattern defined. If the pattern is considered a valid regular expression,
     * an instance of {@link RegexRegisteredServiceWithAttributes} is created. Otherwise,
     * {@link net.unicon.cas.addon.registeredservices.DefaultRegisteredServiceWithAttributes}.
     *
     * @param pattern the pattern of the service definition
     *
     * @return an instance of {@link net.unicon.cas.addon.registeredservices.RegisteredServiceWithAttributes}
     *
     * @see #isValidRegexPattern(String)
     */
    private RegisteredService getRegisteredServiceInstance(final String pattern) {
        if (isValidRegexPattern(pattern)) {
            return new RegexRegisteredServiceWithAttributes();
        }
        return new DefaultRegisteredServiceWithAttributes();
    }

    private boolean isValidRegexPattern(final String pattern) {
        try {
            if (pattern.startsWith(REGEX_PREFIX)) {
                Pattern.compile(pattern);
                return true;
            }
            return false;
        }
        catch (final PatternSyntaxException e) {
            logger.debug("Failed to identify [{}] as a regular expression", pattern);
            return false;
        }
    }
}
