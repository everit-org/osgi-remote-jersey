package org.everit.osgi.remote.jersey.extender.tests;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@Component(immediate = true)
@Service(value = JerseyExtenderTestComponent.class)
@Properties({
        @Property(name = "eosgi.testId", value = "test1"),
        @Property(name = "eosgi.testEngine", value = "junit4") })
public class JerseyExtenderTestComponent {

    @Reference(bind = "bindServer", unbind = "unbindServer")
    private Server server;

    private int testPort = 0;

    private BundleContext bundleContext;

    private ServiceRegistration<Servlet> helloWorldSR;

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
        this.bundleContext = bundleContext;
    }

    @Test
    @TestDuringDevelopment
    public void testSimple() {
        Hashtable<String, Object> serviceProperties = new Hashtable<String, Object>();
        serviceProperties.put("alias", "/hello");
        helloWorldSR = bundleContext.registerService(Servlet.class, new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.getWriter().write("Hello World!");
            }
        }, serviceProperties);
    }

    @Deactivate
    public void deActivate() {
        if (helloWorldSR != null) {
            helloWorldSR.unregister();
        }
    }
}
