package xc;

import index.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import traj.database.io.TrajDataFileInput;
import traj.util.Point;
import traj.util.Trajectory;
import datastruct.LandMarkGraph;
import datastruct.MapEdge;
import datastruct.MapNode;

public class Graph {
	/**
	 * ·���ڽӱ�
	 */
	private Map<Integer, List<Integer>> vertexMap;
	/**
	 * �洢���� ���id-��
	 */
	private Map<Integer, MapNode> nodeMap;
	/**
	 * �洢���� �ߵ�id-��
	 */
	private Map<Integer, MapEdge> edgeMap;
	/**
	 * �ߵ� id-�ܶ�
	 */
	private Map<Integer, Integer> edgeDensity;
	/**
	 * �ر�ͼ
	 */
	private LandMarkGraph lmg;
	/**
	 * ��������
	 */
	private GridIndex gi;

	public Graph() {
		// ��ʼ���ڽӱ�
		vertexMap = new HashMap<Integer, List<Integer>>();
		nodeMap = new HashMap<Integer, MapNode>();
		edgeMap = new HashMap<Integer, MapEdge>();
		edgeDensity = new HashMap<Integer, Integer>();
		init("MapData/edges.txt", "MapData/vertices.txt");
		System.out.println("��ʼ����ͼ���");
		initGridindex();
		System.out.println("��ʼ�������������");
	}

	/**
	 * ��ʼ����������
	 */
	private void initGridindex() {
		MBR mapScale = new MBR(115.416666, 39.43333, 117.5000, 41.05);// ��ͼ�߽羭γ��
		double side = 2000;
		List<SnapPoint> ps = new ArrayList<SnapPoint>();
		for (Integer i : nodeMap.keySet()) {
			MapNode node = nodeMap.get(i);
			SnapPoint sp = new SnapPoint(node.getLng(), node.getLat(), null,
					node.getNodeIdS());
			ps.add(sp);
		}
		gi = new GridIndex(mapScale, side, ps);
	}

	/**
	 * ��ʼ��ͼ
	 * 
	 * @param edgeFile
	 *            ���ļ�
	 * @param nodeFile
	 *            ���ļ�
	 */
	private void init(String edgeFile, String nodeFile) {
		// ��ʼ������Ϣ
		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(nodeFile));
			while ((thisLine = br.readLine()) != null) {
				String[] a = thisLine.split("\\s+");
				int nId = Integer.parseInt(a[0]);
				MapNode n = new MapNode(Integer.parseInt(a[0]),
						Double.parseDouble(a[1]), Double.parseDouble(a[2]));
				nodeMap.put(nId, n);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// System.out.println("��ʼ���㼯�����");
		}

