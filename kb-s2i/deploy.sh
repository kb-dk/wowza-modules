#!/usr/bin/env bash

cp -- /tmp/src/conf/ocp/VHosts.xml "$CONF_DIR/VHosts.xml"

sed -i 's|<KeyStorePath>${user.home}/services/keystore.jks</KeyStorePath>|<KeyStorePath>/app/conf/keystore.jks</KeyStorePath>|' /app/conf/wowza-mediestream-vhost/conf/VHost.xml

cp /app/conf/wowza-mediestream-vhost/conf/mediestream/wowza-module.properties /app/conf/mediestream-wowza-module.properties
ln -sf /app/conf/mediestream-wowza-module.properties  /app/conf/wowza-mediestream-vhost/conf/mediestream/wowza-module.properties
ln -sf /app/conf/mediestream-wowza-module.properties  /app/conf/wowza-mediestream-vhost/conf/mediestreamapple/wowza-module.properties
chmod -R g+w /app/conf

mkdir -p /app/content/mediestream/{doms,kuana,kuanaradio}
ln -s /app/content/mediestream/doms /app/conf/streamingContent/doms
ln -s /app/content/mediestream/kuana /app/conf/streamingContent/kuana
ln -s /app/content/mediestream/kuanaradio /app/conf/streamingContent/kuanaradio
