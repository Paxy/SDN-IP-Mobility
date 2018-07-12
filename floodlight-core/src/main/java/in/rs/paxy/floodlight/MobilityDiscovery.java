package in.rs.paxy.floodlight;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobilityDiscovery implements Runnable {

	private Logger logger;
	private MobilityServiceTable table;

	public MobilityDiscovery(MobilityServiceTable table) {
		logger = LoggerFactory.getLogger(MobilityDiscovery.class);
		this.table=table;
	}
	
	public void run() {
		try{
		
			ServerSocket ss=new ServerSocket(2016);
			logger.info("MobilityDiscovery listening on port 2016");
			while(true){
				Socket sock = ss.accept();
				BufferedReader in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
				String cmd = in.readLine(); // mac#tempIP
				String[] split = cmd.split("#");
				if (split.length==2){
					table.addPeer(split[0],split[1]);
					logger.info("Mobility Peer {} added to table with address {}",split[0],split[1]);
				}
				
				sock.close();
			}
			
		}catch (Exception e){
			logger.error(e.getMessage());
		}
	}

}
