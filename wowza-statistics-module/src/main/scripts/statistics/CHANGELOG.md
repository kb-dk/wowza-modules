20190329:
LARM:
Added support for calls to solr/pvica for empty returns from doms
Uses requests-module for rest calls to pvica and solr

20181004:
Mediestream
Added support for calls to solr/pvica for empty returns from doms
Uses requests-module for rest calls to pvica and solr

20161018:
LARM:

* Fix connection exhaustion on failed http requests

20160830:
LARM:

* Fix horrible, horrible sql injection bug

20160826:
LARM:

* Better post processing of LARM statistics - don't repeat streams and better channel handling. Ignore streams of only a few seconds.

20151026:
LARM:

* Handle case where file isn't a doms ID but also doesn't have an embedded channel in the name.
Mediestream:
* Handle case where sent attributes are 'None'

20151008:
Only changes for LARM statistics.

* Support for wayf attributes. Requires the new logging module (3.7) with the updates to the database described there.
* Do not depend on shards for LARM logging
