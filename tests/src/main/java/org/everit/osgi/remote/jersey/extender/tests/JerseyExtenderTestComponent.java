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
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.remote.jersey.extender.JerseyExtenderConstants;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;

@Component(immediate = true, metatype = true)
@Service(value = JerseyExtenderTestComponent.class)
@Properties({
        @Property(name = "eosgi.testId", value = "test1"),
        @Property(name = "eosgi.testEngine", value = "junit4") })
@TestDuringDevelopment
public class JerseyExtenderTestComponent {

    @Reference(bind = "bindServer", unbind = "unbindServer")
    private Server server;

    /**
     * The test port where Jetty listens.
     */
    private int testPort = 0;

    /**
     * The service registration of the simlpe JAX-RS test component.
     */
    private ServiceRegistration<Object> helloWorldSR;

    /**
     * The context of the test bundle.
     */
    private BundleContext bundleContext;

    @Activate
    public void activate(final BundleContext bundleContext, final Map<String, Object> componentProperties) {
        Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
        serviceProperties.put("alias", "/helloworld");
        serviceProperties.put(JerseyExtenderConstants.SERVICE_PROP_JERSEY_COMPONENT, "true");

        helloWorldSR = bundleContext.registerService(Object.class, new JaxRSTestService(),
                serviceProperties);

        this.bundleContext = bundleContext;
    }

