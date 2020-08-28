package link.infra.jdwp.packets;

/**
 * Abstracted holder of PacketReaders and PacketWriters for packet handlers to use.
 */
public class PacketDispatcher {
	private final PacketReader reader;
	private final PacketWriter writer;
	private final PacketReader readerOpposite;
	private final PacketWriter writerOpposite;

	/**
	 * Constructs a PacketDispatcher.
	 * @param reader The reader in the direction of this packet
	 * @param writer The writer in the direction of this packet
	 * @param readerOpposite The reader in the opposite direction to this packet
	 * @param writerOpposite The writer in the opposite direction to this packet
	 */
	public PacketDispatcher(PacketReader reader, PacketWriter writer, PacketReader readerOpposite, PacketWriter writerOpposite) {
		this.reader = reader;
		this.writer = writer;
		this.readerOpposite = readerOpposite;
		this.writerOpposite = writerOpposite;
	}

	public PacketDispatcher reverse() {
		return new PacketDispatcher(readerOpposite, writerOpposite, reader, writer);
	}

	/**
	 * Write packets to the writer operating in this direction
	 * @param packetBytes Bytes of the packet to send
	 */
	public void dispatch(byte[] packetBytes) {
		writer.queue(packetBytes);
	}

	/**
	 * Write packets to the writer operating in the opposite direction
	 * @param packetBytes Bytes of the packet to send
	 */
	public void dispatchOpposite(byte[] packetBytes) {
		writerOpposite.queue(packetBytes);
	}

	/**
	 * Call deferHandler when packets are received on the reader operating in this direction
	 * @param deferHandler The defer handler to call
	 */
	public void defer(PacketReader.DeferHandler deferHandler) {
		reader.defer(deferHandler);
	}

	/**
	 * Call deferHandler when packets are received on the reader operating in the opposite direction
	 * @param deferHandler The defer handler to call
	 */
	public void deferOpposite(PacketReader.DeferHandler deferHandler) {
		readerOpposite.defer(deferHandler);
	}

	/**
	 * Add a response handler for the opposite direction
	 * @param id The packet ID to call the handler on
	 * @param handler The handler to call
	 */
	public void setResponseHandler(int id, ResponseHandler handler) {
		readerOpposite.setResponseHandler(id, handler);
	}

	/**
	 * Add a response handler for the current direction
	 * @param id The packet ID to call the handler on
	 * @param handler The handler to call
	 */
	public void setResponseHandlerSameDirection(int id, ResponseHandler handler) {
		reader.setResponseHandler(id, handler);
	}
}
