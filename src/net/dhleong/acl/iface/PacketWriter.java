package net.dhleong.acl.iface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.SortedMap;

import net.dhleong.acl.enums.ConnectionType;
import net.dhleong.acl.enums.ObjectType;
import net.dhleong.acl.protocol.ArtemisPacket;
import net.dhleong.acl.protocol.Version;
import net.dhleong.acl.util.BitField;
import net.dhleong.acl.util.BoolState;
import net.dhleong.acl.world.ArtemisObject;

/**
 * Facilitates writing packets to an OutputStream. This object may be reused to
 * write as many packets as desired to a single OutputStream. To write a packet,
 * follow these steps:
 * 
 * 1. Invoke start().
 * 2. Write the payload data using the write*() methods. Payload data is
 *    buffered by the PacketWriter, not written immediately to the OutputStream.
 * 3. Invoke flush(). The proper values for the fields in the preamble will be
 *    automatically computed and written, followed by the payload. The entire
 *    packet is then flushed to the OutputStream.
 * 
 * Once flush() has been called, you can start writing another packet by
 * invoking start() again.
 * 
 * @author rjwut
 */
public class PacketWriter {
	private final OutputStream out;
	private ConnectionType mConnType;
	private Version version;

	private int mPacketType;
	private ByteArrayOutputStream baos;
	private ArtemisObject obj;
	private ObjectType objType;
	private BitField bitField;
	private ByteArrayOutputStream baosObj;
	private byte[] buffer = new byte[4];

	/**
	 * Creates a PacketWriter that writes packets to the given OutputStream.
	 */
	public PacketWriter(OutputStream out) {
		if (out == null) {
			throw new IllegalArgumentException(
					"The out argument cannot be null"
			);
		}

		this.out = out;
	}

	/**
	 * Returns the server version number, or null if unknown.
	 */
	public Version getVersion() {
		return version;
	}

	/**
	 * Sets the server version number. This is invoked by VersionPacket. 
	 */
	public void setVersion(Version version) {
		this.version = version;
	}

	/**
	 * Starts a packet of the given type.
	 */
	public PacketWriter start(ConnectionType connType, int packetType) {
		mConnType = connType;
		mPacketType = packetType;
		baos = new ByteArrayOutputStream();
		return this;
	}

	/**
	 * Convenience method for startObject(object, object.getType(), bits).
	 */
	public PacketWriter startObject(ArtemisObject object, Enum<?>[] bits) {
		return startObject(object, object.getType(), bits);
	}

	/**
	 * Starts writing a new entry into the packet for the given object,
	 * overriding the object's type with the specified ObjectType. If object
	 * entries for this packet have bit fields, an array of the possible enum
	 * values (not just the ones in this packet) should be provided; otherwise,
	 * the bits argument should be null.
	 */
	public PacketWriter startObject(ArtemisObject object, ObjectType type,
			Enum<?>[] bits) {
		assertStarted();
		obj = object;
		objType = type;
		bitField = new BitField(bits);
		baosObj = new ByteArrayOutputStream();
		return this;
	}

	/**
	 * Writes a single byte. You must invoke start() before calling this method.
	 */
	public PacketWriter writeByte(byte v) {
		assertStarted();
		baos.write(v);
		return this;
	}

	/**
	 * Writes a single byte for the current object. You must invoke
	 * startObject() before calling this method.
	 */
	public PacketWriter writeObjByte(byte v) {
		assertObjectStarted();
		baosObj.write(v);
		return this;
	}

	/**
	 * If the given byte is different from defaultValue, the byte is written
	 * to the packet, and the corresponding bit in the object's bit field is set;
	 * otherwise, nothing happens. You must invoke startObject() before calling
	 * this method.
	 */
	public PacketWriter writeByte(Enum<?> bit, byte v, byte defaultValue) {
		assertObjectStarted();

		if (v != defaultValue) {
			bitField.set(bit, true);
			baosObj.write(v);
		}

		return this;
	}

	/**
	 * If the given BoolState is known, it is written to the packet using the
	 * given number of bytes, and the corresponding bit in the object's bit
	 * field is set; otherwise, nothing happens. You must invoke startObject()
	 * before calling this method.
	 */
	public PacketWriter writeBool(Enum<?> bit, BoolState v, int byteCount) {
		assertObjectStarted();

		if (BoolState.isKnown(v)) {
			bitField.set(bit, true);
			byte[] bytes = new byte[byteCount];
			bytes[0] = (byte) (v.getBooleanValue() ? 1 : 0);
			writeBytes(baosObj, bytes);
		}

		return this;
	}

