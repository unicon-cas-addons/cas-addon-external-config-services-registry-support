package net.unicon.cas.addon.serviceregistry.externalconfig;

import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistryDao;

import java.util.ArrayList;
import java.util.List;

/**
 * A convenient wrapper around CAS' in-memory service registry useful for external store registry implementations
 * that un-marshal external representations into {@link org.jasig.cas.services.RegisteredService} object model and store them
 * with the in-memory one at runtime.
 *
 * <strong>Note: </strong> this class does not assume the responsibility for thread safety and so the clients/subclasses should
 * take care of that.
 *
 * @author Dmitriy Kopylenko
 * @since 1.0.0
 */
public class InMemoryServiceRegistryDaoWrapper implements ServiceRegistryDao {

    private final InMemoryServiceRegistryDaoImpl delegate = new InMemoryServiceRegistryDaoImpl();

    @Override
    public RegisteredService save(RegisteredService registeredService) {
        return this.delegate.save(registeredService);
    }

    @Override
    public boolean delete(RegisteredService registeredService) {
        return this.delegate.delete(registeredService);
    }

    @Override
    public List<RegisteredService> load() {
        return this.delegate.load();
    }

    @Override
    public RegisteredService findServiceById(long id) {
        return this.delegate.findServiceById(id);
    }

    public void setRegisteredServices(final List<RegisteredService> registeredServices) {
        this.delegate.setRegisteredServices(registeredServices);
    }
}
