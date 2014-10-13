package net.unicon.cas.addon.serviceregistry.externalconfig;

import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;

import java.util.List;

/**
 * A convenient wrapper around CAS' in-memory service registry useful for external store registry implementations
 * that un-marshal external representations into {@link org.jasig.cas.services.RegisteredService} object model and store them
 * with the in-memory one at runtime.
 *
 * This class is thread-safe.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
public class InMemoryServiceRegistryDaoWrapper implements ServiceRegistryDao {

    private final InMemoryServiceRegistryDaoImpl delegate = new InMemoryServiceRegistryDaoImpl();

    protected final Object mutex = new Object();

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
}
