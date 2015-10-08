# RESTHeart #

The data REST API server for MongoDB.

__Version__: 1.0.x

> Open your data, quickly build HATEOAS applications, use it as your mobile apps back-end,...

### >>> Read the full documentation at [www.restheart.org](http://www.restheart.org) <<<

[![Build Status](https://travis-ci.org/SoftInstigate/restheart.svg?branch=develop)](https://travis-ci.org/SoftInstigate/restheart)

## RESTHeart, the REST API server for [MongoDB](http://www.mongodb.org/).

* Zero development time: just start it and the data REST API is ready to use
* CRUD operations API on your data
* Data model operations API: create dbs, collections, indexes and the data structure
* Super easy setup with convention over configuration approach
* Pluggable security with User Management and ACL
* [HAL](http://stateless.co/hal_specification.html) hypermedia type
* Super lightweight: pipeline architecture, ~7Mb footprint, ~200Mb RAM peek usage, starts in milliseconds,..
* High throughput: very small overhead on MongoDB performance
* Horizontally scalable: fully stateless architecture supporting MongoDB replica sets and shards
* Built on top of [Undertow](http://undertow.io) non-blocking web server
* Embeds the excellent [HAL browser](https://github.com/mikekelly/hal-browser) by Mike Kelly (the author of the HAL specifications)
* Support Cross-origin resource sharing ([CORS](http://en.wikipedia.org/wiki/Cross-origin_resource_sharing)) so that your one page web application can deal with RESTHeart running on a different domain. In other words, CORS is an evolution of JSONP
* Ideal as a [AngularJS](https://angularjs.org) (or any other MVW javascript framework) back-end

## How to run it

### Vagrant

If you don’t want to install and run all the components manually on your host, there is a handy [Vagrant box](https://github.com/SoftInstigate/restheart-vagrant) available for creating a complete virtual development environment, using a Ubuntu 14.04 image with JDK 8, MongoDB 3 and the latest RESTHeart server. You can then skip section 2 to 6 and jump directly to section 7, in case you want to know how to change the default security settings.

### Docker

Docker is even simpler and ours favorite. There is a [Docker container](https://hub.docker.com/r/softinstigate/restheart/) available, with a JVM running RESTHeart linked to another container running MongoDB.

### Bare metal

Please follow the next sections for a full local installation.

> RESTHeart requires [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html), make sure you have it and available on your path. It builds with [Maven](http://www.oracle.com/technetwork/java/javase/downloads/index.html).

Download the latest release from [github releases page](https://github.com/SoftInstigate/restheart/releases/latest), unpack the archive and just run the jar.

	$ java -server -jar restheart.jar

You might also want to specify a configuration file:

	$ java -server -jar restheart.jar etc/restheart.yml

* configuration file [documentation](http://restheart.org/docs/configuration.html)
* example configuration file [restheart.yml](http://restheart.org/docs/configuration.html#conf-example)

## How to build it

Clone the repository and update the git submodules (the __HAL browser__ is included in restheart as a submodule):

    $ git submodule update --init --recursive

Build the project with Maven:

    $ mvn clean package

## Integration tests

Optionally you can run the integration test suite. Make sure __mongod is running__ on localhost on default port 27017 without authentication, i.e. no `--auth` option is specified.

    $ mvn verify -DskipITs=false
