package link.infra.jdwp.packets.referencetype;

import link.infra.jdwp.Remapper;
import link.infra.jdwp.TypeSizeManager;
import link.infra.jdwp.packets.PacketHandler;
import link.infra.jdwp.packets.SerializationUtil;

import java.io.*;

public class MethodsWithGeneric implements PacketHandler.Tracked {
	private final Remapper remapper;
	private final TypeSizeManager typeSizeManager;

	public MethodsWithGeneric(Remapper remapper, TypeSizeManager typeSizeManager) {
		this.remapper = remapper;
		this.typeSizeManager = typeSizeManager;
	}

	@Override
	public byte[] handleCommand(byte[] data) throws IOException {
		return data;
	}

	@Override
	public byte[] handleReply(byte[] data, short errorCode) throws IOException {
		DataInputStream src = new DataInputStream(new ByteArrayInputStream(data));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dest = new DataOutputStream(baos);

		int methodsCount = src.readInt();
		dest.writeInt(methodsCount);
		for (int i = 0; i < methodsCount; i++) {
			// TODO: make this handle IDSizes properly
			dest.writeLong(src.readLong());
			SerializationUtil.writeRemapMethodName(remapper, src, dest);
			SerializationUtil.writeRemapJNITypeSignature(remapper, src, dest);
			// TODO: handle method sigs as well
			SerializationUtil.writeRemapGenericClassSignature(remapper, src, dest);
			dest.writeInt(src.readInt());
		}
		return baos.toByteArray();
	}
}
