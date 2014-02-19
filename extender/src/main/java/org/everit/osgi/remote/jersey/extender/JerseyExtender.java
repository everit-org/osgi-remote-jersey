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
import java.util.List;

/**
 * Information about the current state of the extender can be queried via the OSGi service registered with this
 * interface.
 */
public interface JerseyExtender {

    /**
     * Retrieving an unmodifiable list of the services tracked by this extender. Tracking means that they are
     * re-registered as Jersey servlets.
     * 
     * @return The list of tracked services.
     */
    List<TrackedService> getTrackedServices();
}
