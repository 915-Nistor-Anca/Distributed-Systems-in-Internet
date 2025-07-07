package org.example.abstractions;

import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

/*
- detector de cadere care e folosit pentru a detecta daca alte procese au cazut
- trimite periodic mesaje de viata catre alte procese
- daca nu primeste raspuns de la un proces, il suspecteaza ca a cazut
- daca primeste raspuns de la un proces anterior suspectat, nu il mai suspecteaza
- isi creste gradual timpul de timeout daca detecteaza incertitudine
(daca mai primeste heartbeat-uri de la unii dupa delay, inseamna ca reteaua nu a cazut,
doar e lenta, deci va trebui sa creasca timpul de asteptare)
* */
public class EventuallyPerfectFailureDetector extends Abstraction {

    private static final int DELTA = 100; //deplay de 100ms
    private Set<CommunicationProtocol.ProcessId> alive; // procesele de la care a primit heartbeat reply in runda curenta
    private Set<CommunicationProtocol.ProcessId> suspected; //procesele pe care le suspecteaza ca au cazut
    private int delay;

    public EventuallyPerfectFailureDetector(String abstractionId, Process process) {
        super(abstractionId, process);
        alive = new CopyOnWriteArraySet<>(process.getProcesses()); //creeaza o lista de procese pe care le considera vii la inceput
        suspected = new CopyOnWriteArraySet<>(); //gol la inceput
        delay = DELTA;
        /*
        - porneste un timer care trimite mesaj intern EPFD_TIMEOUT dupa delay ms
        - mesajul va fi tratat de metoda handleEpfdTimeout(), care verifica cine a dat si cine nu a dat reply
        * */
        startTimer(delay);

        /*
        - inregistrez o abstractie PerfectLink (pentrua a putea trimite si primi mesaje fara pierderi)
        - epfd trebuie sa trimita si sa primeasca heartbeat-uri de la alte procese

        * */
        process.registerAbstraction(new PerfectLink(AbstractionIdUtil.getChildAbstractionId(abstractionId, AbstractionType.PL), process));
    }

    @Override
    //se apeleaza automat cand EPFD primeste un mesaj si intoarce true sau false daca a fost procesat sau nu
    public boolean handle(CommunicationProtocol.Message message) {
        switch (message.getType()) { //verifica ce mesaj a primit
            case EPFD_TIMEOUT: //semnalul de la timerul intern ca a trecut delay secunde
                handleEpfdTimeout(); //trimite heartbeat uri la ceilalti, verifica cine nu a raspuns si actualizeaza suspected, poate mari delay
                return true;
            case PL_DELIVER: //inseamna ca un mesaj a fost livrat de pl de la alt proces
                CommunicationProtocol.PlDeliver plDeliver = message.getPlDeliver();
                switch (plDeliver.getMessage().getType()) { //se uita ce mesaj a primit
                    case EPFD_INTERNAL_HEARTBEAT_REQUEST: //daca a cerut heartbeat request, raspunde cu heartbeat reply
                        handleHeartbeatRequest(plDeliver.getSender());
                        return true;
                    case EPFD_INTERNAL_HEARTBEAT_REPLY: //alt proces a trimis un reply, deci e viu => il adauga in lista alive
                        handleHeartbeatReply(plDeliver.getSender());
                        return true;
                    default:
                        return false; //daca mesajul nu e recunoscut, nu face nimic
                }
            default:
                return false;
        }
    }

    private void handleEpfdTimeout() {
        Set<CommunicationProtocol.ProcessId> aliveSuspectIntersection = new CopyOnWriteArraySet<>(alive);
        aliveSuspectIntersection.retainAll(suspected); //face intersectia cu procese pe care le-a suspectat, dar de la care a primit heartbeat
        if (!aliveSuspectIntersection.isEmpty()) {
            delay += DELTA; //daca exista astfel de procese, inseamna ca a fost prea grabit si atunci mareste delay ul
        }

        process.getProcesses().forEach(p -> { //pentru fiecare proces
            if (!alive.contains(p) && !suspected.contains(p)) { //daca nu e alive dar nu e in suspected, devine suspected
                suspected.add(p);
                triggerSuspect(p);
            } else if (alive.contains(p) && suspected.contains(p)) { //daca era in suspected si totusi e alive, il scoate din suspected si trimite Restore
                suspected.remove(p);
                triggerRestore(p);
            }
            triggerPlSendHeartbeatRequest(p); //trimite un heartbeat nou
        });

        alive.clear(); //curata alive
        startTimer(delay); //porneste alive pentru urmatoarea runda
    }

