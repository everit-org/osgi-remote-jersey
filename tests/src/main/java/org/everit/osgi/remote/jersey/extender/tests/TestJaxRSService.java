package org.everit.osgi.remote.jersey.extender.tests;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("testService1")
public class TestJaxRSService {

    @GET
    @Produces("text/plain")
    @Path("hello")
    public String hello(@QueryParam("name") String name) {
        return "Hello " + name + "!";
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("returnJSON")
    public SimpleDTO getJSON() {
        return new SimpleDTO("John", 1);
    }
}
