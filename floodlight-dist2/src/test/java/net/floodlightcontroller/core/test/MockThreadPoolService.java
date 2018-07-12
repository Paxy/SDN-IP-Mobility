/**
 *    Copyright 2013, Big Switch Networks, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 **/

package net.floodlightcontroller.core.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.threadpool.IThreadPoolService;

public class MockThreadPoolService implements IFloodlightModule, IThreadPoolService {
    
    protected ScheduledExecutorService mockExecutor = new MockScheduledExecutor();

    /**
     * Return a mock executor that will simply execute each task 
     * synchronously once.
     */
    
    public ScheduledExecutorService getScheduledExecutor() {
        return mockExecutor;
    }

    // IFloodlightModule
    
    
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IThreadPoolService.class);
        return l;
    }

    
    public Map<Class<? extends IFloodlightService>, IFloodlightService>
            getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
            IFloodlightService> m = 
                new HashMap<Class<? extends IFloodlightService>,
                    IFloodlightService>();
        m.put(IThreadPoolService.class, this);
        // We are the class that implements the service
        return m;
    }

    
    public Collection<Class<? extends IFloodlightService>>
            getModuleDependencies() {
        // No dependencies
        return null;
    }

    
    public void init(FloodlightModuleContext context)
                                 throws FloodlightModuleException {
    }

    
    public void startUp(FloodlightModuleContext context) {
        // no-op
    }
}
