package org.everit.osgi.remote.jersey.extender.internal;

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
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.everit.osgi.remote.jersey.extender.JerseyExtender;
import org.everit.osgi.remote.jersey.extender.TrackedService;

public class JerseyExtenderWebConsolePlugin extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final JerseyExtender jerseyExtender;

    public JerseyExtenderWebConsolePlugin(final JerseyExtender jerseyExtender) {
        this.jerseyExtender = jerseyExtender;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        PrintWriter pw = resp.getWriter();

        pw.println("<table class='content' width='100%' cellspacing='0' cellpadding='0'>");

        pw.println("<tr>");
        pw.println("<th class='content container' colspan='3'>Tracked JAX-RS Services</td>");
        pw.println("</tr>");
        pw.println("<tr>");
        pw.println("<th class='content'>Tracked Service ID</td>");
        pw.println("<th class='content'>Registered Servlet Service ID</td>");
        pw.println("<th class='content'>Information (String representation of tracked service object)</td>");
        pw.println("</tr>");

        List<TrackedService> trackedServices = jerseyExtender.getTrackedServices();
        for (TrackedService trackedService : trackedServices) {
            Long trackedServiceId = trackedService.getTrackedServiceId();
            Long servletServiceId = trackedService.getServletServiceId();
            String info = trackedService.getInfo();
            pw.println("<tr>");
            pw.println("<td class='content'><a href='services/" + trackedServiceId + "'>" + trackedServiceId
                    + "</a></td>");
            pw.println("<td class='content'><a href='services/" + servletServiceId + "'>" + servletServiceId
                    + "</a></td>");
            pw.println("<td class='content'>" + info + "</td>");
            pw.println("</tr>");
        }
        pw.println("</table>");
    }

    public String getLabel()
    {
        return "jerseyextender";
    }

    public String getTitle()
    {
        return "Jersey Extender";
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException
    {
        // only handle GET requests, ensure no error message for other requests
        if ("GET".equals(req.getMethod()) || "HEAD".equals(req.getMethod()))
        {
            super.service(req, resp);
        }
    }
}
