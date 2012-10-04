package dk.statsbiblioteket.doms.wowza.plugin.ticket;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "property", propOrder = {
        "name",
        "value"
})
public class TicketProperty {

	private String name;
	private String value;
	
	public TicketProperty() {
		super();
	}

	public TicketProperty(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "TicketProperty [name=" + name + ", value=" + value + "]";
	}
}