package net.dhleong.acl.net;

import java.io.IOException;

import net.dhleong.acl.ArtemisPacket;
import net.dhleong.acl.enums.ConnectionType;

public abstract class BaseArtemisPacket implements ArtemisPacket {
    protected abstract void appendPacketDetail(StringBuilder b);

    protected final ConnectionType mConnectionType;
    protected final int mType;

    public BaseArtemisPacket(ConnectionType connectionType) {
    	this(connectionType, 0);
    }

    public BaseArtemisPacket(ConnectionType connectionType, int packetType) {
        mConnectionType = connectionType;
        mType = packetType;
    }

    @Override
    public ConnectionType getConnectionType() {
        return mConnectionType;
    }
    
    @Override
    public int getType() {
        return mType;
    }

    @Override
    public void write(PacketWriter writer) throws IOException {
    	throw new UnsupportedOperationException(
    			getClass().getSimpleName() + " does not support write()"
    	);
    }

    @Override
    public final String toString() {
    	StringBuilder b = new StringBuilder();
    	b.append('[').append(getClass().getSimpleName()).append("] ");
    	appendPacketDetail(b);
    	return b.toString();
    }
}