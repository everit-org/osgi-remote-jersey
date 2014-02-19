/**
 * This file is part of Everit - Jersey Extender Tests.
 *
 * Everit - Jersey Extender Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Jersey Extender Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Jersey Extender Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.remote.jersey.extender.tests;

import java.net.URI;
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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.BasicAuthentication;
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
            String contentAsString = callGet("http://localhost:" + testPort
                    + "/helloworldTmp/testService1/returnJSONFromDTO").getContentAsString();

            Assert.assertEquals("{\"name\":\"John\",\"age\":1}", contentAsString);
        } finally {
            helloWorldTmpSR.unregister();
        }
    }

    private ContentResponse callGet(String url) {
        ContentResponse result = null;
        HttpClient httpClient = new HttpClient();
        try {
            httpClient.start();
            ContentResponse contentResponse = httpClient.GET(url);
            result = contentResponse;
        } catch (Exception e) {
            Assert.fail("Http GET '" + url + "' failed with exeption " + e.getClass().getName() + ": "
                    + e.getMessage());
        } finally {
            try {
                httpClient.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * Calling a function that writes to the response output stream directly.
     */
    @Test
    public void testJSON() {
        String contentAsString = callGet("http://localhost:" + testPort + "/helloworld/testService1/returnJSON")
                .getContentAsString();
        System.out.println("Return content" + contentAsString);
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
            String contentAsString = callGet("http://localhost:" + testPort
                    + "/helloworldTmp/testService1/returnJSONFromDTO").getContentAsString();
            Assert.assertEquals("{\"name\":\"John\",\"age\":1}", contentAsString);
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
            callGet("http://localhost:" + testPort + "/helloworldTmp/application.wadl").getContentAsString();

            serviceProperties.put(JerseyExtenderConstants.SERVICE_PROP_JERSEY_PROP_PREFIX
                    + ServerProperties.WADL_FEATURE_DISABLE,
                    true);
            helloWorldTmpSR.setProperties(serviceProperties);

            ContentResponse response = callGet("http://localhost:" + testPort + "/helloworldTmp/application.wadl");
            if (response.getStatus() == 200) {
                Assert.fail("WADL exists although the " + ServerProperties.WADL_FEATURE_DISABLE
                        + " property is set to true: ");
            }
        } finally {
            helloWorldTmpSR.unregister();
        }
    }

    /**
     * A simple method call that has only String parameter and return types.
     */
    @Test
    public void testSimple() {
        String contentAsString = callGet("http://localhost:" + testPort + "/helloworld/testService1/hello?name=John")
                .getContentAsString();
        Assert.assertEquals("Hello John!", contentAsString);
    }

    @Test
    public void testWebConsolePlugin() {

        HttpClient httpClient = new HttpClient();

        try {
            httpClient.start();
            AuthenticationStore auth = httpClient.getAuthenticationStore();
            URI uri = URI.create("http://localhost:" + testPort + "/system/console/jerseyextender");
            auth.addAuthentication(new BasicAuthentication(uri, "OSGi Management Console", "admin", "admin"));
            ContentResponse contentResponse = httpClient.GET(uri);

            if (contentResponse.getStatus() != 200) {
                Assert.fail(uri.toString() + " answered with status " + contentResponse.getStatus());
            }
            String contentAsString = contentResponse.getContentAsString();
            int indexOfTestService = contentAsString.indexOf(JaxRSTestService.class.getName());
            Assert.assertEquals(true, indexOfTestService > 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                httpClient.stop();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
