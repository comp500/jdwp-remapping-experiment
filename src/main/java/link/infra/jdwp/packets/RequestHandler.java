package link.infra.jdwp.packets;

public interface RequestHandler {
	void handleRequest(int id, byte flags, byte commandSet, byte command, byte[] data, PacketDispatcher dispatcher);
}
