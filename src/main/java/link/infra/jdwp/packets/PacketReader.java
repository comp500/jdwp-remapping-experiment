package link.infra.jdwp.packets;

public interface PacketReader {
	/**
	 * Call the given handler after a packet has been read by this packet reader.
	 * If the handler returns true continue calling it after further packet reads, otherwise don't call this again.
	 * @param handler The handler to defer until more packets have been read
	 */
	void defer(DeferHandler handler);
	void setResponseHandler(int id, ResponseHandler handler);

	interface DeferHandler {
		/**
		 * Process additional data that may have been received by response handlers, and determine if more data is necessary.
		 * @return False if the handler should be called again, true if it should be removed
		 */
		boolean process();
	}
}
