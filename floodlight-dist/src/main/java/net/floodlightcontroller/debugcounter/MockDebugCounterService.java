package net.floodlightcontroller.debugcounter;

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

public class MockDebugCounterService implements IFloodlightModule, IDebugCounterService {


    
    public Collection<Class<? extends IFloodlightService>>
            getModuleServices() {
        Collection<Class<? extends IFloodlightService>> services =
                new ArrayList<Class<? extends IFloodlightService>>(1);
        services.add(IDebugCounterService.class);
        return services;
    }

    
    public Map<Class<? extends IFloodlightService>, IFloodlightService>
            getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
            IFloodlightService> m =
                new HashMap<Class<? extends IFloodlightService>,
                    IFloodlightService>();
        m.put(IDebugCounterService.class, this);
        return m;
    }

    
    public Collection<Class<? extends IFloodlightService>>
            getModuleDependencies() {
        return null;
    }

    
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {

    }

    
    public void startUp(FloodlightModuleContext context) {

    }

    
    public boolean registerModule(String moduleName) {
        return true;
    }

    
    public IDebugCounter registerCounter(String moduleName,
                                         String counterHierarchy,
                                         String counterDescription,
                                         MetaData... metaData) {
        return new MockCounterImpl();
    }

    
    public boolean
    resetCounterHierarchy(String moduleName, String counterHierarchy) {
        return true;
    }

    
    public void resetAllCounters() {
    }

    
    public boolean resetAllModuleCounters(String moduleName) {
        return true;
    }

    
    public List<DebugCounterResource>
    getCounterHierarchy(String moduleName, String counterHierarchy) {
        return Collections.emptyList();
    }

    
    public List<DebugCounterResource> getAllCounterValues() {
        return Collections.emptyList();
    }

    
    public List<DebugCounterResource>
    getModuleCounterValues(String moduleName) {
        return Collections.emptyList();
    }

    public static class MockCounterImpl implements IDebugCounter {
        
        public void increment() {
        }

        
        public void add(long incr) {
        }

        
        public long getCounterValue() {
            return -1;
        }

		
		public long getLastModified() {
			return -1;
		}

		
		public void reset() {			
		}
    }

	
	public boolean removeCounterHierarchy(String moduleName,
			String counterHierarchy) {
		return true;
	}

}
