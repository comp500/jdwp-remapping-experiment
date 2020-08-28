package link.infra.jdwp;

import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static TinyTree load() throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\comp500\\.gradle\\caches\\fabric-loom\\mappings\\yarn-1.16.1+build.20-v2.tiny"))) {
			return TinyMappingFactory.loadWithDetection(reader);
		}
	}

	public Server() throws IOException {
		ServerSocket server = new ServerSocket(10000);
		while (true) {
			Socket clientSocket = server.accept();
			try {
				new ConnectionHandler(clientSocket, new TinyTreeRemapper(load()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			// TODO: make sure socket is properly closed
		}
	}
}
