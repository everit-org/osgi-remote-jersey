osgi-remote-jersey
==================

The Jersey extender picks up OSGi services and registers them as Jersey
based JAX-RS components.


## Supported service properties

Currently the extender supports two service properties:

_org.everit.osgi.remote.jersey.component_: When this property has the value
true, the OSGi service will be picked up by the extender and registered as
a Jersey component.

_org.everit.osgi.remote.jackson_: When this property is set to true, the
extender registers the Jackson feature to the Jersey Servlet.


## Registering more than one component to a servlet

In case a registered OSGi service has the type _java.util.Collection_, each
member of the collection will be handled as a Jersey component. If the
collection contains four objects, the newly created Jersey servlet will
contain four components.


## What is a component?

A component can be

- a class / object with JAX-RS annotations
- a class / object that implements an interface that has JAX-RS annotations 
- a class / object that implements _javax.ws.rs.core.Feature_
- any class / object that can be registered in _ResourceConfig_ of Jersey

In case a special feature should be used during processing the registered
components, the user should register a Collection that holds the original
components and the type of the Feature(s) as well.


## How can I configure Jersey?

All service property that begins with _org.everit.osgi.remote.jersey.prop._
will be passed to the ResourceConfig of Jersey.

E.g.:

If a service property with the following key is defined:

_org.everit.osgi.remote.jersey.prop.jersey.config.server.wadl.disableWadl_

That will cause to pass the _jersey.config.server.wadl.disableWadl_ property
to Jersey with the same value as the service property had.


## How will be the Jersey servlet registered into a Servlet container

The extender itself simply creates a Servlet OSGi service that contains the
Jersey components. It is up to the use to register the newly created OSGi
service in any of the containers.

However, the newly created service will contain all service properties that
the original OSGi service had. The user has the chance to define properties
in the original OSGi service that causes the Servlet to be registered. The
easiest way is to use the felix-whiteboard module or if there is an
implementation, use the OSGi HTTP Whiteboard Service properties.


## Is it possible to get information about the tracked services?

The extender has a webconsole plugin. On the webconsole there should be a
"Jersey Extender" menu item. Within the page there is a table that shows
what services were picked up and which Servlet services were created.


## Where are the examples?

Examples are avaialable in the Integration test project. The "tests" project
is available in the source repository.


## Maven repo?

See the pom.xml in the source repository or the pom file that is embedded
into the distributed jar.