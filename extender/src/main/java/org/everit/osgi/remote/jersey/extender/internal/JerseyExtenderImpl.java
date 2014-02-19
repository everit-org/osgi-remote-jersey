/**
 * This file is part of Everit - Jersey Extender.
 *
 * Everit - Jersey Extender is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Jersey Extender is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Jersey Extender.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.remote.jersey.extender.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;

import org.everit.osgi.remote.jersey.extender.JerseyExtender;
import org.everit.osgi.remote.jersey.extender.JerseyExtenderConstants;
import org.everit.osgi.remote.jersey.extender.TrackedService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * An {@link ServiceTrackerCustomizer} that should track the services where the
 * {@link JerseyExtenderConstants#SERVICE_PROP_JERSEY_COMPONENT} property is defined.
 */
public class JerseyExtenderImpl implements ServiceTrackerCustomizer<Object, ServiceRegistration<Servlet>>,
        JerseyExtender {

    private static Dictionary<String, Object> createProperties(final ServiceReference<Object> reference) {
        Hashtable<String, Object> result = new Hashtable<String, Object>();
        String[] propertyKeys = reference.getPropertyKeys();
        for (String propertyKey : propertyKeys) {
            if (org.osgi.framework.Constants.SERVICE_ID.equals(propertyKey)) {
                result.put(JerseyExtenderConstants.SERVICE_PROP_TARGET_SERVICE_ID, reference.getProperty(propertyKey));
            } else if (org.osgi.framework.Constants.SERVICE_PID.equals(propertyKey)) {
                result.put(JerseyExtenderConstants.SERVICE_PROP_TARGET_SERVICE_PID, reference.getProperty(propertyKey));
            } else if (!JerseyExtenderConstants.SERVICE_PROP_TARGET_SERVICE_ID.equals(propertyKey)
                    && !JerseyExtenderConstants.SERVICE_PROP_TARGET_SERVICE_PID.equals(propertyKey)
                    && !JerseyExtenderConstants.SERVICE_PROP_JERSEY_COMPONENT.equals(propertyKey)
                    && !org.osgi.framework.Constants.OBJECTCLASS.equals(propertyKey)) {

                result.put(propertyKey, reference.getProperty(propertyKey));
            }
        }
        return result;
    }

    /**
     * The context of the extender bundle.
     */
    private final BundleContext bundleContext;

    /**
     * The Map of registered Jersey service factories by the OSGi services that were embedded.
     */
    private Map<ServiceReference<Object>, JerseyServletServiceFactory> serviceFactoriesByReferences =
            new ConcurrentHashMap<ServiceReference<Object>, JerseyServletServiceFactory>();

    /**
     * Simple constructor.
     * 
     * @param bundleContext
     *            The context of the extender bundle.
     */
    public JerseyExtenderImpl(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public ServiceRegistration<Servlet> addingService(final ServiceReference<Object> reference) {

        Bundle bundleOfReference = reference.getBundle();
        BundleContext contextOfReference = bundleOfReference.getBundleContext();
        Dictionary<String, ?> properties = JerseyExtenderImpl.createProperties(reference);

        JerseyServletServiceFactory serviceFactory = new JerseyServletServiceFactory(bundleContext, reference);

        ServiceRegistration<Servlet> servletSR = (ServiceRegistration<Servlet>) contextOfReference.registerService(
                Servlet.class.getName(),
                serviceFactory,
                properties);

        serviceFactory.setFactoryServiceId((Long) servletSR.getReference()
                .getProperty(org.osgi.framework.Constants.SERVICE_ID));
        serviceFactoriesByReferences.put(reference, serviceFactory);
        return servletSR;
    }

    @Override
    public List<TrackedService> getTrackedServices() {
        Set<Entry<ServiceReference<Object>, JerseyServletServiceFactory>> entrySet = serviceFactoriesByReferences
                .entrySet();
        List<TrackedService> result = new ArrayList<TrackedService>();
        for (Entry<ServiceReference<Object>, JerseyServletServiceFactory> entry : entrySet) {
            JerseyServletServiceFactory jerseyServletServiceFactory = entry.getValue();
            boolean collection = jerseyServletServiceFactory.isCollection();

            ServiceReference<Object> serviceReference = entry.getKey();
            Long serviceId = (Long) serviceReference.getProperty(org.osgi.framework.Constants.SERVICE_ID);
            Object service = bundleContext.getService(serviceReference);
            String info = service.toString();
            bundleContext.ungetService(serviceReference);

            result.add(new TrackedService(serviceId, jerseyServletServiceFactory.getFactoryServiceId(), collection,
                    info));

        }
        return result;
    }

    @Override
    public void modifiedService(final ServiceReference<Object> reference,
            final ServiceRegistration<Servlet> serviceRegistration) {
        JerseyServletServiceFactory jerseyServletServiceFactory = serviceFactoriesByReferences.get(reference);
        jerseyServletServiceFactory.referesh();
        Dictionary<String, Object> properties = JerseyExtenderImpl.createProperties(reference);
        serviceRegistration.setProperties(properties);
    }

    @Override
    public void removedService(final ServiceReference<Object> reference,
            final ServiceRegistration<Servlet> serviceRegistration) {
        serviceFactoriesByReferences.remove(reference);
        serviceRegistration.unregister();
    }
}
