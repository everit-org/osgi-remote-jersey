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

public final class Constants {

    public static final String SERVICE_PROP_JERSEY_COMPONENT = "org.everit.osgi.remote.jersey.component";
    
    public static final String SERVICE_PROP_JERSEY_EXTENDER = "org.everit.osgi.remote.jersey.extender";

    public static final String SERVICE_PROP_TARGET_SERVICE_ID = "target." + org.osgi.framework.Constants.SERVICE_ID;

    public static final String SERVICE_PROP_TARGET_SERVICE_PID = "target." + org.osgi.framework.Constants.SERVICE_PID;

    private Constants() {
    }
}
