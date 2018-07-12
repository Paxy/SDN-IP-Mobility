package in.rs.paxy.floodlight;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.DHCP;
import net.floodlightcontroller.packet.DHCPPacketType;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.UDP;

public class HostDiscovery implements IOFMessageListener, IFloodlightModule {

	protected IFloodlightProviderService floodlightProvider;
	private IOFSwitchService switchService;
	protected static Logger logger;
	private ArrayList<DatapathId> devices;
	private HashMap<IPv4Address, Long> buffer;

	private String prefix = "172.16.100.0/24";
	private String server="127.0.0.1";
	private int timeout=300000;

	
	public String getName() {
		return HostDiscovery.class.getSimpleName();
	}

	
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// Collection<Class<? extends IFloodlightService>> l = new
		// ArrayList<Class<? extends IFloodlightService>>();
		// l.add(IFloodlightProviderService.class);
		// return l;
		return null;
	}

	
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context
				.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);

		logger = LoggerFactory.getLogger(HostDiscovery.class);

		devices = new ArrayList<>();

		buffer = new HashMap<IPv4Address, Long>();
		
		// read our config options
		Map<String, String> configOptions = context.getConfigParams(this);
		prefix = configOptions.get("prefix");
		server = configOptions.get("server");
		timeout = Integer.parseInt(configOptions.get("timeout"));

		logger.info("HostDescovery module loaded for prefix {}",prefix);
		logger.info("HostDescovery module loaded server {}",server);
		logger.info("HostDescovery module loaded timeout {}",timeout);

	}

	
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);

	}

	
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch (msg.getType()) {
		case PACKET_IN:
			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
					IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

			if (!devices.contains(sw.getId())) {
				Normal(sw.getId());
				SendPrefix(sw.getId());
				// BlockBrodcastMulticast(sw.getId());
				// AllowARP(sw.getId());
				// AllowBOOTP(sw.getId());
				devices.add(sw.getId());
				logger.info("Switch {} added NORMAL flow", sw.getId()
						.toString());
			}

			// Long sourceMACHash = eth.getSourceMACAddress().getLong();
			// if (!macAddresses.contains(sourceMACHash)) {
			// macAddresses.add(sourceMACHash);
			// logger.info("MAC Address: {} seen on switch: {}",
			// eth.getSourceMACAddress().toString(),
			// sw.getId().toString());
			// }

			MacAddress srcMac = eth.getSourceMACAddress();

			if (eth.getEtherType() == EthType.IPv4) {
				/* We got an IPv4 packet; get the payload from Ethernet */
				IPv4 ipv4 = (IPv4) eth.getPayload();
				IPv4Address srcIP = ipv4.getSourceAddress();
				IPv4AddressWithMask net = IPv4AddressWithMask.of(prefix);
			
				if (net.contains(srcIP)) {
					Long entry = buffer.get(srcIP);
					if (entry == null) {
						buffer.put(srcIP, System.currentTimeMillis());
						logger.info("New Client: {} mac: {}", srcIP.toString(),
								srcMac.toString());
						InformMobilityService(srcIP.toString(),
								srcMac.toString());
					} else if (System.currentTimeMillis() - entry > timeout) {
						buffer.put(srcIP, System.currentTimeMillis());
						logger.info("Still Client: {} mac: {}",
								srcIP.toString(), srcMac.toString());
						InformMobilityService(srcIP.toString(),
								srcMac.toString());
					}

				}

//				// // logger.info("SRC ip {}",srcIP.toString());
//				 if (ipv4.getProtocol().equals(IpProtocol.UDP)) {
//				// /* We got a UDP packet; get the payload from IPv4 */
//				 UDP udp = (UDP) ipv4.getPayload();
//				//
//				// /* Various getters and setters are exposed in UDP */
//				 TransportPort srcPort = udp.getSourcePort();
//				// TransportPort dstPort = udp.getDestinationPort();
//				//
//				//
//				//
//				 if (srcPort.getPort() == 67 ) {
//				 DHCP dhcpPacket = (DHCP) udp.getPayload();
//				 MacAddress clientHardwareAddress = dhcpPacket
//				 .getClientHardwareAddress();
//				 IPv4Address clientIPAddress = dhcpPacket
//				 .getClientIPAddress();
//				 //logger.info("UDP: {} mac: {}",srcPort,srcMac);
//				//
//				//
//				 if (clientIPAddress
//				 .compareTo(IPv4Address.of("0.0.0.0")) != 0) {
//					 buffer.put(srcIP, System.currentTimeMillis());
//						logger.info("DHCP Client: {} mac: {}",
//								srcIP.toString(), srcMac.toString());
//						InformMobilityService(srcIP.toString(),
//								srcMac.toString());
//				 }
//				
//				
//				 }
//				//
//				// /* Your logic here! */
//				}
		
			}
			break;
		default:
			break;
		}
		return Command.STOP;
	}

	private void InformMobilityService(String ip, String mac) {
		try {
			Socket sock = new Socket(server, 2016);
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					sock.getOutputStream()), true);
			out.println(mac + "#" + ip);
			sock.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void BlockBrodcastMulticast(DatapathId dp) {

		IOFSwitch mySwitch = switchService.getSwitch(dp);
		OFFactory myOF13Factory = mySwitch.getOFFactory();

		Match myMatch = myOF13Factory
				.buildMatch()
				.setExact(MatchField.IN_PORT, OFPort.of(2))
				.setMasked(MatchField.ETH_DST,
						MacAddress.of("01:00:00:00:00:00"),
						MacAddress.of("01:00:00:00:00:00")).build();

		OFInstructions instructions = myOF13Factory.instructions();

		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();

		/* Output to a port is also an OFAction, not an OXM. */
		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.ZERO).build();
		actionList.add(output);

		/* Supply the OFAction list to the OFInstructionApplyActions. */
		OFInstructionApplyActions applyActions = instructions
				.buildApplyActions().setActions(actionList).build();

		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);

		OFFlowAdd flowAdd = myOF13Factory.buildFlowAdd().setPriority(101)
				.setMatch(myMatch).setInstructions(instructionList)
				.setOutPort(OFPort.ZERO).setTableId(TableId.of(0)).build();

		mySwitch.write(flowAdd);

	}

	public void Normal(DatapathId dp) {

		IOFSwitch mySwitch = switchService.getSwitch(dp);
		OFFactory myOF13Factory = mySwitch.getOFFactory();

		Match myMatch = myOF13Factory.buildMatch().build();

		OFInstructions instructions = myOF13Factory.instructions();

		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();

		/* Output to a port is also an OFAction, not an OXM. */
		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.NORMAL).build();
		actionList.add(output);

		/* Supply the OFAction list to the OFInstructionApplyActions. */
		OFInstructionApplyActions applyActions = instructions
				.buildApplyActions().setActions(actionList).build();

		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);

		OFFlowAdd flowAdd = myOF13Factory.buildFlowAdd().setPriority(100)
				.setMatch(myMatch).setInstructions(instructionList)
				.setOutPort(OFPort.NORMAL).setTableId(TableId.of(0)).build();

		mySwitch.write(flowAdd);

	}

	public void AllowARP(DatapathId dp) {

		IOFSwitch mySwitch = switchService.getSwitch(dp);
		OFFactory myOF13Factory = mySwitch.getOFFactory();

		Match myMatch = myOF13Factory.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.of(0x0806)).build();

		OFInstructions instructions = myOF13Factory.instructions();

		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();

		/* Output to a port is also an OFAction, not an OXM. */
		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.NORMAL).build();
		actionList.add(output);

		/* Supply the OFAction list to the OFInstructionApplyActions. */
		OFInstructionApplyActions applyActions = instructions
				.buildApplyActions().setActions(actionList).build();

		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);

		OFFlowAdd flowAdd = myOF13Factory.buildFlowAdd().setPriority(102)
				.setMatch(myMatch).setInstructions(instructionList)
				.setOutPort(OFPort.NORMAL).setTableId(TableId.of(0)).build();

		mySwitch.write(flowAdd);

	}

	public void SendPrefix(DatapathId dp) {

		IOFSwitch mySwitch = switchService.getSwitch(dp);
		OFFactory myOF13Factory = mySwitch.getOFFactory();

		Match myMatch = myOF13Factory
				.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_SRC,
						IPv4AddressWithMask.of(prefix))
				.build();

		OFInstructions instructions = myOF13Factory.instructions();

		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();

		/* Output to a port is also an OFAction, not an OXM. */
		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.CONTROLLER).build();
		actionList.add(output);

		OFActionOutput output1 = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.NORMAL).build();
		actionList.add(output1);

		/* Supply the OFAction list to the OFInstructionApplyActions. */
		OFInstructionApplyActions applyActions = instructions
				.buildApplyActions().setActions(actionList).build();

		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);

		OFFlowAdd flowAdd = myOF13Factory.buildFlowAdd().setPriority(102)
				.setMatch(myMatch).setInstructions(instructionList)
				.setOutPort(OFPort.CONTROLLER).setTableId(TableId.of(0))
				.build();

		mySwitch.write(flowAdd);

	}

}
