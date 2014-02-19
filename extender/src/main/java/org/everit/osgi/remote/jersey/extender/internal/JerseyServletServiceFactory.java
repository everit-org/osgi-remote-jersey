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
import java.util.Collection;
import java.util.List;

import javax.servlet.Servlet;

import org.everit.osgi.remote.jersey.extender.JerseyExtenderConstants;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * For each service request a new Jersey servlet is created. It is done like this due to the reason that if more
 * whiteboard extenders want to pick up the same REST service, normally we do not want to share the servlet context and
 * the session.
 */
public class JerseyServletServiceFactory implements ServiceFactory<Servlet> {

    private static boolean isCollection(final ServiceReference<Object> reference) {
        String[] objectClass = (String[]) reference.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
        boolean collection = false;
        String collectionClassName = Collection.class.getName();
        for (int i = 0, n = objectClass.length; (i < n) && !collection; i++) {
            collection = collectionClassName.equals(objectClass[i]);
        }
        return collection;
    }

    /**
     * The service reference that contains the component(s) and properties that are embedded into the Jersey servlet.
     */
    private final ServiceReference<Object> reference;

    /**
     * A flag that indicates if this reference points to a collection of components or the reference is one component
     * instance.
     */
    private final boolean collection;

    /**
     * The context of the extender bundle. The extender bundle is used to get a new embedded service instance from the
     * service registry.
     */
    private final BundleContext extenderContext;

    /**
     * The servlet instances that are provided for the requesting bundles.
     */
    private List<ServletContainer> providedServlets = new ArrayList<ServletContainer>();

    /**
     * The id of the OSGi service that holds this instance.
     */
    private Long factoryServiceId;

    /**
     * Simple constructor that sets all the final fields plus generates an initial resource configuration for Jersey.
     * 
     * @param extenderContext
     *            The context of the extender bundle.
     * @param reference
     *            The service reference that is embedded into the Jersey servlet.
     */
    public JerseyServletServiceFactory(final BundleContext extenderContext, final ServiceReference<Object> reference) {
        this.reference = reference;
        this.extenderContext = extenderContext;
        collection = JerseyServletServiceFactory.isCollection(reference);
    }

    /**
     * Creates a configuration for the Jersey servlet. The logic of adding components and properties is defined here.
     * 
     * @return
     */
    private ResourceConfig createResourceConfig() {
        ResourceConfig lResourceConfig = new ResourceConfig();
        Object service = extenderContext.getService(reference);
        // Adding the components
        if (!collection) {
            // If it is not a collection, the service instance is the component itself
            lResourceConfig.register(service);
        } else {
            // If this is a collection, the service instance contains more components / features.
            Collection<Object> services = (Collection<Object>) service;
            for (Object component : services) {
                if (component instanceof Class) {
                    lResourceConfig.register((Class<?>) component);
                } else {
                    lResourceConfig.register(component);
                }
            }
        }
        // Adding the properties for the Jersey configuration that are prefixed.
        String[] referenceProperties = reference.getPropertyKeys();
        for (String referenceProperty : referenceProperties) {
            if (referenceProperty.startsWith(JerseyExtenderConstants.SERVICE_PROP_JERSEY_PROP_PREFIX)) {
                String jerseyProp = referenceProperty.substring(JerseyExtenderConstants.SERVICE_PROP_JERSEY_PROP_PREFIX
                        .length());
                Object propValue = reference.getProperty(referenceProperty);
                lResourceConfig.property(jerseyProp, propValue);
            } else if (JerseyExtenderConstants.SERVICE_PROP_JACKSON_SUPPORT.equals(referenceProperty)
                    && Boolean.valueOf(reference.getProperty(referenceProperty).toString())) {
                lResourceConfig.register(JacksonFeature.class);
            }
        }

        return lResourceConfig;
    }

    public Long getFactoryServiceId() {
        return factoryServiceId;
    }

    @Override
    public synchronized Servlet getService(final Bundle bundle, final ServiceRegistration<Servlet> registration) {
        ResourceConfig resourceConfig = createResourceConfig();
        ServletContainer servletContainer = servletContainer = new ServletContainer(resourceConfig);
        providedServlets.add(servletContainer);

        return servletContainer;
    }

    public boolean isCollection() {
        return collection;
    }

    /**
     * Refreshes the instantiated Jersey servlets. This is necessary when the properties of the embedded OSGi service
     * changes.
     */
    public synchronized void referesh() {
        extenderContext.ungetService(reference);
        ResourceConfig resourceConfig = createResourceConfig();
        for (ServletContainer providedServlet : providedServlets) {
            providedServlet.reload(resourceConfig);
        }

    }

    void setFactoryServiceId(final Long factoryServiceId) {
        this.factoryServiceId = factoryServiceId;
    }

    @Override
    public synchronized void ungetService(final Bundle bundle, final ServiceRegistration<Servlet> registration,
            final Servlet service) {

        providedServlets.remove(service);
        extenderContext.ungetService(reference);
    }
}
