package xc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import traj.database.io.TrajDataFileInput;
import traj.util.Point;
import traj.util.Trajectory;
import datastruct.LandMarkGraph;
import datastruct.MapEdge;
import datastruct.MapNode;

public class Graph {
	private Map<Integer, List<Integer>> vertexMap;// �ڽӱ�
	private Map<Integer, MapNode> nodeMap; // �洢���е�
	private Map<Integer, MapEdge> edgeMap; // �洢���б�
	private Map<Integer, Integer> edgeDensity; // �ߵ��ܶ�
	private List<Integer> LandMarknode; // top-4000���ߵ�id
	private LandMarkGraph lmg;

	public Graph() {
		// ��ʼ���ڽӱ�
		vertexMap = new HashMap<Integer, List<Integer>>();
		nodeMap = new HashMap<Integer, MapNode>();
		edgeMap = new HashMap<Integer, MapEdge>();
		edgeDensity = new HashMap<Integer, Integer>();
		init("MapData/edges.txt", "MapData/vertices.txt");
	}

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

	// �켣��ƥ��·��
	private MapEdge getNearEdge(double lat, double lng) {
		MapEdge e = null;
		double distance = 1000;
		Iterator iter = edgeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			MapEdge tempE = (MapEdge) entry.getValue();
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

	// �㵽ֱ�߾���
	private double getDistance(double lat, double lng, int node1, int node2) {
		MapNode n1 = nodeMap.get(node1);
		MapNode n2 = nodeMap.get(node2);
		double x1 = n1.getxPoint();
		double y1 = n1.getyPoint();
		double x2 = n2.getxPoint();
		double y2 = n2.getyPoint();
		// System.out.println(x0+" "+y0+" "+x1+" "+y1+" "+x2+" "+y2);
		double space = 0;
		double a, b, c;
		a = lineSpace(x1, y1, x2, y2);// �߶εĳ���
		b = lineSpace(x1, y1, lat, lng);// (x1,y1)����ľ���
		c = lineSpace(x2, y2, lat, lng);// (x2,y2)����ľ���
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

	private double lineSpace(double x1, double y1, double x2, double y2) {
		double lineLength = 0;
		lineLength = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		return lineLength;
	}

	public void getroute(MapNode mapNode, MapNode mapNode2) {
		lmg = new LandMarkGraph(edgeDensity);
		MapEdge mapedge = getNearEdge(mapNode.getxPoint(), mapNode.getyPoint());
		MapEdge mapedge2 = getNearEdge(mapNode2.getxPoint(),
				mapNode2.getyPoint());
		List<Integer> roughroute = lmg.getRoughRouting(mapedge.getEdgeId(),
				mapedge2.getEdgeId());
		 for (Integer i : roughroute) {
		 System.out.println(i);
		 }

	}

}
