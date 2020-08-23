package link.infra.jdwp;

import link.infra.jdwp.packets.PacketHandler;
import link.infra.jdwp.packets.PacketHandlerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class ConnectionHandler {
	private static final int port = 5005;
	private static final byte[] validHandshakeBytes = "JDWP-Handshake".getBytes(StandardCharsets.UTF_8);
	private final Remapper remapper;

	public ConnectionHandler(Socket clientSocket, Remapper remapper) throws IOException {
		this.remapper = remapper;

		// Make connection to destination
		try (Socket serverSocket = new Socket("127.0.0.1", port)) {
			new Thread(() -> {
				try (InputStream serverIn = serverSocket.getInputStream(); OutputStream clientOut = clientSocket.getOutputStream()) {
					new ProxyHandler(new DataInputStream(serverIn), new DataOutputStream(clientOut));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}, "jdwp experiment socket handler s2c").start();

			try (InputStream clientIn = clientSocket.getInputStream(); OutputStream serverOut = serverSocket.getOutputStream()) {
				new ProxyHandler(new DataInputStream(clientIn), new DataOutputStream(serverOut));
			}
		}
	}

	private interface ReplyTrackedData {
		byte[] handleReply(byte[] data, short errorCode) throws IOException;
	}

	private static class ReplyTrackedDataWithExtra<T> implements ReplyTrackedData {
		private final PacketHandler.TrackedWithExtra<T> replyHandler;
		public final T extraData;

		private ReplyTrackedDataWithExtra(PacketHandler.TrackedWithExtra<T> replyHandler, T extraData) {
			this.replyHandler = replyHandler;
			this.extraData = extraData;
		}

		public byte[] handleReply(byte[] data, short errorCode) throws IOException {
			return replyHandler.handleReply(data, errorCode, extraData);
		}
	}

	private static class ReplyTrackedDataSimple implements ReplyTrackedData {
		private final PacketHandler.Tracked replyHandler;

		private ReplyTrackedDataSimple(PacketHandler.Tracked replyHandler) {
			this.replyHandler = replyHandler;
		}

		public byte[] handleReply(byte[] data, short errorCode) throws IOException {
			return replyHandler.handleReply(data, errorCode);
		}
	}

	private final HashMap<Integer, ReplyTrackedData> trackedPackets = new HashMap<>();

	private class ProxyHandler {

		private ProxyHandler(DataInputStream src, DataOutputStream dest) throws IOException {
			// TODO: share between threads
			PacketHandlerFactory handlerFactory = new PacketHandlerFactory(remapper, new TypeSizeManager());

			byte[] handshakeBytes = new byte[14];
			src.readFully(handshakeBytes);
			if (!Arrays.equals(handshakeBytes, validHandshakeBytes)) {
				throw new IOException("Invalid handshake!");
			}
			dest.write(validHandshakeBytes);

			// Read packets
			byte[] buf = new byte[4096];
			while (true) {
				int length = src.readInt();
				int id = src.readInt();
				byte flags = src.readByte();
				// 0x80 bit indicates reply
				if ((flags & 0x80) != 0) {
					short errorCode = src.readShort();
					System.out.println(Thread.currentThread().getName() + " reply packet " + id + " (err " + errorCode + ")");
					ReplyTrackedData trackedData;
					synchronized (trackedPackets) {
						trackedData = trackedPackets.remove(id);
					}
					if (trackedData == null) {
						dest.writeInt(length);
						dest.writeInt(id);
						dest.writeByte(flags);
						dest.writeShort(errorCode);
						// Pass data through
						int remaining = length - 11;
						while (remaining > 0) {
							int lengthRead = src.read(buf, 0, Math.min(remaining, buf.length));
							if (lengthRead < 0) {
								throw new EOFException();
							}
							dest.write(buf, 0, lengthRead);
							remaining -= lengthRead;
						}
					} else {
						byte[] data = new byte[length - 11];
						src.readFully(data);
						byte[] result = trackedData.handleReply(data, errorCode);
						dest.writeInt(result.length + 11);
						dest.writeInt(id);
						dest.writeByte(flags);
						dest.writeShort(errorCode);
						dest.write(result);
					}
				} else {
					byte commandSet = src.readByte();
					byte command = src.readByte();
					PacketHandler handler = handlerFactory.getHandler(commandSet, command);
					System.out.println(Thread.currentThread().getName() + " command packet " + id + " (set " + commandSet + " cmd " + command + ")");
					byte[] data = new byte[length - 11];
					src.readFully(data);
					byte[] result;
					if (handler instanceof PacketHandler.TrackedWithExtra<?>) {
						result = handleTrackedExtra(data, (PacketHandler.TrackedWithExtra<?>) handler, id);
					} else if (handler instanceof PacketHandler.Tracked) {
						result = ((PacketHandler.Tracked) handler).handleCommand(data);
						synchronized (trackedPackets) {
							trackedPackets.put(id, new ReplyTrackedDataSimple((PacketHandler.Tracked) handler));
						}
					} else if (handler instanceof PacketHandler.Untracked) {
						result = ((PacketHandler.Untracked) handler).handleCommand(data);
					} else {
						throw new RuntimeException("Java doesn't have sealed classes :(");
					}
					dest.writeInt(result.length + 11);
					dest.writeInt(id);
					dest.writeByte(flags);
					dest.writeByte(commandSet);
					dest.writeByte(command);
					dest.write(result);
				}
			}
		}
	}

	// Extracted from ProxyHandler#<init> to handle generics safely
	private <T> byte[] handleTrackedExtra(byte[] data, PacketHandler.TrackedWithExtra<T> handler, int id) throws IOException {
		PacketHandler.TransformedPacketWithExtra<T> resWithExtra = handler.handleCommand(data);
		synchronized (trackedPackets) {
			trackedPackets.put(id, new ReplyTrackedDataWithExtra<>(handler, resWithExtra.extraData));
		}
		return resWithExtra.transformedBytes;
	}
}
