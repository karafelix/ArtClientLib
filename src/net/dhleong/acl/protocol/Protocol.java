package net.dhleong.acl.protocol;

import net.dhleong.acl.iface.PacketFactoryRegistry;

/**
 * Registers a set of PacketFactories with a PacketFactoryRegistry.
 * @author rjwut
 */
public interface Protocol {
	/**
	 * Creates PacketFactory objects for each packet in the protocol and
	 * registers them with the given PacketFactoryRegistry.
	 */
	public void registerPacketFactories(PacketFactoryRegistry registry);
}