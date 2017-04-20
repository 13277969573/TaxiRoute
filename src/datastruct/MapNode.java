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
	
	/**
	 *mappMatchingʱ��¼��������������� 
	 */
	private  int mapMatchingRoadID;

	public MapNode(int nodeId, double lat, double lng) {
		this.nodeId = nodeId;
		this.lat = lat;
		this.lng = lng;
	}
	
	public MapNode(int nodeId, double lat, double lng,int mapMatchingRoadID) {
		this.nodeId = nodeId;
		this.lat = lat;
		this.lng = lng;
		this.mapMatchingRoadID=mapMatchingRoadID;
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

	public int getMapMatchingRoadID() {
		return mapMatchingRoadID;
	}

	public void printData() {
		System.out.println(nodeId + " " + lat + " " + lng);
	}
}
