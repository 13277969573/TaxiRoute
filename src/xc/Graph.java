package xc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import traj.util.Point;

public class Graph {
	private Map<Integer, List<Integer>> vertexMap;// �ڽӱ�
	private Map<Integer, Point> nodeMap; // �洢���е�
	private Map<Integer, Edge> edgeMap; // �洢���б�

	public Graph() {
		// ��ʼ���ڽӱ�
		vertexMap = new HashMap<Integer, List<Integer>>();
		nodeMap = new HashMap<Integer, Point>();
		edgeMap = new HashMap<Integer, Edge>();
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
						Node n = new Node(Integer.parseInt(a[0]),
								Double.parseDouble(a[1]), Double.parseDouble(a[2]));
//						nodeMap.put(nId, n);
					}
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println("��ʼ���㼯�����");
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
						Edge e = new Edge(eid, n1, n2);
						edgeMap.put(eid, e);
						if (vertexMap.containsKey(n1)) {
							vertexMap.get(n1).add(eid);
						} else {
							List<Integer> ls = new ArrayList<Integer>();
							ls.add(eid);
							vertexMap.put(n1, ls);
						}
						if (vertexMap.containsKey(n2)) {
							vertexMap.get(n2).add(eid);
						} else {
							List<Integer> ls = new ArrayList<Integer>();
							ls.add(eid);
							vertexMap.put(n2, ls);
						}
					}
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println("��ʼ���߼������");
				}	
	}
}

class Edge {
	private int edgeId;
	private int startNode, endNode;

	// private double weight;

	public Edge(int edgeId, int startNode, int endNode) {
		this.edgeId = edgeId;
		this.startNode = startNode; //
		this.endNode = endNode;//
		// this.weight = weight; //
	}

	public int getStartNode() {
		return startNode;
	}

	public int getEndNode() {
		return endNode;
	}

	public int getEdgeId() {
		return edgeId;
	}

	public void printData() {
		System.out.println(edgeId + " " + startNode + " " + endNode);
	}
}

class Node {
	private int nodeId;
	private double x, y;

	public Node(int nodeId, double x, double y) {
		this.nodeId = nodeId;
		this.x = x;
		this.y = y;
	}

	public int getNodeId() {
		return nodeId;
	}

	public double getxPoint() {
		return x;
	}

	public double getyPoint() {
		return y;
	}

	public void printData() {
		System.out.println(nodeId + " " + x + " " + y);
	}
}