		// ��ʼ������Ϣ
		try {
			BufferedReader br = new BufferedReader(new FileReader(edgeFile));

			while ((thisLine = br.readLine()) != null) {
				String[] a = thisLine.split("\\s+");
				int eid = Integer.parseInt(a[0]);
				int n1 = Integer.parseInt(a[1]);
				int n2 = Integer.parseInt(a[2]);
				// System.out.println(a[0] + " " + a[1] + " " + a[2]);
				MapEdge e = new MapEdge(eid, n1, n2);
				edgeMap.put(eid, e);
				if (vertexMap.containsKey(n1)) {
					vertexMap.get(n1).add(eid);
				} else {
					List<Integer> ls = new ArrayList<Integer>();
					ls.add(eid);
					vertexMap.put(n1, ls);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// System.out.println("��ʼ���߼������");
		}

	}

	/**
	 * ·��ƥ�� ���⳵�켣->·��seqence
	 * 
	 * @param tdfi
	 * @param id
	 */
	public void mapMatching(TrajDataFileInput tdfi, int id) {
		// ·��ƥ��
		String filename = "H:/taxidata/mapMatchingResult/" + id + "-0" + ".txt";
		int index = 0;
		int count = 0;
		while (tdfi.hasNextTrajectory()) {
			if (count > 499) {
				index++;
				count -= 500;
				filename = "H:/taxidata/mapMatchingResult/" + id + "-" + index
						+ ".txt";
			} else {
				count++;
			}
			Trajectory tra = tdfi.readTrajectory();
			List<Point> tralist = tra.getTrajPtList();
			List<String> traresult = new ArrayList<String>();
			List<Integer> temp = new ArrayList<Integer>();
			for (int i = 0; i < tra.size(); i++) {
				Point p = tralist.get(i);
				MapEdge mapedge = getNearEdge(p.getLat(), p.getLng());
				if (!temp.contains(mapedge.getEdgeId())) {
					temp.add(mapedge.getEdgeId());
					traresult.add(mapedge.getEdgeId() + " ; " + p.getTime());
				}
			}
			// ���浹�ļ�
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(filename,
						true));
				for (String mapre : traresult) {
					bw.write(mapre);
					bw.newLine();
				}
				bw.write(" ");
				bw.newLine();
				bw.flush();
				bw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				System.out.println("����" + tra.getTrajID() + "ƥ�����");
			}
		}

	}

	/**
	 * �켣����
	 * 
	 * @param mapNode
	 *            ���
	 * @param mapNode2
	 *            �յ�
	 */
	public void getroute(MapNode mapNode, MapNode mapNode2) {
		lmg = new LandMarkGraph(edgeDensity);
		System.out.println("��ʼ���ر�ͼ���");
		MapEdge mapedge = getNearEdge(mapNode.getLat(), mapNode.getLng(),
				lmg.getLandMarknode());
		MapEdge mapedge2 = getNearEdge(mapNode2.getLat(), mapNode2.getLng(),
				lmg.getLandMarknode());
		List<Integer> roughroute = lmg.getRoughRoute(mapedge.getEdgeId(),
				mapedge2.getEdgeId());
		System.out.println("RoughRouteƥ�����");
		List<Integer> finalresult = new ArrayList<Integer>();
		for (int a = 0; a < roughroute.size() - 1; a++) {
			finalresult.add(roughroute.get(a));
			String twonodeid = getTwoNearNode(roughroute.get(a),
					roughroute.get(a + 1));
			List<Integer> tmpresult=getExactRoute(Integer.parseInt(twonodeid.split("\\+")[0]),Integer.parseInt(twonodeid.split("\\+")[1]));
			finalresult.addAll(tmpresult);
		}
		finalresult.add(roughroute.get(roughroute.size()-1));
		// SnapPoint sp = new SnapPoint(mapNode.getLng(), mapNode.getLat(),
		// null,
		// mapNode.getNodeIdS());
		// List<SnapPoint> spl = gi.getPointsFromGrids(sp, 1);
		// List<SnapPoint> spl = gi.getPointsFromGrid(50, 50);
		// for (SnapPoint tmpsp : spl) {
		// System.out.println(tmpsp.getId() + "   " + tmpsp.getLat() + "  "
		// + tmpsp.getLng());
		// }
	}

	/**
	 * ���������ȡ����֮������·��
	 * @param nodeid1
	 * @param nodeid2
	 * @return
	 */
	private List<Integer> getExactRoute(int nodeid1, int nodeid2) {
		MapNode node1 = nodeMap.get(nodeid1);
		MapNode node2 = nodeMap.get(nodeid2);
		IndexPoint ip1 = gi.getIndex(node1.getLng(), node1.getLat());
		IndexPoint ip2 = gi.getIndex(node2.getLng(), node2.getLat());
		int m, n, p, q;
		m = ip1.getIndexLat() < ip2.getIndexLat() ? ip1.getIndexLat() : ip2
				.getIndexLat();
		n = ip1.getIndexLat() > ip2.getIndexLat() ? ip1.getIndexLat() : ip2
				.getIndexLat();
		p = ip1.getIndexLng() < ip2.getIndexLng() ? ip1.getIndexLng() : ip2
				.getIndexLng();
		q = ip1.getIndexLng() > ip2.getIndexLng() ? ip1.getIndexLng() : ip2
				.getIndexLng();
		System.out.println(m + " " + n + " " + p + " " + q + " ");
		List<Integer> nodelist = new ArrayList<Integer>();
		Map<Integer,List<Integer>> vertex = new HashMap<Integer,List<Integer>>();
		for(int i=m;i<n+1;i++){
			for(int j=p;j<q+1;j++){
				List<SnapPoint> pslist = gi.getPointsFromGrid(i, j);
				for(SnapPoint sp : pslist){
					nodelist.add(Integer.parseInt(sp.getId()));
				}
			}
		}
		for(int i=0;i<nodelist.size();i++){
			for(int j=0;j<nodelist.size();j++){
				if(vertexMap.get(nodelist.get(i)).contains(nodelist.get(j))){
					if (vertex.containsKey(nodelist.get(i))) {
						vertex.get(nodelist.get(i)).add(nodelist.get(j));
					} else {
						List<Integer> tmp = new ArrayList<Integer>();
						tmp.add(nodelist.get(j));
						vertex.put(nodelist.get(i), tmp);
					}
				}
			}
		}
		List<Integer> exactway=Floyd(nodelist,vertex, nodeid1,  nodeid2);
		return exactway;
	}

	private List<Integer> Floyd(List<Integer> nodelist,
			Map<Integer, List<Integer>> vertex, int nodeid1, int nodeid2) {
		
		return null;
	}

	/**
	 * ����������֮��ȽϽ�������
	 * 
	 * @param edgeid1
	 * @param edgeid2
	 * @return
	 */
	private String getTwoNearNode(Integer edgeid1, Integer edgeid2) {
		MapEdge edge1 = edgeMap.get(edgeid1);
		MapEdge edge2 = edgeMap.get(edgeid2);
		MapNode node11 = nodeMap.get(edge1.getStartNode());
		MapNode node12 = nodeMap.get(edge1.getEndNode());
		MapNode node21 = nodeMap.get(edge2.getStartNode());
		MapNode node22 = nodeMap.get(edge2.getEndNode());
		double d1 = LineSpace(node11.getLat(), node11.getLng(),
				node21.getLat(), node21.getLng());
		double d2 = LineSpace(node12.getLat(), node12.getLng(),
				node21.getLat(), node21.getLng());
		double d3 = LineSpace(node11.getLat(), node11.getLng(),
				node22.getLat(), node22.getLng());
		double d4 = LineSpace(node12.getLat(), node12.getLng(),
				node22.getLat(), node22.getLng());
		double min = d1;
		if (min > d2) {
			min = d2;
		}
		if (min > d3) {
			min = d3;
		}
		if (min > d4) {
			min = d4;
		}
		if (min == d1) {
			return (node11.getNodeId() + "+" + node21.getNodeId());
		} else if (min == d2) {
			return (node12.getNodeId() + "+" + node21.getNodeId());
		} else if (min == d3) {
			return (node11.getNodeId() + "+" + node22.getNodeId());
		} else {
			return (node12.getNodeId() + "+" + node22.getNodeId());
		}
	}

	/**
	 * �켣��ƥ��·��
	 * 
	 * @param lat
	 *            ά��
	 * @param lng
	 *            ����
	 * @return
	 */
	private MapEdge getNearEdge(double lat, double lng) {
		MapEdge e = null;
		double distance = 1000;
		for (Integer i : edgeMap.keySet()) {
			MapEdge tempE = edgeMap.get(i);
			int node1 = tempE.getStartNode();
			int node2 = tempE.getEndNode();
			double tempDistance = getDistance(lat, lng, node1, node2);
			if (tempDistance < distance) {
				distance = tempDistance;
				e = tempE;
			}
		}
		// System.out.println(e.getEdgeId()+" "+distance);
		return e;
	}

	/**
	 * �켣��ƥ��·����
	 * 
	 * @param lat
	 *            ά��
	 * @param lng
	 *            ����
	 * @return
	 */
	private MapEdge getNearEdge(double lat, double lng,
			List<Integer> landMarknode) {
		MapEdge e = null;
		double distance = 1000;
		for (Integer landmarkid : landMarknode) {
			MapEdge tempE = edgeMap.get(landmarkid);
			int node1 = tempE.getStartNode();
			int node2 = tempE.getEndNode();
			double tempDistance = getDistance(lat, lng, node1, node2);
			if (tempDistance < distance) {
				distance = tempDistance;
				e = tempE;
			}
		}
		// System.out.println(e.getEdgeId()+" "+distance);
		return e;
	}

	/**
	 * �㵽ֱ�߾���
	 * 
	 * @param lat
	 *            ά��
	 * @param lng
	 *            ����
	 * @param node1
	 * @param node2
	 * @return
	 */
	private double getDistance(double lat, double lng, int node1, int node2) {
		MapNode n1 = nodeMap.get(node1);
		MapNode n2 = nodeMap.get(node2);
		double x1 = n1.getLat();
		double y1 = n1.getLng();
		double x2 = n2.getLat();
		double y2 = n2.getLng();
		// System.out.println(x0+" "+y0+" "+x1+" "+y1+" "+x2+" "+y2);
		double space = 0;
		double a, b, c;
		a = LineSpace(x1, y1, x2, y2);// �߶εĳ���
		b = LineSpace(x1, y1, lat, lng);// (x1,y1)����ľ���
		c = LineSpace(x2, y2, lat, lng);// (x2,y2)����ľ���
		if (c <= 0.000001 || b <= 0.000001) {
			space = 0;
			return space;
		}
		if (a <= 0.000001) {
			space = b;
			return space;
		}
		if (c * c >= a * a + b * b) {
			space = b;
			return space;
		}
		if (b * b >= a * a + c * c) {
			space = c;
			return space;
		}
		double p = (a + b + c) / 2;// ���ܳ�
		double s = Math.sqrt(p * (p - a) * (p - b) * (p - c));// ���׹�ʽ�����
		space = 2 * s / a;// ���ص㵽�ߵľ��루���������������ʽ��ߣ�
		return space;
	}

	/**
	 * �㵽�����
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private double LineSpace(double x1, double y1, double x2, double y2) {
		double lineLength = 0;
		lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return lineLength;
	}

}
