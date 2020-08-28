package link.infra.jdwp.packets;

public interface PacketWriter {
	void queue(byte[] packetBytes);
}
