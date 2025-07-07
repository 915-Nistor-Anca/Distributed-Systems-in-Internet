package org.example.abstractions;

import org.example.communication.CommunicationProtocol;
import org.example.communication.MessageSender;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;

import java.util.Optional;
import java.util.UUID;

public class PerfectLink extends Abstraction {

    public PerfectLink(String abstractionId, Process process) {
        super(abstractionId, process);
    } // abstractionId va fi pl

    //PerfectLink poate:
    // - sa trimita un mesaj unui proces: Message(NetworkMessage(PlSendMessage.Message))
    // - sa trimita un mesaj de la un sender la abstractia parinte: adauga Message(PlDeliverMessage(NetworkMessage.Message))
    //   la coada de mesaje a procesului local
    @Override
    public boolean handle(CommunicationProtocol.Message message) { //message este generic pentru toate abstractiile
        switch (message.getType()) { // verifica tipul mesajului primit
            case PL_SEND: // procesul vrea sa trimita un mesaj prin PerfectLink catre un alt proces
                handlePlSend(message.getPlSend(), message.getToAbstractionId());
                return true;
            case NETWORK_MESSAGE: // un mesaj primit din retea, trimis de alt proces
                triggerPlDeliver(message.getNetworkMessage(), AbstractionIdUtil.getParentAbstractionId(message.getToAbstractionId()));
                return true;
        }
        return false;
    }

    private void handlePlSend(CommunicationProtocol.PlSend plSendMessage, String toAbstractionId) {
        // din structura fisierului.proto, PlSend contine destination (la ce proces sa ajunga mesajul) si message
        //toAbstractionId reprezinta cui ii e destinat mesajul in lantul de abstractii (de ex. app.beb)
        CommunicationProtocol.ProcessId sender = process.getProcess(); // procesul curent - senderul
        CommunicationProtocol.ProcessId destination = plSendMessage.getDestination(); // procesul la care se va trimite mesajul

        // pentru ca mesajul sa fie trimis, se creeaza Message(NetworkMessage(PlSendMessage.Message))
        CommunicationProtocol.NetworkMessage networkMessage = CommunicationProtocol.NetworkMessage
                .newBuilder()
                .setSenderHost(sender.getHost())
                .setSenderListeningPort(sender.getPort())
                .setMessage(plSendMessage.getMessage())
                .build();

        CommunicationProtocol.Message outerMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.NETWORK_MESSAGE)
                .setNetworkMessage(networkMessage)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(toAbstractionId)
                .setSystemId(process.getSystemId())
                .setMessageUuid(UUID.randomUUID().toString())
                .build();

        MessageSender.send(outerMessage, destination.getHost(), destination.getPort());

        /*Din fisierul proto:
        message PlSend {
            ProcessId destination = 1;
            Message message = 2;
        }
        Network-traveling message
        When handling MessageA(PlSend(MessageB)) create MessageC(NetworkMessage(MessageB)), setting:
        MessageC.SystemId = MessageA.SystemId
        MessageC.ToAbstractionId = MessageA.ToAbstractionId
        NetworkMessage.senderHost = N/A (ignore)
        NetworkMessage.senderListeningPort = The your listening port
        * */
    }

    private void triggerPlDeliver(CommunicationProtocol.NetworkMessage networkMessage, String toAbstractionId) {
        // PlDeliver contine sender, adica procesul care a trimis mesajul si message
        // gaseste procesul care a trimis mesajul, folosindu-se de host si port din NetworkMessage
        Optional<CommunicationProtocol.ProcessId> sender = process.getProcessByHostAndPort(networkMessage.getSenderHost(), networkMessage.getSenderListeningPort());
        // se creeaza PLDeliver care contine mesajul primit
        CommunicationProtocol.PlDeliver.Builder plDeliverBuilder = CommunicationProtocol.PlDeliver
                .newBuilder()
                .setMessage(networkMessage.getMessage());
        // daca am gasit procesul care a trimis mesajul, il setam ca sender in PlDeliver
        sender.ifPresent(plDeliverBuilder::setSender);

        CommunicationProtocol.PlDeliver plDeliver = plDeliverBuilder.build();
        // impachetez PlDeliver intr-un mesaj de tip Message la care ii pun tipul PL_DELIVER
        // iis setez toAbstractionId si systemId
        CommunicationProtocol.Message message = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_DELIVER)
                .setPlDeliver(plDeliver)
                .setToAbstractionId(toAbstractionId)
                .setSystemId(process.getSystemId())
                .build();
        // pun mesajul in coada de mesaje a procesului, unde va fi luat de abstractiile superioare
        process.addMessageToQueue(message);
    }
}
