package link.infra.jdwp.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ChannelHandler implements PacketCycleConsumer {
	private static final byte[] validHandshakeBytes = "JDWP-Handshake".getBytes(StandardCharsets.UTF_8);

	private final Queue<byte[]> sendQueue = new ArrayDeque<>();
	private final Queue<DeferredResponse> deferredResponseQueue = new ArrayDeque<>();
	private final Map<Integer, ResponseHandler> responseHandlers = new HashMap<>();

	public void handle(PacketCycleConsumer oppositeDirection, RequestHandlerFactory reqHandlerFactory, DataInputStream src, DataOutputStream dest) throws IOException {
		byte[] handshakeBytes = new byte[14];
		src.readFully(handshakeBytes);
		if (!Arrays.equals(handshakeBytes, validHandshakeBytes)) {
			throw new IOException("Invalid handshake!");
		}
		dest.write(validHandshakeBytes);

		// TODO: process
	}

	@Override
	public synchronized void queue(byte[] packetBytes) {
		sendQueue.add(packetBytes);
	}

	@Override
	public synchronized void defer(DeferredResponse res) {
		deferredResponseQueue.add(res);
	}

	@Override
	public synchronized void setResponseHandler(int id, ResponseHandler handler) {
		responseHandlers.put(id, handler);
	}
}
