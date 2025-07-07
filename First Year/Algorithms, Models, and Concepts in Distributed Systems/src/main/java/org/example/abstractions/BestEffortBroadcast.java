package org.example.abstractions;


import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;

public class BestEffortBroadcast extends Abstraction {

    public BestEffortBroadcast(String abstractionId, Process process) {
        super(abstractionId, process);
        // creeaza un PerfectLink care se va ocupa de trimiterea mesajelor
        process.registerAbstraction(new PerfectLink(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.PL), process));
    }

    @Override
    public boolean handle(CommunicationProtocol.Message message) {
        switch (message.getType()) { //verifica ce tip de mesaj a primit
            case BEB_BROADCAST: // atunci trimite mesajul la toate procesele prin PerfectLink
                handleBebBroadcast(message.getBebBroadcast());
                return true;
            case PL_DELIVER: // inseamna ca un proces a primit un mesaj, deci trimite mai departe BEB_DELIVER catre aplicatie
                triggerBebDeliver(message.getPlDeliver().getMessage(), message.getPlDeliver().getSender());
                return true;
        }
        return false;
    }

    private void handleBebBroadcast(CommunicationProtocol.BebBroadcast bebBroadcast) {
        process.getProcesses().forEach(p -> { // ia lista cu toate procesele
            CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend
                    .newBuilder()
                    .setDestination(p)
                    .setMessage(bebBroadcast.getMessage())
                    .build();

            CommunicationProtocol.Message plSendMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.PL_SEND)
                    .setPlSend(plSend)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(AbstractionIdUtil.getChildAbstractionId(this.abstractionId, AbstractionType.PL))
                    .setSystemId(process.getSystemId())
                    .build(); // creeaza un mesaj de tipul PL_SEND

            process.addMessageToQueue(plSendMessage);
        });
    }

    private void triggerBebDeliver(CommunicationProtocol.Message appValueMessage, CommunicationProtocol.ProcessId sender) {
        CommunicationProtocol.BebDeliver bebDeliver = CommunicationProtocol.BebDeliver
                .newBuilder()
                .setMessage(appValueMessage)
                .setSender(sender)
                .build();

        CommunicationProtocol.Message bebDeliverMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_DELIVER)
                .setBebDeliver(bebDeliver)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                .setSystemId(process.getSystemId())
                .build();
        //transforma PL_DELIVER in BEB_DELIVER si il da aplicatiei
        process.addMessageToQueue(bebDeliverMessage);
    }
}
