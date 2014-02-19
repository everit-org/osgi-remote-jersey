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

import java.util.Hashtable;

import javax.servlet.Servlet;

import org.everit.osgi.remote.jersey.extender.JerseyExtender;
import org.everit.osgi.remote.jersey.extender.JerseyExtenderConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Main Activator of the extender solution.
 */
public class Activator implements BundleActivator {

    /**
     * The service tracker that tracks every OSGi service based on the
     * {@link JerseyExtenderConstants#SERVICE_PROP_JERSEY_COMPONENT} service property.
     */
    private ServiceTracker<Object, ServiceRegistration<Servlet>> tracker;

    private ServiceRegistration<JerseyExtender> jerseyExtenderSR;

    private ServiceRegistration<Servlet> webConsolePluginSR;

    @Override
    public void start(final BundleContext context) throws Exception {
        JerseyExtenderImpl jerseyExtender = new JerseyExtenderImpl(context);
        Filter filter = FrameworkUtil.createFilter("(" + JerseyExtenderConstants.SERVICE_PROP_JERSEY_COMPONENT
                + "=true)");
        tracker = new ServiceTracker<Object, ServiceRegistration<Servlet>>(context, filter, jerseyExtender);
        tracker.open();
        jerseyExtenderSR = context.registerService(JerseyExtender.class, jerseyExtender,
                new Hashtable<String, Object>());
        JerseyExtenderWebConsolePlugin webConsolePlugin = new JerseyExtenderWebConsolePlugin(jerseyExtender);
        Hashtable<String, Object> webConsoleProps = new Hashtable<String, Object>();
        webConsoleProps.put("felix.webconsole.title", webConsolePlugin.getTitle());
        webConsoleProps.put("felix.webconsole.label", webConsolePlugin.getLabel());
        webConsolePluginSR = context.registerService(Servlet.class, webConsolePlugin, webConsoleProps);

    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        webConsolePluginSR.unregister();
        jerseyExtenderSR.unregister();
        tracker.close();
    }

}
