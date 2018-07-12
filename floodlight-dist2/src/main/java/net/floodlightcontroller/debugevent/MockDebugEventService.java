package net.floodlightcontroller.debugevent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.debugevent.DebugEventResource.EventInfoResource;
import net.floodlightcontroller.debugevent.DebugEventService.EventCategoryBuilder;

public class MockDebugEventService implements IFloodlightModule, IDebugEventService {

    
    public <T> EventCategoryBuilder<T> buildEvent(Class<T> evClass) {
        DebugEventService des = new DebugEventService();
        return des.buildEvent(evClass);
    }

    
    public void flushEvents() {

    }

    
    public boolean containsModuleEventName(String moduleName,
                                           String eventName) {
        return false;
    }

    
    public boolean containsModuleName(String moduleName) {
        return false;
    }

    
    public List<EventInfoResource> getAllEventHistory() {
        return Collections.emptyList();
    }

    
    public List<EventInfoResource> getModuleEventHistory(String moduleName) {
        return Collections.emptyList();
    }

    
    public EventInfoResource getSingleEventHistory(String moduleName,
                                                   String eventName,
                                                   int numOfEvents) {
        return null;
    }

    
    public void resetAllEvents() {

    }

    
    public void resetAllModuleEvents(String moduleName) {

    }

    
    public void resetSingleEvent(String moduleName, String eventName) {

    }

    
    public List<String> getModuleList() {
        return Collections.emptyList();
    }

    
    public List<String> getModuleEventList(String moduleName) {
        return Collections.emptyList();
    }

    
    public void setAck(int eventId, long eventInstanceId, boolean ack) {

    }

    
    public Collection<Class<? extends IFloodlightService>>
            getModuleServices() {
        Collection<Class<? extends IFloodlightService>> services =
                new ArrayList<Class<? extends IFloodlightService>>(1);
        services.add(IDebugEventService.class);
        return services;
    }

    
    public Map<Class<? extends IFloodlightService>, IFloodlightService>
            getServiceImpls() {
        Map<Class<? extends IFloodlightService>, IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
        m.put(IDebugEventService.class, this);
        return m;
    }

    
    public Collection<Class<? extends IFloodlightService>>
            getModuleDependencies() {
        return null;
    }

    
    public
            void
            init(FloodlightModuleContext context)
                                                 throws FloodlightModuleException {

    }

    
    public
            void
            startUp(FloodlightModuleContext context)
                                                    throws FloodlightModuleException {

    }
}
