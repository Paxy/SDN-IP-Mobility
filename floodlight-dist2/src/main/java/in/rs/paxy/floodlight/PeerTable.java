package in.rs.paxy.floodlight;

import java.util.HashMap;

public class PeerTable {
	private HashMap<String, Peer> macMap;
	private HashMap<String, Peer> tempIPMap;
	private HashMap<String, Peer> virtualIPMAP;

	public PeerTable() {
		macMap = new HashMap<>();
		tempIPMap = new HashMap<>();
		virtualIPMAP = new HashMap<>();
	}

	public void addPeer(Peer peer) {
		macMap.put(peer.mac, peer);
		tempIPMap.put(peer.tempIP, peer);
		virtualIPMAP.put(peer.virtualIP, peer);
	}

	public void addPeer(String mac, String tempIP, String virtualIP) {
		Peer peer = new Peer();
		peer.mac = mac;
		peer.tempIP = tempIP;
		peer.virtualIP = virtualIP;
		addPeer(peer);
	}

	public Peer getPeerByMac(String mac) {
		return macMap.get(mac);
	}

	public Peer getPeerByTempIp(String tempIp) {
		return tempIPMap.get(tempIp);
	}

	public Peer getPeerByVirtualIp(String virtualIp) {
		return virtualIPMAP.get(virtualIp);
	}

}
