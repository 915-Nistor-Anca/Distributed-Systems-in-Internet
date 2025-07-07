package org.example.abstractions;

import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;
import org.example.util.ProcessUtil;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/*
Detecteaza un lider intr-un sistem distribuit folosind un detector de erori
imperfect dar eventual corect.
Determina procesul lider = procesul care este ales de sistem pentru a lua decizii,
chiar daca alte procese pot cadea.
Actualizeaza liderul in timp, daca procesele suspectate se schimba.
* */
public class EventualLeaderDetector extends Abstraction {

    //procesele suspectate ca fiind cazute
    private Set<CommunicationProtocol.ProcessId> suspected;
    //liderul curent
    private CommunicationProtocol.ProcessId leader;

    public EventualLeaderDetector(String abstractionId, Process process) {
        super(abstractionId, process);
        suspected = new CopyOnWriteArraySet<>();

        //inregistreaza un EventuallyPerfectFailureDetector ca subabstractie, care trimite EPFD_SUSPECT si EPFD_RESTORE
        process.registerAbstraction(new EventuallyPerfectFailureDetector(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.EPFD), process));
    }

    @Override
    public boolean handle(CommunicationProtocol.Message message) {
        switch (message.getType()) {
            case EPFD_SUSPECT:
                //daca un proces este suspectat ca a picat, se adauga in suspected
                suspected.add(message.getEpfdSuspect().getProcess());
                performCheck();
                return true;
            case EPFD_RESTORE:
                // in cazul in care procesul revine, se elimina din suspected
                suspected.remove(message.getEpfdSuspect().getProcess());
                performCheck();
                return true;
            default:
                return false;
        }
    }

    //verifica si stabileste liderul
    /*
    - ia toate procesele din sistem si elimina procesele suspectate
    - din procesele ramase, alege procesul cu cel mai mare rank ca lider

    * */
    private void performCheck() {
        Set<CommunicationProtocol.ProcessId> notSuspected = new CopyOnWriteArraySet<>(process.getProcesses());
        notSuspected.removeAll(suspected); //elimina procesele suspectate
        CommunicationProtocol.ProcessId maxRankedProcess = ProcessUtil.getMaxRankedProcess(notSuspected); //alege procesul cu cel mai mare rank
        if (maxRankedProcess != null && !maxRankedProcess.equals(leader)) { //verifica daca nu e null si e diferit de liderul actual
            leader = maxRankedProcess; //actualizeaza liderul local cu noul lider selectat

            CommunicationProtocol.EldTrust eldTrust = CommunicationProtocol.EldTrust
                    .newBuilder()
                    .setProcess(leader)
                    .build();

            CommunicationProtocol.Message trustMessage = CommunicationProtocol.Message
                    .newBuilder()
                    .setType(CommunicationProtocol.Message.Type.ELD_TRUST)
                    .setEldTrust(eldTrust)
                    .setFromAbstractionId(this.abstractionId)
                    .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                    .setSystemId(process.getSystemId())
                    .build(); //construiesc un mesaj de tip ELD_TRUST care contine noul lider

            process.addMessageToQueue(trustMessage);
        }
    }
}
