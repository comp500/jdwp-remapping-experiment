package link.infra.jdwp.packets.virtualmachine;

import link.infra.jdwp.Remapper;
import link.infra.jdwp.TypeSizeManager;
import link.infra.jdwp.packets.PacketDispatcher;
import link.infra.jdwp.packets.RequestHandler;
import link.infra.jdwp.packets.ResponseHandler;
import link.infra.jdwp.packets.SerializationUtil;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AllClassesWithGeneric implements RequestHandler, ResponseHandler {
	private final Remapper remapper;
	private final TypeSizeManager typeSizeManager;

	public AllClassesWithGeneric(Remapper remapper, TypeSizeManager typeSizeManager) {
		this.remapper = remapper;
		this.typeSizeManager = typeSizeManager;
	}

	@Override
	public void handleRequest(int id, byte flags, byte commandSet, byte command, byte[] data, PacketDispatcher dispatcher) {
		dispatcher.dispatch(SerializationUtil.getRequestBytes(id, flags, commandSet, command, data));
		dispatcher.setResponseHandler(id, this);
	}

	@Override
	public void handleResponse(int id, byte flags, short errorCode, byte[] data, PacketDispatcher dispatcher) {
		ByteBuffer src = ByteBuffer.wrap(data);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dest = new DataOutputStream(baos);

		try {
			int classesCount = src.getInt();
			dest.writeInt(classesCount);
			for (int i = 0; i < classesCount; i++) {
				dest.writeByte(src.get());
				// TODO: store reference type information
				SerializationUtil.writeThroughVarInt(src, dest, typeSizeManager.getReferenceTypeID());
				SerializationUtil.writeString(dest, remapper.remapJNITypeSignature(SerializationUtil.readString(src)));
				SerializationUtil.writeString(dest, remapper.remapGenericClassSignature(SerializationUtil.readString(src)));
				dest.writeInt(src.getInt());
			}

			byte[] newData = baos.toByteArray();
			dispatcher.dispatch(SerializationUtil.getResponseHeaderBytes(id, flags, errorCode, newData.length));
			dispatcher.dispatch(newData);
		} catch (IOException e) {
			// TODO: different exception type? logger?
			throw new RuntimeException(e);
		}
	}
}
