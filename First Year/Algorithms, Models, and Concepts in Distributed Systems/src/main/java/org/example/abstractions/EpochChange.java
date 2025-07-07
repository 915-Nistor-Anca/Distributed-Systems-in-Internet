package org.example.abstractions;

import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;
import org.example.util.ProcessUtil;

public class EpochChange extends Abstraction {

    private CommunicationProtocol.ProcessId trusted;
    private int lastTimestamp;
    private int timestamp;

    public EpochChange(String abstractionId, Process process) {
        super(abstractionId, process);
        trusted = ProcessUtil.getMaxRankedProcess(process.getProcesses()); //procesul considerat de incredere
        lastTimestamp = 0; //cel mai mare timestamp acceptat pana acum de acest proces
        timestamp = process.getProcess().getRank(); //valoare locala propusa de timestamp cand procesul e liderul de incredere

        //inregistreaza subabstractiile
        //detecteaza liderul de incredere
        process.registerAbstraction(new EventualLeaderDetector(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.ELD), process));
        process.registerAbstraction(new BestEffortBroadcast(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.BEB), process));
        process.registerAbstraction(new PerfectLink(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.PL), process));
    }

    @Override
    public boolean handle(CommunicationProtocol.Message message) {
        switch (message.getType()) {
            case ELD_TRUST: //actualizeaza procesul de incredere (liderul)
                handleEldTrust(message.getEldTrust().getProcess());
                return true;
            case BEB_DELIVER: // primeste un mesaj difuzat
                CommunicationProtocol.BebDeliver bebDeliver = message.getBebDeliver();
                //il gestioneaza daca este de tipul EC_INTERNAL_NEW_EPOCH (care semnaleaza inceputul unei epoci noi)
                if (bebDeliver.getMessage().getType() == CommunicationProtocol.Message.Type.EC_INTERNAL_NEW_EPOCH) {
                    handleBebDeliverNewEpoch(bebDeliver.getSender(), bebDeliver.getMessage().getEcInternalNewEpoch().getTimestamp());
                    return true;
                }
                return false;
            case PL_DELIVER: //primeste un mesaj prin PerfectLink
                CommunicationProtocol.PlDeliver plDeliver = message.getPlDeliver();
                //un proces respinge o epoca daca mesajul vine de la un lider care nu mai este de incredere
                //sau timestamp-ul este mai mic decat cel deja cunoscut
                if (plDeliver.getMessage().getType() == CommunicationProtocol.Message.Type.EC_INTERNAL_NACK) {
                    handleNack();
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    //se ocupa de evenimentul ELD_TRUST, adica liderul de incredere s a schimbat
    private void handleEldTrust(CommunicationProtocol.ProcessId p) {
        trusted = p;
        if (p.equals(process.getProcess())) { //daca procesul in sine e liderul
            timestamp += process.getProcesses().size(); //creste pt a propune o noua epoca si pt a se asigura ca timestamp ul propus de el e mai mare decat al oricaruia
            triggerBebBroadcastNewEpoch(); //de fiecare data cand se schimba liderul, se creeaza o epoca noua
        }
    }

    //procesul primeste un mesaj nou de epoca (EC_INTERNAL_NEW_EPOCH) prin BEB
    private void handleBebDeliverNewEpoch(CommunicationProtocol.ProcessId sender, int newTimestamp) {
        if (sender.equals(trusted) && newTimestamp > lastTimestamp) { //daca senderul e liderul si timestampul noii epoci > cel al ultimei
            lastTimestamp = newTimestamp; //actualizeaza noul timestamp
            CommunicationProtocol.EcStartEpoch startEpoch = CommunicationProtocol.EcStartEpoch
                    .newBuilder()
                    .setNewLeader(sender)
                    .setNewTimestamp(newTimestamp)
                    .build();//creeaza un mesaj nou care marcheaza inceputul epocii cu noul lider si timestamp

            CommunicationProtocol.Message startEpochMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.EC_START_EPOCH)
                    .setEcStartEpoch(startEpoch)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                    .setSystemId(process.getSystemId())
                    .build(); //construieste un mesaj EC_START_EPOCH care va fi trimis mai departe in sistem

            process.addMessageToQueue(startEpochMessage);
        } else { //senderul nu e lider sau timestampul e vechi
            CommunicationProtocol.EcInternalNack ecNack = CommunicationProtocol.EcInternalNack
                    .newBuilder()
                    .build();

            CommunicationProtocol.Message nackMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.EC_INTERNAL_NACK)
                    .setEcInternalNack(ecNack)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(this.abstractionId)
                    .setSystemId(process.getSystemId())
                    .build();

            CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend
                    .newBuilder()
                    .setDestination(sender)
                    .setMessage(nackMessage)
                    .build();

            CommunicationProtocol.Message plSendMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.PL_SEND)
                    .setPlSend(plSend)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(AbstractionIdUtil.getChildAbstractionId(this.abstractionId, AbstractionType.PL))
                    .setSystemId(process.getSystemId())
                    .build(); //se creeaza o comanda PL_SEND care trimite mesajul NACK inapoi expeditorului

            process.addMessageToQueue(plSendMessage);
        }
    }

    private void handleNack() {
        if (trusted.equals(process.getProcess())) { //daca procesul e liderul
            timestamp += process.getProcesses().size();
            triggerBebBroadcastNewEpoch(); //creez o noua epoca cu timestamp-ul actualizat
        }
    }

    private void triggerBebBroadcastNewEpoch() {
        CommunicationProtocol.EcInternalNewEpoch newEpoch = CommunicationProtocol.EcInternalNewEpoch
                .newBuilder()
                .setTimestamp(timestamp)
                .build(); //construieste mesajul cu timestampul curent

        CommunicationProtocol.Message newEpochMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EC_INTERNAL_NEW_EPOCH) //tipul mesajului: noua epoca
                .setEcInternalNewEpoch(newEpoch)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId) //isi trimite un mesaj intern ca sa porneasca tranzitia
                .setSystemId(process.getSystemId())
                .build();

        CommunicationProtocol.BebBroadcast bebBroadcast = CommunicationProtocol.BebBroadcast
                .newBuilder()
                .setMessage(newEpochMessage)
                .build();

        CommunicationProtocol.Message bebBroadcastMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_BROADCAST)
                .setBebBroadcast(bebBroadcast)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getChildAbstractionId(this.abstractionId, AbstractionType.BEB))
                .setSystemId(process.getSystemId())
                .build(); //trimite mesaj ca s a schimbat epoca la toate procesele

        process.addMessageToQueue(bebBroadcastMessage);
    }
}
