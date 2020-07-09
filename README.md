# wowza-modules
These are modules for Wowza 4.8.5 used at the Royal Danish Library.

It consists of 8 modules and three example VHosts

## The modules 
wowza-content-resolver-module
  Given configuration, lookup streams in diferent directory structure

wowza-ticket-checker-module
  Read a ticket ID from query param, and check that it is a ticket issued for the given stream and ip of client

wowza-statistics-module
  Log events from wowza to a simple text file. Also logs information from ticket.

wowza-mcm-authorization-module
  Given a query parameter, check that it as a valid MCM session. This module is deprecated. Use MCM3 module instead.
  Some classes are used by wowza-mcm3-authorization-module

wowza-mcm3-authorization-module
  Given a query parameter, check that it as a valid MCM3 session

wowza-chaosv6-authorization-module
  Given a query parameter, check that it as a valid chaosv6 session

wowza-mcm-statistics-module
  Log events from wowza to MCM. This module is deprecated, statistics logging is not supported in MCM3. Some classes
  are used by wowza-db-statistics-module.

wowza-db-statistics-module
  Log events from wowza to a database, including MCM session information.


## The VHosts 
### wowza-mediestream-vhost

A complete VHost that contains configuration and the modules
 * wowza-content-resolver-module
 * wowza-ticket-checker-module
 * wowza-statistics-module
Example configuration can be found in conf/mediestream/wowza-modules.properties
The streamingContent directory in the VHost should point to the content to be served.
conf/Vhost.xml should probably be updated with correct port numbers

### wowza-chaos-vhost

Four complete VHosts that contain configuration and the modules
 * wowza-content-resolver-module (only in two VHost)
 * wowza-chaosv6-authorization-module
 * wowza-db-statistics-module
Example configuration can be found in conf/chaos/wowza-modules.properties
The streamingContent directory in the VHost should point to the content to be served.
conf/Vhost.xml should probably be updated with correct port numbers

## Requirements
The project requires Java 11 be build and run. Known to build with OpenJDK 11, other JDKs may work

The project depends on java libraies from WowzaStreamingEngine, and as a commercial product can't be included. To obtain the needed libraies an installation of WowzaStreamingEngine is required. 
WowzaStreamingEngine installation program and developer license can be obtained from [Wowza](http://www.wowza.com/streaming/developers).
The installer itself offers arguments to specify non-standard installation directory but does not respect the value, and additionally requires super-user rights to install itself in the default location of `/usr/local/WowzaStreamingEngine-${wms.version}`.
Part of the maven project lifecycle (`verify` phase) the needed libraries will be installed from `/usr/local/WowzaStreamingEngine-${wms.version}/lib` into the local maven repository/cache. 

## Building
Use maven to build the project i.e. `mvn clean package`

## Test 
There are unit tests that are run during the default build (see above). 

Operative tests of the product requires an installation, available streaming content and additional infrastructure (content-resolver, ticket system). Internally at The Royal Danish Library we have a setup for that currently running on the server `iapetus`.
