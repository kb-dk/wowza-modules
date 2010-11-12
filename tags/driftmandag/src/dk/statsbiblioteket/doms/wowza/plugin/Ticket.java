package dk.statsbiblioteket.doms.wowza.plugin;

import javax.xml.bind.annotation.*;
import java.util.Date;

/* $Id$
 * $Revision$
 * $Date$
 * $Author$
 *
 * The DOMS project.
 * Copyright (C) 2007-2009  The State and University Library
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ticket", propOrder = {
        "ID",
        "url",
        "username"
})
@XmlRootElement
public class Ticket {
    private String ID;

    private String url;

    private String username;

    @XmlTransient
    private long creationTime = new Date().getTime();

    public Ticket() {
    }

    public Ticket(String ID, String url, String username) {
        this.ID = ID;
        this.url = url;
        this.username = username;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public String getID() {
        return ID;
    }
}
