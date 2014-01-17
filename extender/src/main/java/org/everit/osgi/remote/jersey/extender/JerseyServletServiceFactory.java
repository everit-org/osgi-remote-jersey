package org.everit.osgi.remote.jersey.extender;

import java.util.Collection;

import javax.servlet.Servlet;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
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

    private final BundleContext bundleContext;

    private final ServiceReference<Object> reference;

    private final boolean collection;

    public JerseyServletServiceFactory(BundleContext bundleContext, ServiceReference<Object> reference) {
        this.bundleContext = bundleContext;
        this.reference = reference;
        this.collection = isCollection(reference);
    }

    @Override
    public Servlet getService(Bundle bundle, ServiceRegistration<Servlet> registration) {
        ResourceConfig resourceConfig = new ResourceConfig();
        Object service = bundleContext.getService(reference);
        if (!collection) {
            resourceConfig.getSingletons().add(service);
        } else {
            resourceConfig.getSingletons().addAll((Collection<?>) service);
        }
        ServletContainer servletContainer = new ServletContainer(resourceConfig);
        return servletContainer;
    }

    @Override
    public void ungetService(Bundle bundle, ServiceRegistration<Servlet> registration, Servlet service) {
        bundleContext.ungetService(reference);
    }

}
