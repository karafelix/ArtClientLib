package net.dhleong.acl.net.helm;

import net.dhleong.acl.net.ShipActionPacket;

/**
 * Request to dock. The request is automatically targeted at the nearest
 * station.
 * @author dhleong
 */
public class HelmRequestDockPacket extends ShipActionPacket {
    public HelmRequestDockPacket() {
        super(TYPE_REQUEST_DOCK, 0);
    }

	@Override
	protected void appendPacketDetail(StringBuilder b) {
		// do nothing
	}
}