	/**
	 * Writes a short (two bytes). You must invoke start() before calling this
	 * method.
	 */
	public PacketWriter writeShort(int v) {
		assertStarted();
		writeShort(baos, v);
		return this;
	}

	/**
	 * If the given int is different from defaultValue, the int is coerced to a
	 * short and written to the packet, and the corresponding bit in the
	 * object's bit field is set; otherwise, nothing happens. You must invoke
	 * startObject() before calling this method.
	 */
	public PacketWriter writeShort(Enum<?> bit, int v, int defaultValue) {
		assertObjectStarted();

		if (v != defaultValue) {
			bitField.set(bit, true);
			writeShort(baosObj, v);
		}

		return this;
	}

	/**
	 * Writes an int (four bytes). You must invoke start() before calling this
	 * method.
	 */
	public PacketWriter writeInt(int v) {
		assertStarted();
		writeInt(baos, v);
		return this;
	}

	/**
	 * If the given int is different from defaultValue, the int is written to
	 * the packet, and the corresponding bit in the object's bit field is set;
	 * otherwise, nothing happens. You must invoke startObject() before calling
	 * this method.
	 */
	public PacketWriter writeInt(Enum<?> bit, int v, int defaultValue) {
		assertObjectStarted();

		if (v != defaultValue) {
			bitField.set(bit, true);
			writeInt(baosObj, v);
		}

		return this;
	}

	/**
	 * Writes a float (four bytes). You must invoke start() before calling this
	 * method.
	 */
	public PacketWriter writeFloat(float v) {
		return writeInt(Float.floatToRawIntBits(v));
	}

	/**
	 * If the given float is different from defaultValue, the float is written
	 * to the packet, and the corresponding bit in the object's bit field is
	 * set; otherwise, nothing happens. You must invoke startObject() before
	 * calling this method.
	 */
	public PacketWriter writeFloat(Enum<?> bit, float v, float defaultValue) {
		return writeInt(
				bit,
				Float.floatToRawIntBits(v),
				Float.floatToRawIntBits(defaultValue)
		);
	}

	/**
	 * Writes a String. This handles writing the string length and the
	 * terminating null character automatically. You must invoke start() before
	 * calling this method.
	 */
	public PacketWriter writeString(String str) {
		writeString(baos, str);
		return this;
	}

	/**
	 * If the given String is not null, it is written to the packet, and the
	 * corresponding bit in the object's bit field is set; otherwise, nothing
	 * happens. You must invoke startObject() before calling this method.
	 */
	public PacketWriter writeString(Enum<?> bit, String str) {
		assertObjectStarted();

		if (str != null) {
			bitField.set(bit, true);
			writeString(baosObj, str);
		}

		return this;
	}

	/**
	 * Writes a byte array. You must invoke start() before calling this method.
	 */
	public PacketWriter writeBytes(byte[] bytes) {
		assertStarted();
		writeBytes(baos, bytes);
		return this;
	}

	/**
	 * If the given byte array is not null, it is written to the packet, and the
	 * corresponding bit in the object's bit field is set; otherwise, nothing
	 * happens. You must invoke startObject() before calling this method.
	 */
	public PacketWriter writeBytes(Enum<?> bit, byte[] bytes) {
		assertObjectStarted();

		if (bytes != null) {
			bitField.set(bit, true);
			writeBytes(baosObj, bytes);
		}

		return this;
	}

	/**
	 * Retrieves the named unknown value as a byte array from the unknown
	 * properties map. If the retrieved value is not null, it is written to the
	 * packet; otherwise, the defaultValue byte array will be written. You must
	 * invoke startObject() before calling this method.
	 */
	public PacketWriter writeUnknown(String name, byte[] defaultValue) {
		assertObjectStarted();
		SortedMap<String, byte[]> unknownProps = obj.getUnknownProps();
		byte[] v = unknownProps != null ? unknownProps.get(name) : null;
		writeBytes(baosObj, v != null ? v : defaultValue);
		return this;
	}

	/**
	 * Retrieves the unknown value identified by the indicated bit as a byte
	 * array from the unknown properties map. If the retrieved value is not
	 * null, it is written to the packet and the corresponding bit in the
	 * object's bit field is set; otherwise, nothing happens. You must invoke
	 * startObject() before calling this method.
	 */
	public PacketWriter writeUnknown(Enum<?> bit) {
		assertObjectStarted();
		SortedMap<String, byte[]> unknownProps = obj.getUnknownProps();

		if (unknownProps != null) {
			byte[] v = unknownProps.get(bit.name());
	
			if (v != null) {
				bitField.set(bit, true);
				writeBytes(baosObj, v);
			}
		}

		return this;
	}

