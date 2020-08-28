package link.infra.jdwp.packets;

public interface ResponseHandler {
	void handleResponse(int id, byte flags, short errorCode, byte[] data, PacketDispatcher dispatcher);
}
