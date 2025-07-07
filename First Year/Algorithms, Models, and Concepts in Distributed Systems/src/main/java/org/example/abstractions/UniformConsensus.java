package org.example.abstractions;


import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;
import org.example.util.ProcessUtil;
import org.example.util.ValueUtil;
/*Garanteaza ca:
- toate procesele decid aceeasi valoare
- o valoare este decisa o singura data
- chiar daca liderul se schimba, consensul ramane corect si unitar
* */

/*
- uc creeaza epoch consensus cu, liderul reprezentand procesul cu rankul cel mai mare
- in epoch consensus, liderul face broadcast cu internal read
* */
public class UniformConsensus extends Abstraction {
    private CommunicationProtocol.Value val; //valoarea propusa de proces
    private boolean proposed; //daca procesul a propus deja o valoare in epoca curenta
    private boolean decided; //daca procesul a decis deja o valoare
    private int ets; //numarul epocii curente
    private CommunicationProtocol.ProcessId leader; //liderul epocii curente
    private int newts; // noua epoca care urmeaza sa fie activata
    private CommunicationProtocol.ProcessId newLeader; //noul lider

    public UniformConsensus(String abstractionId, Process process) {
        super(abstractionId, process);

        val = ValueUtil.buildUndefinedValue();
        proposed = false;
        decided = false;

        CommunicationProtocol.ProcessId leader0 = ProcessUtil.getMaxRankedProcess(process.getProcesses()); //se alege liderul initial
        ets = 0;
        leader = leader0;
        newts = 0;
        newLeader = null;

        //inregistreaza subabstractiile
        //schimba epoca (reinitializeaza procesul de consens cu un alt lider)
        process.registerAbstraction(new EpochChange(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.EC), process));
        //realizeaza consensul efectiv in cadrul unei epoci
        process.registerAbstraction(new EpochConsensus(AbstractionIdUtil.getNamedAbstractionId(abstractionId, AbstractionType.EP, "0"), process,
                0, leader0, new EpState(0, ValueUtil.buildUndefinedValue())));
    }

    @Override
    public boolean handle(CommunicationProtocol.Message message) {
        switch (message.getType()) {
            case UC_PROPOSE: //procesul vrea sa faca consens pe o valoare
                val = message.getUcPropose().getValue();
                performCheck();
                return true;
            case EC_START_EPOCH: //cand moare liderul, se trece la o epoca noua
                newts = message.getEcStartEpoch().getNewTimestamp();
                newLeader = message.getEcStartEpoch().getNewLeader();
                triggerEpEtsAbort();
                return true;
            case EP_ABORTED: // epoca curenta s-a incheiat fara consens
                if (message.getEpAborted().getEts() == ets) { //se verifica ca mesajul EP_ABORTED vine din epoca curenta
                    ets = newts; //trece la epoca noua
                    leader = newLeader; //seteaza noul lider
                    proposed = false; //nu a propus inca nimic

                    // se inregistreaza un nou epoch consensus pentru noua epoca cu noul lider
                    process.registerAbstraction(new EpochConsensus(AbstractionIdUtil.getNamedAbstractionId(abstractionId, AbstractionType.EP, Integer.toString(ets)), process,
                            ets, leader, new EpState(message.getEpAborted().getValueTimestamp(), message.getEpAborted().getValue())));
                    performCheck();
                    return true;
                }
                return false;
            case EP_DECIDE: //s-a ajuns la consens in epoca curenta si trimite decizia mai departe catre parinte
                if (message.getEpDecide().getEts() == ets) {
                    if (!decided) {
                        decided = true;
                        triggerUcDecide(message.getEpDecide().getValue());
                    }
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    //cand un lider e detectat ca mort, trimite mesaj de EP_ABORT catre EpochConsensus, pentru a incheia epoca curenta
    private void triggerEpEtsAbort() {
        CommunicationProtocol.EpAbort epAbort = CommunicationProtocol.EpAbort
                .newBuilder()
                .build();

        CommunicationProtocol.Message epAbortMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EP_ABORT)
                .setEpAbort(epAbort)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getNamedAbstractionId(this.abstractionId, AbstractionType.EP, Integer.toString(ets)))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(epAbortMessage);
    }

    private void performCheck() {
        if (leader.equals(process.getProcess()) && val.getDefined() && !proposed) {
            proposed = true;

            CommunicationProtocol.EpPropose epPropose = CommunicationProtocol.EpPropose
                    .newBuilder()
                    .setValue(val)
                    .build();

            CommunicationProtocol.Message epProposeMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.EP_PROPOSE)
                    .setEpPropose(epPropose)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(AbstractionIdUtil.getNamedAbstractionId(this.abstractionId, AbstractionType.EP, Integer.toString(ets)))
                    .setSystemId(process.getSystemId())
                    .build();

            process.addMessageToQueue(epProposeMessage);
        }
    }

    //cand se ajunge la o decizie, trimite un mesaj la UC_DECIDE catre abstractia parinte,
    // indicand ca aceasta valoare a fost decisa in mod uniform si definitiv
    private void triggerUcDecide(CommunicationProtocol.Value value) {
        CommunicationProtocol.UcDecide ucDecide = CommunicationProtocol.UcDecide
                .newBuilder()
                .setValue(value)
                .build();

        CommunicationProtocol.Message ucDecideMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.UC_DECIDE)
                .setUcDecide(ucDecide)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(ucDecideMessage);
    }
}
