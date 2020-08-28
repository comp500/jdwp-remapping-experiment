package link.infra.jdwp.packets;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class InputChannelHandler implements PacketReader {
	private static final byte[] validHandshakeBytes = "JDWP-Handshake".getBytes(StandardCharsets.UTF_8);

	private final List<PacketReader.DeferHandler> deferHandlers = new ArrayList<>();
	private final Map<Integer, ResponseHandler> responseHandlers = new HashMap<>();

	@SuppressWarnings("InfiniteLoopStatement")
	public void handle(PacketDispatcher dispatcher, DataInputStream src, RequestHandlerFactory requestHandlerFactory) throws IOException {
		byte[] handshakeBytes = new byte[14];
		src.readFully(handshakeBytes);
		if (!Arrays.equals(handshakeBytes, validHandshakeBytes)) {
			throw new IOException("Invalid handshake!");
		}
		dispatcher.dispatch(validHandshakeBytes);

		Deque<DeferHandler> deferHandlersCopy = new ArrayDeque<>();
		Deque<DeferHandler> deferHandlersToRemove = new ArrayDeque<>();
		while (true) {
			// Attempt to read a packet
			int length = src.readInt();
			int id = src.readInt();
			byte flags = src.readByte();
			// 0x80 bit indicates reply
			if ((flags & 0x80) != 0) {
				short errorCode = src.readShort();
				System.out.println(Thread.currentThread().getName() + " reply packet " + id + " (err " + errorCode + ")");
				byte[] data = new byte[length - 11];
				src.readFully(data);
				ResponseHandler handler;
				synchronized (responseHandlers) {
					handler = responseHandlers.remove(id);
				}
				if (handler == null) {
					handler = DefaultPacketHandler.INSTANCE;
				}
				handler.handleResponse(id, flags, errorCode, data, dispatcher);
			} else {
				byte commandSet = src.readByte();
				byte command = src.readByte();
				System.out.println(Thread.currentThread().getName() + " command packet " + id + " (set " + commandSet + " cmd " + command + ")");
				byte[] data = new byte[length - 11];
				src.readFully(data);
				RequestHandler handler = requestHandlerFactory.getHandler(commandSet, command);
				if (handler == null) {
					handler = DefaultPacketHandler.INSTANCE;
				}
				handler.handleRequest(id, flags, commandSet, command, data, dispatcher);
			}

			// Process defer handlers
			synchronized (deferHandlers) {
				deferHandlersCopy.addAll(deferHandlers);
			}
			while (!deferHandlersCopy.isEmpty()) {
				DeferHandler handler = deferHandlersCopy.pop();
				if (handler.process()) {
					deferHandlersToRemove.push(handler);
				}
			}
			synchronized (deferHandlers) {
				deferHandlers.removeAll(deferHandlersToRemove);
			}
		}
	}

	@Override
	public void defer(DeferHandler handler) {
		synchronized (deferHandlers) {
			deferHandlers.add(handler);
		}
	}

	@Override
	public void setResponseHandler(int id, ResponseHandler handler) {
		synchronized (responseHandlers) {
			responseHandlers.put(id, handler);
		}
	}
}
