package link.infra.jdwp.packets.virtualmachine;

import link.infra.jdwp.Remapper;
import link.infra.jdwp.TypeSizeManager;
import link.infra.jdwp.packets.PacketHandler;
import link.infra.jdwp.packets.SerializationUtil;

import java.io.*;

public class AllClassesWithGeneric implements PacketHandler.Tracked {
	private final Remapper remapper;
	private final TypeSizeManager typeSizeManager;

	public AllClassesWithGeneric(Remapper remapper, TypeSizeManager typeSizeManager) {
		this.remapper = remapper;
		this.typeSizeManager = typeSizeManager;
	}

	@Override
	public byte[] handleCommand(byte[] data) {
		return data;
	}

	@Override
	public byte[] handleReply(byte[] data, short errorCode) throws IOException {
		DataInputStream src = new DataInputStream(new ByteArrayInputStream(data));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dest = new DataOutputStream(baos);

		int classesCount = src.readInt();
		dest.writeInt(classesCount);
		for (int i = 0; i < classesCount; i++) {
			dest.writeByte(src.readByte());
			// TODO: make this handle IDSizes properly
			dest.writeLong(src.readLong());
			SerializationUtil.writeRemapJNITypeSignature(remapper, src, dest);
			SerializationUtil.writeRemapGenericClassSignature(remapper, src, dest);
			dest.writeInt(src.readInt());
		}
		return baos.toByteArray();
	}
}
