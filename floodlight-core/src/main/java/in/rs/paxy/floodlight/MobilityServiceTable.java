package in.rs.paxy.floodlight;

import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobilityServiceTable {
	private Logger logger;
	private PeerTable table;
	MobilityService mobilityService;

	public MobilityServiceTable(MobilityService mobilityService) {
		logger = LoggerFactory.getLogger(MobilityServiceTable.class);
		this.mobilityService = mobilityService;
		try {

			table = new PeerTable();
			// table.addPeer("00:00:00:00:00:11", "172.16.100.100",
			// "172.16.111.100");

			new Thread(new MobilityDiscovery(this)).start();

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}

	public String getPeerBySrc(String src) {
		Peer peer = table.getPeerByTempIp(src);
		if (peer == null)
			return null;
		return peer.virtualIP;
	}

	public void addPeer(String mac, String tempIp) {
		Peer peer = table.getPeerByMac(mac);
		if (peer == null) {
			peer = new Peer();
			peer.mac = mac;
			peer.tempIP = tempIp;
			peer.virtualIP = allocateFreeVritualIP();
			table.addPeer(peer);
		} else
			{
			peer.tempIP = tempIp;
			table.addPeer(peer);
			}

		// procative
		mobilityService.floodNat(peer);

	}

	private String allocateFreeVritualIP() {
		// rewrite method !
		String prefix = "172.16.111.";
		String ip;
		do {
			int nr = (int) (Math.random() * 255);
			ip = prefix + nr;
		} while (table.getPeerByVirtualIp(ip) != null);

		return ip;
	}
}
