package link.infra.jdwp.packets;

import link.infra.jdwp.Remapper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SerializationUtil {
	public static void writeRemapJNITypeSignature(Remapper remapper, DataInputStream src, DataOutputStream dest) throws IOException {
		int stringLen = src.readInt();
		byte[] signatureBuf = new byte[stringLen];
		src.readFully(signatureBuf);
		String mappedSignature = remapper.remapJNITypeSignature(new String(signatureBuf, StandardCharsets.UTF_8));
		byte[] mappedSignatureBytes = mappedSignature.getBytes(StandardCharsets.UTF_8);
		dest.writeInt(mappedSignatureBytes.length);
		dest.write(mappedSignatureBytes);
	}

	public static void writeRemapGenericClassSignature(Remapper remapper, DataInputStream src, DataOutputStream dest) throws IOException {
		int stringLen = src.readInt();
		byte[] signatureBuf = new byte[stringLen];
		src.readFully(signatureBuf);
		String mappedSignature = remapper.remapGenericClassSignature(new String(signatureBuf, StandardCharsets.UTF_8));
		byte[] mappedSignatureBytes = mappedSignature.getBytes(StandardCharsets.UTF_8);
		dest.writeInt(mappedSignatureBytes.length);
		dest.write(mappedSignatureBytes);
	}

	public static void writeRemapMethodName(Remapper remapper, DataInputStream src, DataOutputStream dest) throws IOException {
		int stringLen = src.readInt();
		byte[] nameBuf = new byte[stringLen];
		src.readFully(nameBuf);
		// TODO: track class name?
		String mappedName = remapper.remapMethodName(null, new String(nameBuf, StandardCharsets.UTF_8));
		byte[] mappedNameBytes = mappedName.getBytes(StandardCharsets.UTF_8);
		dest.writeInt(mappedNameBytes.length);
		dest.write(mappedNameBytes);
	}

	public static void writeRemapFieldName(Remapper remapper, DataInputStream src, DataOutputStream dest) throws IOException {
		int stringLen = src.readInt();
		byte[] nameBuf = new byte[stringLen];
		src.readFully(nameBuf);
		// TODO: track class name?
		String mappedName = remapper.remapFieldName(null, new String(nameBuf, StandardCharsets.UTF_8));
		byte[] mappedNameBytes = mappedName.getBytes(StandardCharsets.UTF_8);
		dest.writeInt(mappedNameBytes.length);
		dest.write(mappedNameBytes);
	}
}
