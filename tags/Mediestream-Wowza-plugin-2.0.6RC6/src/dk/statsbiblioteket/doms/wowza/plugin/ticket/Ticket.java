package dk.statsbiblioteket.doms.wowza.plugin.ticket;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.List;

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
        "resource",
        "username",
        "property"
})
@XmlRootElement
public class Ticket {
    private String ID;

    private String resource;

    private String username;
    
    private List<TicketProperty> property;

    public Ticket() {
    }

    public Ticket(String ID, String resource, String username, List<TicketProperty> property) {
        this.ID = ID;
        this.resource = resource;
        this.username = username;
        this.property = property;
    }

    public List<TicketProperty> getProperty() {
		return property;
	}

	public String getResource() {
        return resource;
    }

    public String getUsername() {
        return username;
    }

    public String getID() {
        return ID;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ID == null) ? 0 : ID.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ticket other = (Ticket) obj;
		if (ID == null) {
			if (other.ID != null)
				return false;
		} else if (!ID.equals(other.ID))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Ticket [ID=" + ID + ", resource=" + resource + ", username=" + username + ", property=" + property + "]";
	}
}
