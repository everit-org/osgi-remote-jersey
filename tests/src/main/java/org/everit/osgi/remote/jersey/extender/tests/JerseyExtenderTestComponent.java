package org.everit.osgi.remote.jersey.extender.tests;

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

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.remote.jersey.extender.Constants;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

@Component(immediate = true)
@Service(value = JerseyExtenderTestComponent.class)
@Properties({
        @Property(name = "eosgi.testId", value = "test1"),
        @Property(name = "eosgi.testEngine", value = "junit4") })
public class JerseyExtenderTestComponent {

    @Reference(bind = "bindServer", unbind = "unbindServer")
    private Server server;

    private int testPort = 0;
    
    private ServiceRegistration<Object> helloWorldSR;

    public void bindServer(Server server) {
        Connector[] connectors = server.getConnectors();
        for (int i = 0, n = connectors.length; i < n && testPort == 0; i++) {
            if (connectors[i] instanceof NetworkConnector) {
                NetworkConnector networkConnector = (NetworkConnector) connectors[i];
                List<String> protocols = networkConnector.getProtocols();
                boolean httpAvailable = false;
                Iterator<String> protocolIterator = protocols.iterator();
                while (!httpAvailable && protocolIterator.hasNext()) {
                    String protocol = protocolIterator.next();
                    if (protocol.toLowerCase().startsWith("http")) {
                        httpAvailable = true;
                    }
                }
                if (httpAvailable) {
                    int localPort = networkConnector.getLocalPort();
                    if (localPort > 0) {
                        testPort = localPort;
                    }
                }
            }
        }
    }

    public void unbindServer(Server server) {
        testPort = 0;
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        
        Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
        serviceProperties.put("alias", "/helloworld");
        serviceProperties.put(Constants.SERVICE_PROP_JERSEY_COMPONENT, "true");

        helloWorldSR = bundleContext.registerService(Object.class, new TestJaxRSService(),
                serviceProperties);
    }

    @Test
    @TestDuringDevelopment
    public void testSimple() {

        try {
            WebClient webClient = new WebClient();
            Page page = webClient.getPage("http://localhost:" + testPort + "/helloworld/testService1/hello?name=John");
            String contentAsString = page.getWebResponse().getContentAsString();
            Assert.assertEquals("Hello John!", contentAsString);
        } catch (IOException e) {
            throw new AssertionError("Unexpected error during test", e);
        }
    }

    @Deactivate
    public void deActivate() {
        helloWorldSR.unregister();
    }
}
