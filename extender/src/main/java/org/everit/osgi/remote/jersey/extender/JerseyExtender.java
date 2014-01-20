package org.everit.osgi.remote.jersey.extender;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.Servlet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * An {@link ServiceTrackerCustomizer} that should track the services where the {@link Constants#SERVICE_PROP_JERSEY_COMPONENT}
 * property is defined.
 */
public class JerseyExtender implements ServiceTrackerCustomizer<Object, ServiceRegistration<Servlet>> {

    @Override
    public ServiceRegistration<Servlet> addingService(ServiceReference<Object> reference) {

        Bundle bundleOfReference = reference.getBundle();
        BundleContext contextOfReference = bundleOfReference.getBundleContext();
        Dictionary<String, ?> properties = createProperties(reference);

        JerseyServletServiceFactory serviceFactory = new JerseyServletServiceFactory(reference);
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
                    && !Constants.SERVICE_PROP_JERSEY_COMPONENT.equals(propertyKey)
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
