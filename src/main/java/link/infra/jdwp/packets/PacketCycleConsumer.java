package link.infra.jdwp.packets;

public interface PacketCycleConsumer {
	void queue(byte[] packetBytes);
	void defer(DeferredResponse res);
	void setResponseHandler(int id, ResponseHandler handler);
}
