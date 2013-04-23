package dk.statsbiblioteket.chaos.wowza.plugin.bes;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "objectstatus", propOrder = {
        "completionPercentage",
        "positionInQueue",
        "serviceUrl",
        "status",
        "streamId"
})
@XmlRootElement(name = "objectstatus")
public class ObjectStatus {

	private String completionPercentage;
	private String positionInQueue;
	private String serviceUrl;
	private String status;
	private String streamId;
	
	public ObjectStatus() {
		super();
	}
	public String getCompletionPercentage() {
		return completionPercentage;
	}
	public void setCompletionPercentage(String completionPercentage) {
		this.completionPercentage = completionPercentage;
	}
	public String getPositionInQueue() {
		return positionInQueue;
	}
	public void setPositionInQueue(String positionInQueue) {
		this.positionInQueue = positionInQueue;
	}
	public String getServiceUrl() {
		return serviceUrl;
	}
	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStreamId() {
		return streamId;
	}
	public void setStreamId(String streamId) {
		this.streamId = streamId;
	}
	
	@Override
	public String toString() {
		return "ObjectStatus [completionPercentage=" + completionPercentage
				+ ", positionInQueue=" + positionInQueue + ", serviceUrl="
				+ serviceUrl + ", status=" + status + ", streamId=" + streamId
				+ "]";
	}
	
	
}
