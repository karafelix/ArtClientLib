package net.dhleong.acl.net.setup;

import net.dhleong.acl.net.ShipActionPacket;


/**
 * Ready packets seem to get sent after the client
 *  picks a station and joins. Maybe this will 
 *  hint the server to send all info for objs?
 *   
 * Client also seems to send this in ACK to
 *  a GameOverPacket 
 *   
 * @author dhleong
 *
 */
public class ReadyPacket extends ShipActionPacket {
    public ReadyPacket() {
        super(TYPE_READY, 0);
    }
}