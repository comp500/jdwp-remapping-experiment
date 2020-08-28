package link.infra.jdwp.packets.referencetype;

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

public class MethodsWithGeneric implements RequestHandler {
	private final Remapper remapper;
	private final TypeSizeManager typeSizeManager;

	public MethodsWithGeneric(Remapper remapper, TypeSizeManager typeSizeManager) {
		this.remapper = remapper;
		this.typeSizeManager = typeSizeManager;
	}

	@Override
	public void handleRequest(int id, byte flags, byte commandSet, byte command, byte[] data, PacketDispatcher dispatcher) {
		dispatcher.dispatch(SerializationUtil.getRequestBytes(id, flags, commandSet, command, data));
		dispatcher.setResponseHandler(id, new Response(SerializationUtil.readVarInt(ByteBuffer.wrap(data), typeSizeManager.getReferenceTypeID())));
	}

	private class Response implements ResponseHandler {
		private final long referenceTypeID;

		public Response(long referenceTypeID) {
			this.referenceTypeID = referenceTypeID;
		}

		@Override
		public void handleResponse(int id, byte flags, short errorCode, byte[] data, PacketDispatcher dispatcher) {
			ByteBuffer src = ByteBuffer.wrap(data);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dest = new DataOutputStream(baos);

			try {
				int methodsCount = src.getInt();
				dest.writeInt(methodsCount);
				for (int i = 0; i < methodsCount; i++) {
					// TODO: store field information
					SerializationUtil.writeThroughVarInt(src, dest, typeSizeManager.getFieldID());
					// TODO: use cached reference type information (or defer if necessary)
					SerializationUtil.writeString(dest, remapper.remapMethodName(null, SerializationUtil.readString(src)));
					SerializationUtil.writeString(dest, remapper.remapJNITypeSignature(SerializationUtil.readString(src)));
					// TODO: should this be method signature?
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
}
