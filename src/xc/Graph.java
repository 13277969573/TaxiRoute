package xc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import traj.database.io.TrajDataFileInput;
import traj.util.Point;
import traj.util.Trajectory;

public class Graph {
	private Map<Integer, List<Integer>> vertexMap;// �ڽӱ�
	private Map<Integer, MapNode> nodeMap; // �洢���е�
	private Map<Integer, MapEdge> edgeMap; // �洢���б�
	private Map<Integer, Integer> edgeDensity; // �ߵ��ܶ�
	private List<Integer> LandMarknode; // ��һ������Ϊ�ߵ�id���ڶ�������Ϊ�߶���

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

	// ����landmark
	public void getLandMarks() {
		try {
			String path = "G:/taxidata/mapMatchingResult/RoadSeqence";
			File file = new File(path);
			File[] filelist = file.listFiles();
			// ͳ���ܶ�
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i].getAbsolutePath();
				String thisLine = null;
				BufferedReader br = new BufferedReader(new FileReader(filename));
				while ((thisLine = br.readLine()) != null) {
					if (thisLine.length() != 1) {
						String[] a = thisLine.split(";");
						int roadid = Integer.parseInt(a[0].trim());
						if (edgeDensity.containsKey(roadid)) {
							edgeDensity
									.put(roadid, edgeDensity.get(roadid) + 1);
						} else {
							edgeDensity.put(roadid, 1);
						}
					}
				}
				br.close();
				System.out.println(filelist[i].getName() + "  ͳ�ƽ���");
			}
			// �ܶ�����
			List<Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(
					edgeDensity.entrySet());
			Collections.sort(list,
					new Comparator<Map.Entry<Integer, Integer>>() {
						public int compare(Entry<Integer, Integer> o1,
								Entry<Integer, Integer> o2) {
							return (o2.getValue() - o1.getValue());
						}
					});
			// ����ǰ4000������
			String filename = "G:/taxidata/mapMatchingResult/edgeDensity.txt";
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < 4000; i++) {
				Entry<Integer, Integer> mapping = list.get(i);
				bw.write(mapping.getKey().toString());
				bw.newLine();
			}
			bw.flush();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println("�ܶ�ͳ�����");
		}

	}

	// roadseqenceת����landmarkseqence
	public void ConvertTraToLandMarkSeq() {
		// ��ʼ��landmark�ر���Ϣ
		try {
			LandMarknode = new ArrayList<Integer>();
			String thisLine = null;
			BufferedReader br = new BufferedReader(new FileReader(
					"G:/taxidata/mapMatchingResult/edgeDensity.txt"));
			while ((thisLine = br.readLine()) != null) {
				int a = Integer.parseInt(thisLine);
				LandMarknode.add(a);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		// roadseqenceת����landmarkseqence
		try {
			String path = "G:/taxidata/mapMatchingResult/RoadSeqence";
			File file = new File(path);
			File[] filelist = file.listFiles();
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i].getAbsolutePath();
				String thisLine = null;
				BufferedReader br = new BufferedReader(new FileReader(filename));
				BufferedWriter bw = new BufferedWriter(new FileWriter(
						"G:/taxidata/mapMatchingResult/LandMarkSeqence/LandSeqence "
								+ filelist[i].getName()));
				while ((thisLine = br.readLine()) != null) {
					if (thisLine.length() != 1) {
						String[] a = thisLine.split(";");
						int roadid = Integer.parseInt(a[0].trim());
						if (LandMarknode.contains(roadid)) {
							bw.write(a[0] + ";" + a[1]);
							bw.newLine();
						}
					} else {
						bw.write(" ");
						bw.newLine();
					}
				}
				bw.flush();
				bw.close();
				br.close();
				System.out.println(filelist[i].getName() + " ת�����");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

	// ����landmark�����й켣 ��ʼ��landmarkgraph
	public void initLandMarkGraph() {
		// ��ʼ��landmark�ر���Ϣ
		try {
			LandMarknode = new ArrayList<Integer>();
			String thisLine = null;
			BufferedReader br = new BufferedReader(new FileReader(
					"G:/taxidata/mapMatchingResult/edgeDensity.txt"));
			while ((thisLine = br.readLine()) != null) {
				int a = Integer.parseInt(thisLine);
				LandMarknode.add(a);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		// ͳ��candidate�ܶ�
		Map<String, List<String>> candidateedge = new HashMap<String, List<String>>();
		try {
			String path = "G:/taxidata/mapMatchingResult/LandMarkSeqence";
			File file = new File(path);
			File[] filelist = file.listFiles();
			for (int i = 0; i < filelist.length; i++) {
				String filename = filelist[i].getAbsolutePath();
				String oneLine = null;
				String twoLine = null;
				BufferedReader br = new BufferedReader(new FileReader(filename));
				oneLine = br.readLine();
				String[] s1 = oneLine.split(";");
				while ((twoLine = br.readLine()) != null) {
					if (twoLine.length() != 1) {
						String[] s2 = twoLine.split(";");
						int time = getTimeBetweenPoi(s1[1], s2[1]);
						if (time <= 10) {
							String candidateid = s1[0].trim() + "+"
									+ s2[0].trim();
							String day = (s1[1].split(" ")[1]).split("-")[2];
							String hour = (s1[1].split(" ")[2]).split(":")[0];
							if (candidateedge.containsKey(candidateid)) {
								candidateedge.get(candidateid).add(
										day + "+" + hour + "+" + time);
							} else {
								List<String> timelist = new ArrayList<String>();
								timelist.add(day + "+" + hour + "+" + time);
								candidateedge.put(candidateid, timelist);
							}
						}
						oneLine = twoLine;
						s1 = oneLine.split(";");
					} else {
						oneLine = br.readLine();
						twoLine = br.readLine();
					}
				}
				br.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// for (String key : candidateedge.keySet()) {
			// System.out.println(key);
			// List<String> value = candidateedge.get(key);
			// for (int i = 0; i < value.size(); i++) {
			// System.out.println(value.get(i));
			// }
			// System.out.println();
			// System.out.println();
			// }
			// System.out.println(candidateedge.size());
		}
		System.out.println("~~~~~~~~~~~~~");
		// ��candidateedge��ѡ�ߵ�landmarkedge
		Map<String, landmarkedge> landmarkedge = new HashMap<String, landmarkedge>();
		for (String key : candidateedge.keySet()) {
				if (candidateedge.get(key).size() >= 30) {
					landmarkedge.put(key,
							new landmarkedge(key, candidateedge.get(key)));
			}
		}
		System.out.println(landmarkedge.size());
	}

	// ��������ʱ��֮��
	private int getTimeBetweenPoi(String time1, String time2) {
		String[] a1 = time1.split(" ");
		String[] a2 = time2.split(" ");
		int minute1 = Integer.parseInt(a1[2].split(":")[1]);
		int minute2 = Integer.parseInt(a2[2].split(":")[1]);
		return ((minute2 + 60 - minute1) % 60);
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

}
