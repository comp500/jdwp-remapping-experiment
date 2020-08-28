package link.infra.jdwp.packets;

import link.infra.jdwp.Remapper;
import link.infra.jdwp.TypeSizeManager;
import link.infra.jdwp.packets.referencetype.FieldsWithGeneric;
import link.infra.jdwp.packets.referencetype.MethodsWithGeneric;
import link.infra.jdwp.packets.virtualmachine.AllClassesWithGeneric;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RequestHandlerFactory {
	private final Map<CommandIdentifier, RequestHandler> handlerMap;

	public RequestHandlerFactory(Remapper remapper, TypeSizeManager typeSizeManager) {
		Map<CommandIdentifier, RequestHandler> handlerMapMut = new HashMap<>();

		// TODO: parse IDSizes

		handlerMapMut.put(new CommandIdentifier(1, 20), new AllClassesWithGeneric(remapper, typeSizeManager));

		handlerMapMut.put(new CommandIdentifier(2, 14), new FieldsWithGeneric(remapper, typeSizeManager));
		handlerMapMut.put(new CommandIdentifier(2, 15), new MethodsWithGeneric(remapper, typeSizeManager));

		handlerMap = Collections.unmodifiableMap(handlerMapMut);
	}

	public RequestHandler getHandler(byte commandSet, byte command) {
		RequestHandler handler = handlerMap.get(new CommandIdentifier(commandSet, command));
		if (handler != null) {
			return handler;
		}
		return DefaultPacketHandler.INSTANCE;
	}

	private static class CommandIdentifier {
		public final byte commandSet;
		public final byte command;

		public CommandIdentifier(byte commandSet, byte command) {
			this.commandSet = commandSet;
			this.command = command;
		}

		public CommandIdentifier(int commandSet, int command) {
			this.commandSet = (byte) commandSet;
			this.command = (byte) command;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CommandIdentifier that = (CommandIdentifier) o;
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
