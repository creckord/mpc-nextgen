# Marketplace Client 2.0

## REST Interface

The central part of the Eclipse Marketplace Client (MPC) is its interface to the Marketplace server. Historically, this interface was not explicitly defined, but implemented on both server- and client-side separately and manually. Keeping the two implementations synchronized is a fully manual process as well.

The next generation of this REST interface aims to make the specification a first-class citizen, using OpenAPI to define and document REST endpoints and data models.

This document explores how best to leverage this REST API spec to define the Marketplace Client API and implementation and redefine the technological foundation of the MPC in a clean and lightweight fashion.

### Goal

All REST endpoints consumed by MPC are automatically derived from the server's OpenAPI spec and JSON schemas. No communication layer code is hand-written. The MPC REST layer can be consumed by clients without (too much) consideration of technical details.

### Technical considerations

#### Use of Frameworks
As far as possible, all communication layer implementation - HTTP communication as well as marshalling/unmarshalling of data - should be left to libraries and automatically generated interfaces. This way, we can focus on MPC business logic and value, and delegate correctness and safety considerations of the communication to those frameworks.

Frameworks to consider
- Resteasy for OpenAPI REST interfaces
- Jackson or GSON for JSON de-/serialization
- Datamodel libraries like Immutables.org or Lombok

#### Versioning

Over time, changes to the OpenAPI spec might require breaking changes to the client implementation of the REST API. In fact, since one goal of the OpenAPI redesign of the REST interface is to achieve more agility. This can be challenging to reconcile with a client API that is expected to be stable, especially in the context of OSGi semantic versioning. It is generally not desirable to require a new major version for the client API.

The OpenAPI spec will be versioned, so one solution would be to have separate client-side implementations per spec version. In this model, a new spec version would just expose new API in a new minor version update of the client API. Implementing interfaces of the old version against the new one or vise versa would give us flexible ways of maintaining backwards compatibility with 
- new clients talking to old servers
- old clients talking to new servers

#### Speed

A major issue with the current MPC REST implementation is that it feels rather sluggish when used interactively, e.g. through the MPC UI. Initial startup requires several client-server roundtrips for performing marketplace server discovery, loading the list of catalogs and getting initial content. There are two things that can be done about this:

##### Caching

Current versions of MPC keep their own high-level caches of MPC data, which do not refresh changed content once filled, but forget their data when restarting the IDE. This gives some performance gain, but a) is complicated to maintain and b) results in outdated entry data. Http caching information wasn't used in the past, because previous versions of the server API would not provide reliable metadata for caching: Last-Modified-Since would be updated regularly without actual changes, and ETags were not provided.

With the new server API, we will get reliable ETag data, which we can use to implement usage of Http-level caching. This can be done through configuration of the httpclient library, reducing complexity in our implementation and leaning on the well-maintained httpclient code base. Caches can even be kept between Eclipse restarts and shared between multiple workbenches.

##### Prefetching

Another way to improve user experience and overall response time is strategic prefetching of required data. With proper caching provided by the REST implementation, this can be exposed to clients like the MPC UI by simply having API for the corresponding endpoints, and possibly a helper encapsulating endpoint calls for typical prefetching scenarios. 

Clients will then be able to call these endpoints at a time of their choosing (e.g. in the background after workbench startup), or even in regular intervals (e.g. to refresh the default content list).

#### Data Model

In order to make data handling easier, the data model should be kept immutable. This makes it easier and safer to hand out data to clients without explicitly maintaining guards like protective copying in the MPC REST implementation. It also makes it easier to cache data and reason about changes, which become much more explicit.

This could be done by using aforementioned Immutables.org or Lombok frameworks in the generated data models and leverage builder patterns for instance construction.

## Eclipse Rich Client UI

TODO

# Tasks

- [ ] xxx