    /**
     * Bind method for the Jetty server. The test port is filled here.
     * 
     * @param server
     *            The Jetty server.
     */
    public void bindServer(final Server server) {
        Connector[] connectors = server.getConnectors();
        for (int i = 0, n = connectors.length; (i < n) && (testPort == 0); i++) {
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

    @Deactivate
    public void deActivate() {
        helloWorldSR.unregister();
    }

    /**
     * Testing the usage of {@link JerseyExtenderConstants#SERVICE_PROP_JACKSON_SUPPORT} service property.
     */
    @Test
    public void testJacksonServiceProperty() {
        Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
        serviceProperties.put("alias", "/helloworldTmp");
        serviceProperties.put(JerseyExtenderConstants.SERVICE_PROP_JERSEY_COMPONENT, true);
        serviceProperties.put(JerseyExtenderConstants.SERVICE_PROP_JACKSON_SUPPORT, true);

        ServiceRegistration<JaxRSTestService> helloWorldTmpSR = bundleContext.registerService(JaxRSTestService.class,
                new JaxRSTestService(),
                serviceProperties);

        try {
            WebClient webClient = new WebClient();
            webClient.setJavaScriptEnabled(false);
            Page page = webClient.getPage("http://localhost:" + testPort
                    + "/helloworldTmp/testService1/returnJSONFromDTO");
            String contentAsString = page.getWebResponse().getContentAsString();
            Assert.assertEquals("{\"name\":\"John\",\"age\":1}", contentAsString);
        } catch (IOException e) {
            throw new AssertionError("Unexpected error during test", e);
        } finally {
            helloWorldTmpSR.unregister();
        }
    }

    /**
     * Calling a function that writes to the response output stream directly.
     */
    @Test
    public void testJSON() {

        try {
            WebClient webClient = new WebClient();
            webClient.setJavaScriptEnabled(false);
            Page page = webClient.getPage("http://localhost:" + testPort + "/helloworld/testService1/returnJSON");
            String contentAsString = page.getWebResponse().getContentAsString();
            System.out.println("Return content" + contentAsString);
        } catch (IOException e) {
            throw new AssertionError("Unexpected error during test", e);
        }
    }

    /**
     * Registering a component with the {@link JacksonFeature} to test the generation of JSON objects.
     */
    @Test
    public void testJSONFromDTO() {
        Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
        serviceProperties.put("alias", "/helloworldTmp");
        serviceProperties.put(JerseyExtenderConstants.SERVICE_PROP_JERSEY_COMPONENT, "true");

        ServiceRegistration<Collection> helloWorldTmpSR = bundleContext.registerService(Collection.class,
                Arrays.asList(new Object[] { new JaxRSTestService(), JacksonFeature.class }),
                serviceProperties);

        try {
            WebClient webClient = new WebClient();
            webClient.setJavaScriptEnabled(false);
            Page page = webClient.getPage("http://localhost:" + testPort
                    + "/helloworldTmp/testService1/returnJSONFromDTO");
            String contentAsString = page.getWebResponse().getContentAsString();
            Assert.assertEquals("{\"name\":\"John\",\"age\":1}", contentAsString);
        } catch (IOException e) {
            throw new AssertionError("Unexpected error during test", e);
        } finally {
            helloWorldTmpSR.unregister();
        }
    }

    /**
     * Testing the case when jersey configuration is changed by changing the registered JAX-RS component OSGi service
     * properties without unregistering and registering it. To do that, the WADL existence is checked of the REST
     * service.
     */
    @Test
    public void testServicePropertyChange() {
        Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
        serviceProperties.put("alias", "/helloworldTmp");
        serviceProperties.put(JerseyExtenderConstants.SERVICE_PROP_JERSEY_COMPONENT, "true");

        ServiceRegistration<Collection> helloWorldTmpSR = bundleContext.registerService(Collection.class,
                Arrays.asList(new Object[] { new JaxRSTestService(), JacksonFeature.class }),
                serviceProperties);

        try {
            WebClient webClient = new WebClient();
            webClient.setJavaScriptEnabled(false);
            webClient.getPage("http://localhost:" + testPort + "/helloworldTmp/application.wadl");

            serviceProperties.put(JerseyExtenderConstants.SERVICE_PROP_JERSEY_PROP_PREFIX
                    + ServerProperties.WADL_FEATURE_DISABLE,
                    true);
            helloWorldTmpSR.setProperties(serviceProperties);

            try {
                webClient.getPage("http://localhost:" + testPort + "/helloworldTmp/application.wadl");
                Assert.fail("WADL exists although the " + ServerProperties.WADL_FEATURE_DISABLE
                        + " property is set to true: ");
            } catch (FailingHttpStatusCodeException e) {
                Assert.assertEquals(HttpStatus.NOT_FOUND_404, e.getStatusCode());
            }
        } catch (IOException e) {
            throw new AssertionError("Unexpected error during test", e);
        } finally {
            helloWorldTmpSR.unregister();
        }
    }

    /**
     * A simple method call that has only String parameter and return types.
     */
    @Test
    public void testSimple() {

        try {
            WebClient webClient = new WebClient();
            webClient.setJavaScriptEnabled(false);
            Page page = webClient.getPage("http://localhost:" + testPort + "/helloworld/testService1/hello?name=John");
            String contentAsString = page.getWebResponse().getContentAsString();
            Assert.assertEquals("Hello John!", contentAsString);
        } catch (IOException e) {
            throw new AssertionError("Unexpected error during test", e);
        }
    }

    @Test
    public void testWebConsolePlugin() {
        try {
            WebClient webClient = new WebClient();
            DefaultCredentialsProvider credentialsProvider = new DefaultCredentialsProvider();
            credentialsProvider.addCredentials("admin", "admin");
            webClient.setCredentialsProvider(credentialsProvider);
            webClient.setJavaScriptEnabled(false);
            Page page = webClient
                    .getPage("http://localhost:" + testPort + "/system/console/jerseyextender");
            String contentAsString = page.getWebResponse().getContentAsString();
            int indexOfTestService = contentAsString.indexOf(JaxRSTestService.class.getName());
            Assert.assertEquals(true, indexOfTestService > 0);
        } catch (IOException e) {
            throw new AssertionError("Unexpected error during test", e);
        }
    }

    /**
     * Unbinding the Jetty server and setting the test port to0 ;
     * 
     * @param server
     *            The Jetty server.
     */
    public void unbindServer(final Server server) {
        testPort = 0;
    }
}
