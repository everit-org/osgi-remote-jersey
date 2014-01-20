package org.everit.osgi.remote.jersey.extender.tests;

public class SimpleDTO {

    private final String name;

    private final int age;

    public SimpleDTO(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
