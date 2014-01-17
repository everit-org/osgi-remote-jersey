package org.everit.osgi.remote.jersey.extender;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Servlet;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * An {@link ServiceTrackerCustomizer} that should track the services where the {@link Constants#SERVICE_PROP_EXTEND}
 * property is defined.
 */
public class JerseyExtender implements ServiceTrackerCustomizer<Object, ServiceRegistration<Servlet>> {

    private BundleContext bundleContext;

    @Override
    public ServiceRegistration<Servlet> addingService(ServiceReference<Object> reference) {

        Bundle bundleOfReference = reference.getBundle();
        BundleContext contextOfReference = bundleOfReference.getBundleContext();
        Dictionary<String, ?> properties = createProperties(reference);

        JerseyServletServiceFactory serviceFactory = new JerseyServletServiceFactory(bundleContext, reference);
        ServiceRegistration<Servlet> servletSR = (ServiceRegistration<Servlet>) contextOfReference.registerService(
                Servlet.class.getName(),
                serviceFactory,
                properties);
        return servletSR;
    }

    private static Dictionary<String, Object> createProperties(ServiceReference<Object> reference) {
        Hashtable<String, Object> result = new Hashtable<>();
        String[] propertyKeys = reference.getPropertyKeys();
        for (String propertyKey : propertyKeys) {
            if (org.osgi.framework.Constants.SERVICE_ID.equals(propertyKey)) {
                result.put(Constants.SERVICE_PROP_TARGET_SERVICE_ID, reference.getProperty(propertyKey));
            } else if (org.osgi.framework.Constants.SERVICE_PID.equals(propertyKey)) {
                result.put(Constants.SERVICE_PROP_TARGET_SERVICE_PID, reference.getProperty(propertyKey));
            } else if (!Constants.SERVICE_PROP_TARGET_SERVICE_ID.equals(propertyKey)
                    && !Constants.SERVICE_PROP_TARGET_SERVICE_PID.equals(propertyKey)
                    && !Constants.SERVICE_PROP_EXTEND.equals(propertyKey)
                    && !org.osgi.framework.Constants.OBJECTCLASS.equals(propertyKey)) {

                result.put(propertyKey, reference.getProperty(propertyKey));
            }
        }
        return result;
    }

    @Override
    public void modifiedService(ServiceReference<Object> reference, ServiceRegistration<Servlet> serviceRegistration) {
        Dictionary<String, Object> properties = createProperties(reference);
        serviceRegistration.setProperties(properties);
    }

    @Override
    public void removedService(ServiceReference<Object> reference, ServiceRegistration<Servlet> serviceRegistration) {
        serviceRegistration.unregister();
    }

}
