package link.infra.jdwp.packets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class SerializationUtil {
	public static byte[] getRequestBytes(int id, byte flags, byte commandSet, byte command, byte[] data) {
		ByteBuffer buf = ByteBuffer.allocate(data.length + 11);
		writeHeaderRequest(buf, id, flags, commandSet, command, data.length);
		buf.put(data);
		return buf.array();
	}

	public static void writeHeaderRequest(ByteBuffer buf, int id, byte flags, byte commandSet, byte command, int dataLength) {
		buf.putInt(dataLength + 11);
		buf.putInt(id);
		buf.put(flags);
		buf.put(commandSet);
		buf.put(command);
	}

	public static byte[] getResponseBytes(int id, byte flags, short errorCode, byte[] data) {
		ByteBuffer buf = ByteBuffer.allocate(data.length + 11);
		writeHeaderResponse(buf, id, flags, errorCode, data.length);
		buf.put(data);
		return buf.array();
	}

	public static byte[] getResponseHeaderBytes(int id, byte flags, short errorCode, int dataLength) {
		ByteBuffer buf = ByteBuffer.allocate(11);
		writeHeaderResponse(buf, id, flags, errorCode, dataLength);
		return buf.array();
	}

	public static void writeHeaderResponse(ByteBuffer buf, int id, byte flags, short errorCode, int dataLength) {
		buf.putInt(dataLength + 11);
		buf.putInt(id);
		buf.put(flags);
		buf.putShort(errorCode);
	}

	public static void writeVarInt(long value, int size, DataOutputStream dest) throws IOException {
		if (size > 8 || size < 1) {
			throw new IOException("Unexpected varint size: " + size);
		}
		for (int i = size - 1; i >= 0; i--) {
			dest.writeByte((byte) (value >>> (i * 8)));
		}
	}

	public static long readVarInt(ByteBuffer buf, int size) {
		if (size > 8 || size < 1) {
			// TODO: different exception type?
			throw new RuntimeException("Unexpected varint size: " + size);
		}
		long value = 0;
		for (int i = size - 1; i >= 0; i--) {
			value |= (long)(buf.get() & 0xFF) << (i * 8L);
		}
		return value;
	}

	public static long writeThroughVarInt(ByteBuffer buf, DataOutputStream dest, int size) throws IOException {
		long value = readVarInt(buf, size);
		writeVarInt(value, size, dest);
		return value;
	}

	public static void writeString(DataOutputStream dest, String str) throws IOException {
		byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
		dest.writeInt(bytes.length);
		dest.write(bytes);
	}

	public static String readString(ByteBuffer buf) {
		int len = buf.getInt();
		byte[] stringBuf = new byte[len];
		buf.get(stringBuf);
		return new String(stringBuf, StandardCharsets.UTF_8);
	}
}
