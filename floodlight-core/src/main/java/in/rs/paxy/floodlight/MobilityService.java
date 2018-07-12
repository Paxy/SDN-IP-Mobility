package in.rs.paxy.floodlight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFType;
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
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.IListener.Command;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;

public class MobilityService implements IOFMessageListener, IFloodlightModule {
	protected IFloodlightProviderService floodlightProvider;
	private IOFSwitchService switchService;
	private ArrayList<DatapathId> devices;
	protected static Logger logger;
	private MobilityServiceTable mobilityServiceTable;

	private String[] controlledIPs = { "192.168.111.0/24","192.168.222.0/24" };
	private static int timeout;

	@Override
	public String getName() {
		return MobilityService.class.getSimpleName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context
				.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);

		logger = LoggerFactory.getLogger(MobilityService.class);

		devices = new ArrayList<>();

		mobilityServiceTable = new MobilityServiceTable(this);

		logger.info("MobilityService module loaded");
		
		Map<String, String> configOptions = context.getConfigParams(this);
		timeout = Integer.parseInt(configOptions.get("timeout"));

		logger.info("MobilityService loaded timeout {}",timeout);

	}

	@Override
	public void startUp(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		switch (msg.getType()) {
		case PACKET_IN:
			Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
					IFloodlightProviderService.CONTEXT_PI_PAYLOAD);

			if (!devices.contains(sw.getId())) {
				Normal(sw.getId());
				//ControlledIPs(sw.getId());
				//BlockBrodcastMulticast(sw.getId());
				devices.add(sw.getId());
				logger.info("Switch {} added NORMAL flow", sw.getId()
						.toString());
			}

			if (eth.getEtherType() == EthType.IPv4) {
				IPv4 ipv4 = (IPv4) eth.getPayload();
				IPv4Address srcIP = ipv4.getSourceAddress();

				//logger.info("SRC IP {}", srcIP.toString());

				// if u tabeli natovanja
				String targetIP = srcIP.toString();
				String natIP = mobilityServiceTable.getPeerBySrc(targetIP);
				if (srcIP.compareTo(IPv4Address.of(targetIP)) == 0
						&& natIP != null) {

					SrcNat(sw, targetIP, natIP);
					DstNat(sw, natIP, targetIP);
					logger.info("Reactive added NAT flows {}<->{}", targetIP, natIP);
					//return Command.STOP;
				}
					

			}

			break;
		default:
			break;
		}
		return Command.CONTINUE;
	}

	public static void SrcNat(IOFSwitch sw, String targetIP, String natIp) {

		OFFactory myOF13Factory = sw.getOFFactory();

		Match myMatch = myOF13Factory
				.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_SRC,
						IPv4AddressWithMask.of(targetIP)).build();

		OFInstructions instructions = myOF13Factory.instructions();

		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();

		/* Use OXM to modify network layer dest field. */
		OFActionSetField setNwDst = actions
				.buildSetField()
				.setField(
						oxms.buildIpv4Src().setValue(IPv4Address.of(natIp))
								.build()).build();
		actionList.add(setNwDst);

		/* Output to a port is also an OFAction, not an OXM. */
		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.NORMAL).build();
		actionList.add(output);

		/* Supply the OFAction list to the OFInstructionApplyActions. */
		OFInstructionApplyActions applyActions = instructions
				.buildApplyActions().setActions(actionList).build();

		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);

		OFFlowAdd flowAdd = myOF13Factory.buildFlowAdd().setHardTimeout(timeout)
				.setIdleTimeout(timeout*2).setPriority(600).setMatch(myMatch)
				.setInstructions(instructionList).setOutPort(OFPort.NORMAL)
				.setTableId(TableId.of(0)).build();

		sw.write(flowAdd);

	}

	public static void DstNat(IOFSwitch sw, String natIP, String targetIP) {

		OFFactory myOF13Factory = sw.getOFFactory();

		Match myMatch = myOF13Factory
				.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_DST,
						IPv4AddressWithMask.of(natIP)).build();

		OFInstructions instructions = myOF13Factory.instructions();

		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();

		/* Use OXM to modify network layer dest field. */
		OFActionSetField setNwDst = actions
				.buildSetField()
				.setField(
						oxms.buildIpv4Dst()
								.setValue(IPv4Address.of(targetIP))
								.build()).build();
		actionList.add(setNwDst);

		/* Output to a port is also an OFAction, not an OXM. */
		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.NORMAL).build();
		actionList.add(output);

		/* Supply the OFAction list to the OFInstructionApplyActions. */
		OFInstructionApplyActions applyActions = instructions
				.buildApplyActions().setActions(actionList).build();

		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);

		OFFlowAdd flowAdd = myOF13Factory.buildFlowAdd().setHardTimeout(timeout)
				.setIdleTimeout(timeout*2).setPriority(500).setMatch(myMatch)
				.setInstructions(instructionList).setOutPort(OFPort.NORMAL)
				.setTableId(TableId.of(0)).build();

		sw.write(flowAdd);

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

	public void ControlledIPs(DatapathId dp) {
		for (int i = 0; i < controlledIPs.length; i++) {
			ControlledIPs(dp, controlledIPs[i]);
		}
	}

	public void ControlledIPs(DatapathId dp, String controlledNet) {

		IOFSwitch mySwitch = switchService.getSwitch(dp);
		OFFactory myOF13Factory = mySwitch.getOFFactory();

		Match myMatch = myOF13Factory
				.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setMasked(MatchField.IPV4_SRC,
						IPv4AddressWithMask.of(controlledNet)).build();

		OFInstructions instructions = myOF13Factory.instructions();

		ArrayList<OFAction> actionList = new ArrayList<OFAction>();
		OFActions actions = myOF13Factory.actions();
		OFOxms oxms = myOF13Factory.oxms();

		/* Output to a port is also an OFAction, not an OXM. */
		OFActionOutput output = actions.buildOutput().setMaxLen(0xFFffFFff)
				.setPort(OFPort.CONTROLLER).build();
		actionList.add(output);

		/* Supply the OFAction list to the OFInstructionApplyActions. */
		OFInstructionApplyActions applyActions = instructions
				.buildApplyActions().setActions(actionList).build();

		ArrayList<OFInstruction> instructionList = new ArrayList<OFInstruction>();
		instructionList.add(applyActions);

		OFFlowAdd flowAdd = myOF13Factory.buildFlowAdd().setPriority(101)
				.setMatch(myMatch).setInstructions(instructionList)
				.setOutPort(OFPort.CONTROLLER).setTableId(TableId.of(0))
				.build();

		mySwitch.write(flowAdd);

	}
	public void BlockBrodcastMulticast(DatapathId dp) {

		IOFSwitch mySwitch = switchService.getSwitch(dp);
		OFFactory myOF13Factory = mySwitch.getOFFactory();

		Match myMatch = myOF13Factory
				.buildMatch()
				.setMasked(MatchField.ETH_DST,
						MacAddress.of("01:00:00:00:00:00"),MacAddress.of("01:00:00:00:00:00")).build();

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
				.setOutPort(OFPort.ZERO).setTableId(TableId.of(0))
				.build();

		mySwitch.write(flowAdd);

	}
	
	public void floodNat(Peer peer){
		for(DatapathId device: devices){
			IOFSwitch sw = switchService.getSwitch(device);
			SrcNat(sw, peer.tempIP, peer.virtualIP);
			DstNat(sw, peer.virtualIP, peer.tempIP);
			logger.info("Proactive added NAT flows {}<->{}", peer.tempIP, peer.virtualIP);
		}
	}

}
