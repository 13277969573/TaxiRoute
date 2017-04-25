package index;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datastruct.MapNode;

public class MapMatchingGridIndex {
	/**
	 * ��С��γ�� ���γ��
	 */
	private double minLng, minLat;
	private double maxLng, maxLat;
	/**
	 * ������γ�ȷ�����Ҫ����Ϊ���ٸ�һ�����ٸ�
	 */
	private int lngAmount, latAmount, alAmount;
	/**
	 * ÿ�����ӵı߳����ף�
	 */
	private double side;
	/**
	 * �������������е㼯�ϣ��б������ã�
	 */
	private List<MapNode> pointsList;
	/**
	 * �����б�,�б�ʾ���Ȼ��֣��б�ʾ���Ȼ���
	 */
	private Grid[][] grids;

	public MapMatchingGridIndex(MBR mapScale, double side) {
		this.side = side;

		initLngLat(mapScale);

		// ��ʼ����������
		initGrids();
	}

	/**
	 * ��ʼ����������
	 * 
	 * @throws IOException
	 */
	private void initGrids() {
		// ����ƽ�泤��
		double disLng = TrajMath.compMeterDistance(minLng, minLat, maxLng,
				minLat);
		double disLat = TrajMath.compMeterDistance(minLng, minLat, minLng,
				maxLat);
		// ���㳤������������
		this.lngAmount = ((int) (disLng / side)) + 1;
		this.latAmount = ((int) (disLat / side)) + 1;
		this.alAmount = lngAmount * latAmount;
		// ���������λ����
		this.grids = new Grid[lngAmount][latAmount];

		// ��ʼ�������б�,��������̫�࣬ѡ�񲻳�ʼ��
		// initGridsSub();
		// �������
		fillGrids();

	}

	private void fillGrids() {
		BufferedReader br;
		String thisLine = null;
		try {
			br = new BufferedReader(new FileReader("MapData/geos.txt"));
			while ((thisLine = br.readLine()) != null) {
				String a[] = thisLine.split("\\s+");
				int edgeID = Integer.parseInt(a[0]);
				for (int i = 1; i < a.length; i++) {
					double lat = Double.parseDouble(a[i]);
					i++;
					double lng = Double.parseDouble(a[i]);
					MapNode node = new MapNode(0, lat, lng, edgeID);
					IndexPoint indexPoint = getIndex(node);
					if (indexPoint != null) {
						if (grids[indexPoint.indexLng][indexPoint.indexLat] == null) {
							grids[indexPoint.indexLng][indexPoint.indexLat] = new Grid();
						} else {
							grids[indexPoint.indexLng][indexPoint.indexLat].substances
									.add(node);
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public int getRoadIDFromGrids(MapNode p) {
		// // ��ȡ�����
		IndexPoint indexPoint = getIndex(p);
		List<MapNode> pl = null;
		if (indexPoint != null) {
			if (grids[indexPoint.indexLng][indexPoint.indexLat] != null) {
				pl = grids[indexPoint.indexLng][indexPoint.indexLat].substances;
			} else {
				return -1;
			}
		}
		if (pl == null) {
			return -1;
		}
		double distance = Integer.MAX_VALUE;
		double tmpDistance;
		int roadID = -1;
		for (int i = 0; i < pl.size(); i++) {
			MapNode node = pl.get(i);
			tmpDistance = Math.sqrt(Math.pow(p.getLat() - node.getLat(), 2)
					+ Math.pow(p.getLng() - node.getLng(), 2));
			if (tmpDistance < distance) {
				roadID = node.getMapMatchingRoadID();
				distance = tmpDistance;
			}
		}
		return roadID;

		// // ��ʼ����
		// for (int i = indexLng - n; i <= indexLng + n; i++) {
		// for (int j = indexLat - n; j <= indexLat + n; j++) {
		// List<MapNode> ps = getPointsFromGrid(i, j);
		// for (int x = 0; x < ps.size(); x++) {
		// points.add(ps.get(x));
		// }
		// }
		// }
		// return points;
	}

	/**
	 * ���������±�ĵ�
	 * 
	 * @param point
	 *            ��Ҫ���������±�ĵ�
	 * @return �±�
	 */
	public IndexPoint getIndex(MapNode point) {
		if (point.getLng() < minLng || point.getLat() < minLat
				|| point.getLng() > maxLng || point.getLat() > maxLat)
			return null;
		return getIndex(point.getLng(), point.getLat());
	}

	/**
	 * * ���������±�ĵ�
	 * 
	 * @param lng
	 *            ����
	 * @param lat
	 *            γ��
	 * @return �±�
	 */
	public IndexPoint getIndex(double lng, double lat) {
		if (lng < minLng || lat < minLat || lng > maxLng || lat > maxLat)
			return null;
		IndexPoint indexPoint = new IndexPoint();
		double disLng = TrajMath.compMeterDistance(lng, minLat, minLng, minLat);
		double disLat = TrajMath.compMeterDistance(minLng, lat, minLng, minLat);
		indexPoint.indexLng = (int) (disLng / side);
		indexPoint.indexLat = (int) (disLat / side);
		return indexPoint;
	}

	private void initLngLat(MBR mapScale) {
		this.minLng = mapScale.minLng;
		this.minLat = mapScale.minLat;
		this.maxLng = mapScale.maxLng;
		this.maxLat = mapScale.maxLat;

	}

	/**
	 * ���ڵ��������
	 */
	class Grid {
		/**
		 * ��������ĵ��id����
		 */
		public List<MapNode> substances;

		public Grid() {
			this.substances = new ArrayList<MapNode>();
		}

	}
}
