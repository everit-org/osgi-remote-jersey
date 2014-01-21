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
import java.io.OutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("testService1")
public class TestJaxRSService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("returnJSON")
    public Response getJSONAsString() {
        return Response.ok(new StreamingOutput() {

            @Override
            public void write(final OutputStream output) throws IOException, WebApplicationException {
                output.write("HELLO WOOORLD!!!".getBytes());
            }
        }).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("returnJSONFromDTO")
    public SimpleDTO getJSONFromDTO() {
        return new SimpleDTO("John", 1);
    }

    @GET
    @Produces("text/plain")
    @Path("hello")
    public String hello(@QueryParam("name") final String name) {
        return "Hello " + name + "!";
    }
}