	/**
	 * Flushes the current object's bytes to the packet, but not to the wrapped
	 * OutputStream. This prepares the packet for writing another byte. You must
	 * invoke startObject() before calling this method. When this method
	 * returns, you will have to call startObject() again before you can write
	 * another object.
	 */
	public void endObject() {
		writeByte(objType.getId());
		writeInt(obj.getId());

		try {
			bitField.write(baos);
		} catch (IOException ex) {
			// ByteArrayOutputStream doesn't actually throw IOException; it
			// just inherits the declaration from the OutputStream class. Since
			// it won't ever actually happen, we wrap it in a RuntimeException.
			throw new RuntimeException(ex);
		}

		writeBytes(baosObj.toByteArray());
		obj = null;
		objType = null;
		bitField = null;
		baosObj = null;
	}

	/**
	 * Writes the completed packet to the OutputStream. You must invoke start()
	 * before calling this method. When this method returns, you will have to
	 * call start() again before you can write more data. The given Debugger
	 * will also be notified.
	 */
	public void flush(Debugger debugger) throws IOException {
		assertStarted();
		byte[] payload = baos.toByteArray();
		baos = null;
		writeIntToStream(ArtemisPacket.HEADER);				// header
		writeIntToStream(payload.length + 24);				// packet length
		writeIntToStream(mConnType.toInt());				// connection type
		writeIntToStream(0);								// padding
		writeIntToStream(payload.length + 4);				// remaining bytes
		writeIntToStream(mPacketType);						// packet type
		out.write(payload);									// payload
		out.flush();
		debugger.onSendPacketBytes(mConnType, mPacketType, payload);
	}

	/**
	 * Throws an IllegalStateException if start() has not been called since the
	 * time this object was constructed or since the last call to flush().
	 */
	private void assertStarted() {
		if (baos == null) {
			throw new IllegalStateException("Must invoke start() first");
		}
	}

	/**
	 * Throws an IllegalStateException if startObject() has not been called
	 * since the time this object was constructed or since the last call to
	 * endObject() or flush().
	 */
	private void assertObjectStarted() {
		if (baosObj == null) {
			throw new IllegalStateException("Must invoke startObject() first");
		}
	}

	/**
	 * Writes an int directly to the wrapped OutputStream. This is used by
	 * PacketWriter to write values in the preamble, which are all ints.
	 */
	private void writeIntToStream(int value) throws IOException {
		buffer[0] = (byte) (0xff & value);
		buffer[1] = (byte) (0xff & (value >> 8));
		buffer[2] = (byte) (0xff & (value >> 16));
		buffer[3] = (byte) (0xff & (value >> 24));
		out.write(buffer);
	}

	/**
	 * Writes an int (coerced into a short) to the given ByteArrayOutputStream.
	 */
	private void writeShort(ByteArrayOutputStream o, int v) {
		buffer[0] = (byte) (v & 0xff);
		buffer[1] = (byte) ((v >> 8) & 0xff);
		o.write(buffer, 0, 2);
	}

	/**
	 * Writes an int to the given ByteArrayOutputStream.
	 */
	private void writeInt(ByteArrayOutputStream o, int v) {
		buffer[0] = (byte) (v & 0xff);
		buffer[1] = (byte) ((v >> 8) & 0xff);
		buffer[2] = (byte) ((v >> 16) & 0xff);
		buffer[3] = (byte) ((v >> 24) & 0xff);
		o.write(buffer, 0, 4);
	}

	/**
	 * Writes a String to the given ByteArrayOutputStream.
	 */
	private void writeString(ByteArrayOutputStream o, String str) {
		int charCount = str.length() + 1;
		writeInt(o, charCount);

		try {
			o.write(str.getBytes(ArtemisPacket.CHARSET));
		} catch (IOException ex) {
			// ByteArrayOutputStream doesn't actually throw IOException; it
			// just inherits the declaration from the OutputStream class. Since
			// it won't ever actually happen, we wrap it in a RuntimeException.
			throw new RuntimeException(ex);
		}

		writeShort(o, 0);	// terminating null
	}

	/**
	 * Writes a byte array to the given ByteArrayOutputStream.
	 */
	private static void writeBytes(ByteArrayOutputStream o, byte[] bytes) {
		try {
			o.write(bytes);
		} catch (IOException ex) {
			// ByteArrayOutputStream doesn't actually throw IOException; it
			// just inherits the declaration from the OutputStream class. Since
			// it won't ever actually happen, we wrap it in a RuntimeException.
			throw new RuntimeException(ex);
		}
	}
}