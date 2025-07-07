package org.example.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

public class MessageReceiver extends Observable implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MessageReceiver.class);

    private final int processPort; //portul pe care procesul va asculta conexiuni de la alte procese

    public MessageReceiver(int processPort) {
        this.processPort = processPort;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(processPort)) {
            log.info("Port {} is waiting for requests.", processPort);
            while (true) {
                receiveMessage(serverSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void receiveMessage(ServerSocket serverSocket) throws IOException {
        try (Socket clientSocket = serverSocket.accept(); //asteapta o conexiune
             DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream()); //primeste marimea mesajului in bytes
        ) {
            int messageSize = dataInputStream.readInt();
            byte[] byteBuffer = new byte[messageSize];
            int readMessageSize = dataInputStream.read(byteBuffer, 0, messageSize); //citeste continutul

            if (messageSize != readMessageSize) {
                throw new RuntimeException("Network message has incorrect size: expected = " + messageSize + ", actual = " + readMessageSize);
            }

            CommunicationProtocol.Message message = CommunicationProtocol.Message.parseFrom(byteBuffer); //decodeaza mesajul

            if (!CommunicationProtocol.Message.Type.NETWORK_MESSAGE.equals(message.getType())) { //se accepta doar mesaje care vin de la alte procese
                throw new RuntimeException("Network message has incorrect type: expected = " + CommunicationProtocol.Message.Type.NETWORK_MESSAGE + ", actual = " + message.getType());
            }

            log.info("Received {} from {}", message.getNetworkMessage().getMessage().getType(), message.getNetworkMessage().getSenderListeningPort());

            setChanged();
            notifyObservers(message);
        }
    }
}
