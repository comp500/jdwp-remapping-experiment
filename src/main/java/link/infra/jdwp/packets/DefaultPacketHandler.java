package link.infra.jdwp.packets;

import java.nio.ByteBuffer;

public class DefaultPacketHandler implements RequestHandler, ResponseHandler {
	public static final DefaultPacketHandler INSTANCE = new DefaultPacketHandler();

	@Override
	public void handleRequest(int id, byte flags, byte commandSet, byte command, byte[] data, PacketCycleConsumer requestDirConsumer, PacketCycleConsumer responseDirConsumer) {
		ByteBuffer res = ByteBuffer.allocate(data.length + 11);
		res.putInt(data.length);
		res.putInt(id);
		res.put(flags);
		res.put(commandSet);
		res.put(command);
		res.put(data);
		requestDirConsumer.queue(res.array());
	}

	@Override
	public void handleResponse(int id, byte flags, short errorCode, byte[] data, PacketCycleConsumer requestDirConsumer, PacketCycleConsumer responseDirConsumer) {
		ByteBuffer res = ByteBuffer.allocate(data.length + 11);
		res.putInt(data.length);
		res.putInt(id);
		res.put(flags);
		res.putShort(errorCode);
		res.put(data);
		responseDirConsumer.queue(res.array());
	}
}
