package net.floodlightcontroller.core;

import java.net.SocketAddress;
import java.util.List;

import java.util.Date;
import net.floodlightcontroller.core.internal.IOFConnectionListener;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFRequest;
import org.projectfloodlight.openflow.protocol.OFStatsReply;
import org.projectfloodlight.openflow.protocol.OFStatsRequest;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.OFAuxId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class NullConnection implements IOFConnectionBackend, IOFMessageWriter {
    private static final Logger logger = LoggerFactory.getLogger(NullConnection.class);

    
    public boolean isConnected() {
        return false;
    }

    
    public Date getConnectedSince() {
        return null;
    }

    private void warn() {
        logger.debug("Switch {} not connected -- cannot send message", getDatapathId());
    }

    
    public void write(OFMessage m) {
        warn();
    }

    
    public void write(Iterable<OFMessage> msglist) {
        warn();
    }

    
    public SocketAddress getRemoteInetAddress() {
        return null;
    }

    
    public SocketAddress getLocalInetAddress() {
        return null;
    }

    
    public OFFactory getOFFactory() {
        return OFFactories.getFactory(OFVersion.OF_13);
    }

    
    public <REPLY extends OFStatsReply> ListenableFuture<List<REPLY>> writeStatsRequest(
            OFStatsRequest<REPLY> request) {
        return Futures.immediateFailedFuture(new SwitchDisconnectedException(getDatapathId()));
    }

    
    public void cancelAllPendingRequests() {
        // noop
    }

    
    public void flush() {
        // noop
    }

    
    public <R extends OFMessage> ListenableFuture<R> writeRequest(OFRequest<R> request) {
        return Futures.immediateFailedFuture(new SwitchDisconnectedException(getDatapathId()));
    }

    
    public void disconnect(){
        // noop
    }

    public void disconnected() {
        // noop
    }

    
    public boolean isWritable() {
        return false;
    }

    
    public DatapathId getDatapathId() {
        return DatapathId.NONE;
    }

    
    public OFAuxId getAuxId() {
        return OFAuxId.MAIN;
    }

    
    public void setListener(IOFConnectionListener listener) {
    }

}