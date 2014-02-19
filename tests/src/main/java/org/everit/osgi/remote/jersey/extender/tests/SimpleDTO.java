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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple DTO class for testing purposes.
 */
@XmlRootElement
public class SimpleDTO {

    private String name;

    private int age;

    public SimpleDTO() {
    }

    public SimpleDTO(final String name, final int age) {
        this.name = name;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public void setAge(final int age) {
        this.age = age;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
