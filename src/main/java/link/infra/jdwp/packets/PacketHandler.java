package link.infra.jdwp.packets;

import java.io.IOException;

public interface PacketHandler {
	Untracked DEFAULT = data -> data;

	interface Untracked extends PacketHandler {
		byte[] handleCommand(byte[] data) throws IOException;
	}

	interface Tracked extends PacketHandler {
		byte[] handleCommand(byte[] data) throws IOException;
		byte[] handleReply(byte[] data, short errorCode) throws IOException;
	}

	interface TrackedWithExtra<T> {
		TransformedPacketWithExtra<T> handleCommand(byte[] data) throws IOException;
		byte[] handleReply(byte[] data, short errorCode, T extraData) throws IOException;
	}

	class TransformedPacketWithExtra<T> {
		public final byte[] transformedBytes;
		public final T extraData;

		public TransformedPacketWithExtra(byte[] transformedBytes, T extraData) {
			this.transformedBytes = transformedBytes;
			this.extraData = extraData;
		}
	}
}
