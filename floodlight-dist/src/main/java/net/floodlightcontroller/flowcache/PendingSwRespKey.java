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

package net.floodlightcontroller.flowcache;

@Deprecated
public class PendingSwRespKey {
    long swDpid;
    int  transId;

    public PendingSwRespKey(long swDpid, int transId) {
        this.swDpid  = swDpid;
        this.transId = transId;
    }

    
    public int hashCode() {
        final int prime = 97;
        Long dpid   = swDpid;
        Integer tid = transId;
        return (tid.hashCode()*prime + dpid.hashCode());
    }

    
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PendingSwRespKey)) {
            return false;
        }
        PendingSwRespKey other = (PendingSwRespKey) obj;
        if ((swDpid != other.swDpid) || (transId != other.transId)) {
            return false;
        }
        return true;
    }

    
    public String toString() {
        return Long.toHexString(swDpid)+","+Integer.toString(transId);
    }
}
