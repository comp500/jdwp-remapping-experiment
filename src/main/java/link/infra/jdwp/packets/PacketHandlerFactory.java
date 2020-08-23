package link.infra.jdwp.packets;

import link.infra.jdwp.Remapper;
import link.infra.jdwp.TypeSizeManager;
import link.infra.jdwp.packets.referencetype.FieldsWithGeneric;
import link.infra.jdwp.packets.referencetype.MethodsWithGeneric;
import link.infra.jdwp.packets.virtualmachine.AllClassesWithGeneric;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PacketHandlerFactory {
	public final Map<PacketIdentifier, PacketHandler> handlerMap = new HashMap<>();

	public PacketHandlerFactory(Remapper remapper, TypeSizeManager typeSizeManager) {
		handlerMap.put(new PacketIdentifier(1, 20), new AllClassesWithGeneric(remapper, typeSizeManager));

		handlerMap.put(new PacketIdentifier(2, 14), new FieldsWithGeneric(remapper, typeSizeManager));
		handlerMap.put(new PacketIdentifier(2, 15), new MethodsWithGeneric(remapper, typeSizeManager));
	}

	public PacketHandler getHandler(byte commandSet, byte command) {
		PacketHandler handler = handlerMap.get(new PacketIdentifier(commandSet, command));
		if (handler != null) {
			return handler;
		}
		return PacketHandler.DEFAULT;
	}

	private static class PacketIdentifier {
		public final byte commandSet;
		public final byte command;

		public PacketIdentifier(byte commandSet, byte command) {
			this.commandSet = commandSet;
			this.command = command;
		}

		public PacketIdentifier(int commandSet, int command) {
			this.commandSet = (byte) commandSet;
			this.command = (byte) command;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			PacketIdentifier that = (PacketIdentifier) o;
			return commandSet == that.commandSet &&
				command == that.command;
		}

		@Override
		public int hashCode() {
			return Objects.hash(commandSet, command);
		}

		@Override
		public String toString() {
			return "PacketIdentifier{" +
				commandSet + ", " +
				command + '}';
		}
	}

}
