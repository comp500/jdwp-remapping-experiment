package link.infra.jdwp.packets;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class OutputChannelHandler implements PacketWriter {
	private final BlockingQueue<byte[]> sendQueue = new LinkedBlockingDeque<>();

	@SuppressWarnings("InfiniteLoopStatement")
	public void handle(DataOutputStream dest) throws IOException, InterruptedException {
		while (true) {
			byte[] data = sendQueue.take();
			dest.write(data);
		}
		// TODO: make sure closing of one stream closes all
	}

	@Override
	public void queue(byte[] packetBytes) {
		try {
			sendQueue.put(packetBytes);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
