#!/usr/bin/python


from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import Node
from mininet.log import setLogLevel, info
from mininet.cli import CLI
from mininet.log import info, output, warn, setLogLevel
from mininet.node import OVSSwitch

from random import randint


class MobilitySwitch( OVSSwitch ):
    "Switch that can reattach and rename interfaces"

    def delIntf( self, intf ):
        "Remove (and detach) an interface"
        port = self.ports[ intf ]
        del self.ports[ intf ]
        del self.intfs[ port ]
        del self.nameToIntf[ intf.name ]

    def addIntf( self, intf, rename=False, **kwargs ):
        "Add (and reparent) an interface"
        OVSSwitch.addIntf( self, intf, **kwargs )
        intf.node = self
        if rename:
            self.renameIntf( intf )

    def attach( self, intf ):
        "Attach an interface and set its port"
        port = self.ports[ intf ]
        if port:
            if self.isOldOVS():
                self.cmd( 'ovs-vsctl add-port', self, intf )
            else:
                self.cmd( 'ovs-vsctl add-port', self, intf,
                          '-- set Interface', intf,
                          'ofport_request=%s' % port )
            self.validatePort( intf )

    def validatePort( self, intf ):
        "Validate intf's OF port number"
        ofport = int( self.cmd( 'ovs-vsctl get Interface', intf,
                                'ofport' ) )
        if ofport != self.ports[ intf ]:
            warn( 'WARNING: ofport for', intf, 'is actually', ofport,
                  '\n' )

    def renameIntf( self, intf, newname='' ):
        "Rename an interface (to its canonical name)"
        intf.ifconfig( 'down' )
        if not newname:
            newname = '%s-eth%d' % ( self.name, self.ports[ intf ] )
        intf.cmd( 'ip link set', intf, 'name', newname )
        del self.nameToIntf[ intf.name ]
        intf.name = newname
        self.nameToIntf[ intf.name ] = intf
        intf.ifconfig( 'up' )

    def moveIntf( self, intf, switch, port=None, rename=True ):
        "Move one of our interfaces to another switch"
        self.detach( intf )
        self.delIntf( intf )
        switch.addIntf( intf, port=port, rename=rename )
        switch.attach( intf )

    def moveHost( self, host, oldSwitch, newSwitch, newPort=None ):
            "Move a host from old switch to new switch"
            hintf, sintf = host.connectionsTo( oldSwitch )[ 0 ]
            oldSwitch.moveIntf( sintf, newSwitch, port=newPort )
            return hintf, sintf

class PaxySim( Node ):
    "A Node with IP forwarding enabled."

    def config( self, **params ):
        super( PaxySim, self).config( **params )
        # Enable forwarding on the router
        self.cmd( 'sysctl net.ipv4.ip_forward=1' )

    def terminate( self ):
        self.cmd( 'sysctl net.ipv4.ip_forward=0' )
        super( PaxySim, self ).terminate()


class NetworkTopo( Topo ):
    "PaxySimulation"

    def build( self, **_opts ):

        cr = self.addNode( 'cr', cls=PaxySim, ip='192.168.100.1/30' )
        
        dr1 = self.addNode( 'dr1', cls=PaxySim, ip='192.168.111.1/24' )
        dr2 = self.addNode( 'dr2', cls=PaxySim, ip='192.168.222.1/24' )
        
        as1, as2 = [ self.addSwitch( s ) for s in ( 'as1', 'as2' ) ]
        
        self.addLink( as1, dr1, intfName2='dr1-eth1',
                      params2={ 'ip' : '192.168.111.1/24' } )  
        self.addLink( as2, dr2, intfName2='dr2-eth1',
                      params2={ 'ip' : '192.168.222.1/24' } )
        
        self.addLink( cr, dr1, intfName1='cr-eth1', intfName2='dr1-eth2', params1={ 'ip' : '192.168.100.1/30' }, params2={ 'ip' : '192.168.100.2/30' } )
        self.addLink( cr, dr2, intfName1='cr-eth2', intfName2='dr2-eth2', params1={ 'ip' : '192.168.100.5/30' }, params2={ 'ip' : '192.168.100.6/30' } )

        ap11, ap12, ap21, ap22 = [ self.addSwitch( s ) for s in ( 'ap11', 'ap12','ap21','ap22' ) ]
        
        for asn, ap in [ (as1, ap11), (as1, ap12), (as2, ap21), (as2,ap22) ]:
            self.addLink( asn, ap )
            
        srv = self.addHost( 'srv', ip='10.0.0.10/24',defaultRoute='via 10.0.0.1' )    
        self.addLink( cr, srv, intfName1='cr-eth5', params1={ 'ip' : '10.0.0.1/24' })
        
        m = self.addHost( 'm', ip='192.168.111.3/24',defaultRoute='via 192.168.111.1' )    
        self.addLink(m,ap11);

   
        
        
def addRoutes(net):
    "Add static routes"
    
    net['cr'].cmd( 'ip route add 192.168.111.0/24 via 192.168.100.2')
    net['cr'].cmd( 'ip route add 192.168.222.0/24 via 192.168.100.6')
    
    net['dr1'].cmd( 'route add default gw 192.168.100.1 dr1-eth2')
    net['dr2'].cmd( 'route add default gw 192.168.100.5 dr2-eth2')

    
def run():
    "Test PaxySimulation"
    topo = NetworkTopo()
    net = Mininet( topo=topo, switch=MobilitySwitch )  # controller is used by s1-s3
    net.start()
    addRoutes(net)
    info( '*** Routing Table on Routers:\nCR:\n' )
    info( net[ 'cr' ].cmd( 'route' ) )
    #info( 'DR1:\n' )
    #info( net[ 'dr1' ].cmd( 'route' ) )
    #info( 'DR2:\n' )
    #info( net[ 'dr2' ].cmd( 'route' ) )
    
    info( '* Testing network\n' )
    net.pingAll()
    
    
    CLI( net )
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    run()


