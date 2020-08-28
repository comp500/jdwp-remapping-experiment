package link.infra.jdwp.packets;

public class DefaultPacketHandler implements RequestHandler, ResponseHandler {
	public static final DefaultPacketHandler INSTANCE = new DefaultPacketHandler();

	@Override
	public void handleRequest(int id, byte flags, byte commandSet, byte command, byte[] data, PacketDispatcher dispatcher) {
		dispatcher.dispatch(SerializationUtil.getRequestBytes(id, flags, commandSet, command, data));
	}

	@Override
	public void handleResponse(int id, byte flags, short errorCode, byte[] data, PacketDispatcher dispatcher) {
		dispatcher.dispatch(SerializationUtil.getResponseBytes(id, flags, errorCode, data));
	}
}
