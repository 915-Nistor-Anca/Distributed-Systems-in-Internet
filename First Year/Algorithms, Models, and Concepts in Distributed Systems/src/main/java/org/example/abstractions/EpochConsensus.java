package org.example.abstractions;

import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;
import org.example.util.ValueUtil;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/*
Gestioneaza consensul unei epoci.
Fiecare epoca are un lider si un numar de epoca.
Procesele ajung sa decida o valoare prin mai multi pasi:
- propun o valoare
- colecteaza stari de la alte procese
- in final decid valoarea consensuala
* */
public class EpochConsensus extends Abstraction {

    private int ets; //numarul epocii curente
    private CommunicationProtocol.ProcessId leader; //procesul lider pentru aceasta epoca

    private EpState state; //starea locala a procesului (cu valoarea si timestamp-ul asociat)
    private CommunicationProtocol.Value tmpVal; //valoarea temporala propusa pentru consens
    private Map<CommunicationProtocol.ProcessId, EpState> states; //starile de la alte procese
    private int accepted; // numarul de acceptari primite
    private boolean halted; //daca procesul s a oprit sau nu

    public EpochConsensus(String abstractionId, Process process, int ets, CommunicationProtocol.ProcessId leader, EpState state) {
        super(abstractionId, process);
        this.ets = ets;
        this.leader = leader;
        this.state = state;
        this.tmpVal = ValueUtil.buildUndefinedValue();
        this.states = new HashMap<>();
        this.accepted = 0;
        this.halted = false;

        process.registerAbstraction(new BestEffortBroadcast(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.BEB), process));
        process.registerAbstraction(new PerfectLink(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.PL), process));
    }

    @Override
    public boolean handle(CommunicationProtocol.Message message) {
        if (halted) {
            return false;
        }

        switch (message.getType()) {
            case EP_PROPOSE: //cineva propune o valoare de consens
                tmpVal = message.getEpPropose().getValue(); //se preia valoarea din mesaj
                triggerBebBroadcastEpInternalRead(); //lanseaza o cerere de citire a starilor prin beb a celorlalti participanti
                return true;
            case BEB_DELIVER: // primeste un mesaj difuzat
                CommunicationProtocol.BebDeliver bebDeliver = message.getBebDeliver();
                switch (bebDeliver.getMessage().getType()) {
                    case EP_INTERNAL_READ: //raspunde cu starea locala
                        triggerPlSendEpState(bebDeliver.getSender());
                        return true;
                    case EP_INTERNAL_WRITE: //actualizeaza starea locala cu valoarea primita si trimite un mesaj de acceptare catre expeditor
                        state = new EpState(ets, bebDeliver.getMessage().getEpInternalWrite().getValue());
                        triggerPlSendEpAccept(bebDeliver.getSender());
                        return true;
                    case EP_INTERNAL_DECIDED: //daca s a decis o valoare, declanseaza decizia locala
                        triggerEpDecide(bebDeliver.getMessage().getEpInternalDecided().getValue());
                        return true;
                    default:
                        return false;
                }
            case PL_DELIVER: //a primit un mesaj prin perfect link
                CommunicationProtocol.PlDeliver plDeliver = message.getPlDeliver();
                switch (plDeliver.getMessage().getType()) {
                    case EP_INTERNAL_STATE: // primeste starea de la alt proces si o salveaza in states (harta cu starile celorlalti)
                        CommunicationProtocol.EpInternalState deliveredState = plDeliver.getMessage().getEpInternalState();
                        states.put(plDeliver.getSender(), new EpState(deliveredState.getValueTimestamp(), deliveredState.getValue()));
                        performStatesCheck(); //verifica daca are suficiente stari pentru a continua
                        return true;
                    case EP_INTERNAL_ACCEPT: //un alt proces a acceptat valoarea propusa
                        accepted++; //numara cate mesaje de acceptare a primit
                        performAcceptedCheck();
                        return true;
                    default:
                        return false;
                }
                //consensul poate fi oprit pt ca nu s a ajuns la un acord sau s a intamplat o eroare
            case EP_ABORT:
                triggerEpAborted(); // anunta ca consensul a fost anulat
                halted = true; //procesul e oprit
                return true; // mesajul a fost procesat cu succes
            default:
                return false;
        }
    }

    private void triggerBebBroadcastEpInternalRead() {
        CommunicationProtocol.EpInternalRead epRead = CommunicationProtocol.EpInternalRead
                .newBuilder()
                .build();

        CommunicationProtocol.Message epReadMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EP_INTERNAL_READ)
                .setEpInternalRead(epRead)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId)
                .setSystemId(process.getSystemId())
                .build(); //mesaj care cere celorlalte procese sa si trimita starea

        triggerBebBroadcast(epReadMessage);
    }

    //trimite mesajul EP_INTERNAL_STATE la un proces anume (ii comunica starea locala a procesului curent)
    private void triggerPlSendEpState(CommunicationProtocol.ProcessId sender) {
        CommunicationProtocol.EpInternalState epState = CommunicationProtocol.EpInternalState
                .newBuilder()
                .setValueTimestamp(state.getValTimestamp())
                .setValue(state.getVal())
                .build();

        CommunicationProtocol.Message epStateMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EP_INTERNAL_STATE)
                .setEpInternalState(epState)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId)
                .setSystemId(process.getSystemId())
                .build();

        triggerPlSend(epStateMessage, sender);
    }

    //trimite un mesaj de acceptare unui proces anume (a acceptat valoarea pe care a propus o)
    private void triggerPlSendEpAccept(CommunicationProtocol.ProcessId sender) {
        CommunicationProtocol.EpInternalAccept epAccept = CommunicationProtocol.EpInternalAccept
                .newBuilder()
                .build();

        CommunicationProtocol.Message epAcceptMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EP_INTERNAL_ACCEPT)
                .setEpInternalAccept(epAccept)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId)
                .setSystemId(process.getSystemId())
                .build();

        triggerPlSend(epAcceptMessage, sender);
    }

    //verifica daca s au primit suficiente stari de la celelalte procese
    //daca da, selecteaza cea mai inalta stare
    //actualizeaza valoarea propusa temporar
    //trimite un mesaj de tip EP_INTERNAL_WRITE
    private void performStatesCheck() {
        if (states.size() > process.getProcesses().size() / 2) {
            EpState highest = getHighestState();
            if (highest.getVal().getDefined()) {
                tmpVal = highest.getVal();
            }
            states.clear();

            CommunicationProtocol.EpInternalWrite epWrite = CommunicationProtocol.EpInternalWrite
                    .newBuilder()
                    .setValue(tmpVal)
                    .build();

            CommunicationProtocol.Message epWriteMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.EP_INTERNAL_WRITE)
                    .setEpInternalWrite(epWrite)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(this.abstractionId)
                    .setSystemId(process.getSystemId())
                    .build();

            triggerBebBroadcast(epWriteMessage);
        }
    }

    //verifica daca procesul a primit suficiente mesaje de acceptare
    //daca da, inseamna ca majoritatea proceselor au acceptat valoarea propusa si
    // atunci procesul poate decide asupra acelei valori
    private void performAcceptedCheck() {
        if (accepted > process.getProcesses().size() / 2) {
            accepted = 0; //reseteaza controlul pentru urmatoarea runda

            CommunicationProtocol.EpInternalDecided epDecided = CommunicationProtocol.EpInternalDecided
                    .newBuilder()
                    .setValue(tmpVal)
                    .build();

            CommunicationProtocol.Message epDecidedMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.EP_INTERNAL_DECIDED)
                    .setEpInternalDecided(epDecided)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(this.abstractionId)
                    .setSystemId(process.getSystemId())
                    .build();

            triggerBebBroadcast(epDecidedMessage);
        }
    }

    //trimite un mesaj de decizie catre un nivel superior
    private void triggerEpDecide(CommunicationProtocol.Value value) {
        CommunicationProtocol.EpDecide epDecide = CommunicationProtocol.EpDecide
                .newBuilder()
                .setEts(ets) //numarul epocii curente
                .setValue(value) //valorea finala asupra careia s a decis
                .build();

        CommunicationProtocol.Message epDecideMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EP_DECIDE)
                .setEpDecide(epDecide)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(epDecideMessage);
    }

    //semnaleaza ca procesul de decizie a fost intrerupt si trimite mesajul la componenta parinte
    private void triggerEpAborted() {
        CommunicationProtocol.EpAborted epAborted = CommunicationProtocol.EpAborted
                .newBuilder()
                .setEts(ets)
                .setValueTimestamp(state.getValTimestamp())
                .setValue(state.getVal())
                .build();

        CommunicationProtocol.Message epAbortedMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EP_ABORTED)
                .setEpAborted(epAborted)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(epAbortedMessage);
    }

    //trimite un mesaj catre protocolul beb care il va difuza celorlalti membri ai sistemului
    private void triggerBebBroadcast(CommunicationProtocol.Message message) {
        CommunicationProtocol.BebBroadcast bebBroadcast = CommunicationProtocol.BebBroadcast
                .newBuilder()
                .setMessage(message)
                .build();

        CommunicationProtocol.Message bebBroadcastMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.BEB_BROADCAST)
                .setBebBroadcast(bebBroadcast)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getChildAbstractionId(this.abstractionId, AbstractionType.BEB))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(bebBroadcastMessage);
    }

    private void triggerPlSend(CommunicationProtocol.Message message, CommunicationProtocol.ProcessId destination) {
        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend
                .newBuilder()
                .setDestination(destination)
                .setMessage(message)
                .build();

        CommunicationProtocol.Message plSendMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getChildAbstractionId(this.abstractionId, AbstractionType.PL))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(plSendMessage);
    }

    //cauta cea mai recenta stare primita de la alte procese (
    private EpState getHighestState() {
        return states.values().stream()
                .max(Comparator.comparing(EpState::getValTimestamp))
                .orElse(new EpState(0, ValueUtil.buildUndefinedValue()));
    }
}
