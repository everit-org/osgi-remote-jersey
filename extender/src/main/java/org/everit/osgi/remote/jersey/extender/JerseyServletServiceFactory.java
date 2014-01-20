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

import java.util.Collection;

import javax.servlet.Servlet;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

public class JerseyServletServiceFactory implements ServiceFactory<Servlet> {

    private static boolean isCollection(ServiceReference<Object> reference) {
        String[] objectClass = (String[]) reference.getProperty(org.osgi.framework.Constants.OBJECTCLASS);
        boolean collection = false;
        String collectionClassName = Collection.class.getName();
        for (int i = 0, n = objectClass.length; i < n && !collection; i++) {
            collection = collectionClassName.equals(objectClass[i]);
        }
        return collection;
    }

    private final ServiceReference<Object> reference;

    private final boolean collection;

    public JerseyServletServiceFactory(ServiceReference<Object> reference) {
        this.reference = reference;
        this.collection = isCollection(reference);
    }

    @Override
    public Servlet getService(Bundle bundle, ServiceRegistration<Servlet> registration) {
        ResourceConfig resourceConfig = new ResourceConfig();
        Object service = bundle.getBundleContext().getService(reference);
        if (!collection) {
            resourceConfig.register(service);
        } else {
            Collection<Object> services = (Collection<Object>) service;
            resourceConfig.registerInstances(services.toArray(new Object[services.size()]));
        }
        ServletContainer servletContainer = new ServletContainer(resourceConfig);
        return servletContainer;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration<Servlet> registration, Servlet service) {
        bundle.getBundleContext().ungetService(reference);
    }

}