    //cand primeste un heartbeat request, trebuie sa raspunda cu heartbeat reply
    private void handleHeartbeatRequest(CommunicationProtocol.ProcessId sender) {
        CommunicationProtocol.EpfdInternalHeartbeatReply epfdHeartbeatReply = CommunicationProtocol.EpfdInternalHeartbeatReply
                .newBuilder()
                .build();

        CommunicationProtocol.Message heartbeatReplyMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REPLY)
                .setEpfdInternalHeartbeatReply(epfdHeartbeatReply)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId) //se trimite de la EPFD ul procesului la cel al destinatarului
                .setSystemId(process.getSystemId())
                .build(); //creeaza un mesaj de tip EPFD_INTERNAL_HEARTBEAT_REPLY

        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend
                .newBuilder()
                .setDestination(sender)
                .setMessage(heartbeatReplyMessage)
                .build();

        CommunicationProtocol.Message plSendMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.PL_SEND)
                .setPlSend(plSend)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getChildAbstractionId(this.abstractionId, AbstractionType.PL))
                .setSystemId(process.getSystemId())
                .build(); //PL_SEND va fi trimis catre procesul care a trimis heartbeat request ul

        process.addMessageToQueue(plSendMessage);
    }

    private void handleHeartbeatReply(CommunicationProtocol.ProcessId sender) {
        alive.add(sender);
    }

    //trimite un mesaj EPFD_SUSPECT catre nivelul superior, anuntand ca procesul p nu a trimis heartbeat la timp si e suspectat de failure
    private void triggerSuspect(CommunicationProtocol.ProcessId p) {
        CommunicationProtocol.EpfdSuspect epfdSuspect = CommunicationProtocol.EpfdSuspect
                .newBuilder()
                .setProcess(p)
                .build();

        CommunicationProtocol.Message suspectMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EPFD_SUSPECT)
                .setEpfdSuspect(epfdSuspect)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(suspectMessage);
    }

    //restaureaza un proces p care a fost suspectat anterior dar acum pare din nou viu
    private void triggerRestore(CommunicationProtocol.ProcessId p) {
        CommunicationProtocol.EpfdRestore epfdRestore = CommunicationProtocol.EpfdRestore
                .newBuilder()
                .setProcess(p)
                .build();

        CommunicationProtocol.Message restoreMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EPFD_RESTORE)
                .setEpfdRestore(epfdRestore)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(AbstractionIdUtil.getParentAbstractionId(this.abstractionId))
                .setSystemId(process.getSystemId())
                .build();

        process.addMessageToQueue(restoreMessage);
    }

    //trimite un heartbeat request catre procesul p printr-un perfect link
    private void triggerPlSendHeartbeatRequest(CommunicationProtocol.ProcessId p) {
        CommunicationProtocol.EpfdInternalHeartbeatRequest epfdHeartbeatRequest = CommunicationProtocol.EpfdInternalHeartbeatRequest
                .newBuilder()
                .build();

        CommunicationProtocol.Message heartbeatRequestMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EPFD_INTERNAL_HEARTBEAT_REQUEST)
                .setEpfdInternalHeartbeatRequest(epfdHeartbeatRequest)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId) //sunt egale pt ca mesajul vine si este destinat componentei EPFD
                .setSystemId(process.getSystemId())
                .build();

        CommunicationProtocol.PlSend plSend = CommunicationProtocol.PlSend
                .newBuilder()
                .setDestination(p)
                .setMessage(heartbeatRequestMessage)
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

    //seteaza un timer care trimite EPFD_TIMEOUT dupa delay milisecunde
    private void startTimer(int delay) {
        CommunicationProtocol.EpfdTimeout epfdTimeout = CommunicationProtocol.EpfdTimeout
                .newBuilder()
                .build(); //mesajul indica faptul ca a trecut timpul de asteptare definit

        CommunicationProtocol.Message timeoutMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.EPFD_TIMEOUT)
                .setEpfdTimeout(epfdTimeout)
                .setFromAbstractionId(this.abstractionId)
                .setToAbstractionId(this.abstractionId)
                .setSystemId(process.getSystemId())
                .build();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() { //dupa delay ms, run se apeleaza
            @Override
            public void run() {
                //EPFD va primi in mod intern un mesaj EPFD_TIMEOUT care il va determina sa
                //reactioneze (sa verifice alive/suspected, sa ajusteze delay ul)
                process.addMessageToQueue(timeoutMessage);
            }
        }, delay);
    }
}
