package link.infra.jdwp;

import link.infra.jdwp.packets.InputChannelHandler;
import link.infra.jdwp.packets.OutputChannelHandler;
import link.infra.jdwp.packets.PacketDispatcher;
import link.infra.jdwp.packets.RequestHandlerFactory;

import java.io.*;
import java.net.Socket;

public class ConnectionHandler {
	private static final int port = 5005;

	public ConnectionHandler(Socket clientSocket, Remapper remapper) throws IOException {
		InputChannelHandler inputHandlerC2S = new InputChannelHandler();
		OutputChannelHandler outputHandlerC2S = new OutputChannelHandler();
		InputChannelHandler inputHandlerS2C = new InputChannelHandler();
		OutputChannelHandler outputHandlerS2C = new OutputChannelHandler();
		PacketDispatcher dispatcherC2S = new PacketDispatcher(inputHandlerC2S, outputHandlerC2S, inputHandlerS2C, outputHandlerS2C);
		PacketDispatcher dispatcherS2C = dispatcherC2S.reverse();
		RequestHandlerFactory requestHandlerFactory = new RequestHandlerFactory(remapper, new TypeSizeManager());

		// Make connection to destination
		try (Socket serverSocket = new Socket("127.0.0.1", port)) {
			try (InputStream serverIn = serverSocket.getInputStream(); OutputStream clientOut = clientSocket.getOutputStream();
				 InputStream clientIn = clientSocket.getInputStream(); OutputStream serverOut = serverSocket.getOutputStream()) {
				new Thread(() -> {
					try {
						inputHandlerS2C.handle(dispatcherS2C, new DataInputStream(serverIn), requestHandlerFactory);
					} catch (IOException e) {
						// TODO: handle this better
						e.printStackTrace();
					}
				}, "jdwp experiment socket handler s2c input").start();
				new Thread(() -> {
					try {
						outputHandlerS2C.handle(new DataOutputStream(clientOut));
					} catch (IOException | InterruptedException e) {
						// TODO: handle this better
						e.printStackTrace();
					}
				}, "jdwp experiment socket handler s2c output").start();
				new Thread(() -> {
					try {
						inputHandlerC2S.handle(dispatcherC2S, new DataInputStream(clientIn), requestHandlerFactory);
					} catch (IOException e) {
						// TODO: handle this better
						e.printStackTrace();
					}
				}, "jdwp experiment socket handler c2s input").start();
				try {
					outputHandlerC2S.handle(new DataOutputStream(serverOut));
				} catch (IOException | InterruptedException e) {
					// TODO: handle this better
					e.printStackTrace();
				}
			}
		}
		// TODO: clean up closing
	}
}
