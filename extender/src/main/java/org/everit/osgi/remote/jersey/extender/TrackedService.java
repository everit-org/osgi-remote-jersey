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
 * Information about the services that are tracked.
 */
public class TrackedService {

    /**
     * Whether this tracked service holds one or a collection of components.
     */
    private final boolean collection;

    /**
     * The id of the tracked service.
     */
    private final Long trackedServiceId;

    /**
     * The id of the Servlet OSGi service that was registered by this extender.
     */
    private final Long servletServiceId;

    /**
     * Information about the tracked service. This field contains a string representation of the service object.
     */
    private final String info;

    /**
     * Simple constructor.
     * 
     * @param embeddedServiceId
     *            The id of the service that is embedded.
     * @param collection
     *            Whether this tracked service holds one or a collection of components.
     * @param info
     *            Information about the tracked service.
     */
    public TrackedService(final Long embeddedServiceId, final Long servletServiceId, final boolean collection,
            final String info) {
        trackedServiceId = embeddedServiceId;
        this.collection = collection;
        this.info = info;
        this.servletServiceId = servletServiceId;
    }

    public String getInfo() {
        return info;
    }

    public Long getServletServiceId() {
        return servletServiceId;
    }

    public Long getTrackedServiceId() {
        return trackedServiceId;
    }

    public boolean isCollection() {
        return collection;
    }
}
