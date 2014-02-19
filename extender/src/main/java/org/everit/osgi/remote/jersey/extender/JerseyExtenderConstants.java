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
package org.everit.osgi.remote.jersey.extender;
/**
 * Constants of the extender.
 */
public final class JerseyExtenderConstants {

    /**
     * When a JAX-RS annotated class should be extended by Jersey, it should registered as an OSGi service that has a
     * service property with the key defined by this constant and the value 'true'.
     */
    public static final String SERVICE_PROP_JERSEY_COMPONENT = "org.everit.osgi.remote.jersey.component";

    /**
     * The id of OSGi service that is annotated with JAX-RS and re-registered as a servlet by this extender.
     */
    public static final String SERVICE_PROP_TARGET_SERVICE_ID = "target." + org.osgi.framework.Constants.SERVICE_ID;

    /**
     * The persistent id of OSGi service (if available) that is annotated with JAX-RS and re-registered as a servlet by
     * this extender.
     */
    public static final String SERVICE_PROP_TARGET_SERVICE_PID = "target." + org.osgi.framework.Constants.SERVICE_PID;

    /**
     * All service properties with the specified prefix will be set for the Jersey servlet.
     */
    public static final String SERVICE_PROP_JERSEY_PROP_PREFIX = "org.everit.osgi.remote.jersey.prop.";

    /**
     * In case a JAX-RS service is registered with this service property, the Jackson Feature will be automatically
     * added to the Jersey ServletContainer. The value of the property must be true.
     */
    public static final String SERVICE_PROP_JACKSON_SUPPORT = "org.everit.osgi.remote.jackson";

    private JerseyExtenderConstants() {
    }
}
