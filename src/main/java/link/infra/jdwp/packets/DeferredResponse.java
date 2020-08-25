package link.infra.jdwp.packets;

public interface DeferredResponse {
	boolean attemptResponse(PacketCycleConsumer requestDirConsumer, PacketCycleConsumer responseDirConsumer);
}
