package datastruct;

public class MapNode {
	/**
	 * ���id
	 */
	private Integer nodeId;
	/**
	 * lat ά�� lng ����
	 */
	private double lat, lng;

	public MapNode(int nodeId, double lat, double lng) {
		this.nodeId = nodeId;
		this.lat = lat;
		this.lng = lng;
	}

	public int getNodeId() {
		return nodeId;
	}

	public String getNodeIdS() {
		return nodeId.toString();
	}

	public double getLat() {
		return lat;
	}

	public double getLng() {
		return lng;
	}

	public void printData() {
		System.out.println(nodeId + " " + lat + " " + lng);
	}
}
