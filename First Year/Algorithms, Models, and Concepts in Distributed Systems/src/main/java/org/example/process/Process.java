package org.example.process;

import lombok.Getter;
import org.example.abstractions.Abstraction;
import org.example.abstractions.AbstractionType;
import org.example.abstractions.Application;
import org.example.abstractions.NNAtomicRegister;
import org.example.communication.CommunicationProtocol;
import org.example.communication.MessageReceiver;
import org.example.communication.MessageSender;
import org.example.util.AbstractionIdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class Process implements Runnable, Observer {

    private static final Logger log = LoggerFactory.getLogger(Process.class);
    private final CommunicationProtocol.ProcessId hub; //hub-ul la care se face inregistrarea (127.0.0.1:5000)
    private final BlockingQueue<CommunicationProtocol.Message> messageQueue;
    /*
    - coada pentru mesaje care asteapta sa fie procesate
    - messageQueue poate fi accesata simultan de mai multe thread-uri
    fara sa apara probleme de corupere a datelor sau comportament neașteptat.
    * */
    private final Map<String, Abstraction> abstractions; //mapa de abstractizari; fiecare abstractizare trateaza anumite mesaje
    private CommunicationProtocol.ProcessId process; //id-ul propriu al procesului
    private List<CommunicationProtocol.ProcessId> processes; //lista cu toate procesele (abc-1, abc-2, abc-3)
    private String systemId; //identificatorul sistemului la nivel global

    public Process(CommunicationProtocol.ProcessId process, CommunicationProtocol.ProcessId hub) throws InterruptedException {
        this.process = process;
        this.hub = hub;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.abstractions = new ConcurrentHashMap<>();
    }

    /*Cand pornesc un Process, el isi porneste threadul run() si se inregistreaza la hub() prin metoda
    reister(), trimitand PROC_REGISTRATION.
    Hub-ul primeste toate inregistrarile si le retine intr-o lista interne de procese.
    Cand a primit toate inregistrarile, trimite un mesaj PROC_INITIALIZE_SYSTEM catre toate procesele.
    Procesele primesc mesajul prin MessageReceiver(), care declanseaza update().
    In update, daca mesajul e de tip PROC_INITIALIZE_SYSTEM, e procesat imediat.
    * */
    @Override
    public void run() {
        log.info("This process is {}, hub is at {}:{}. Running process {}-{}.", process.getIndex(), hub.getHost(), hub.getPort(), process.getOwner(), process.getIndex());

        registerAbstraction(new Application(AbstractionType.APP.getId(), this)); //adauga abstractia principala APP, care va trata mesajele de tip aplicatie

        //eventLoop preia mesaje din coada, le redirectioneaza catre abstractia potrivita si instantiaza abstractii noi (daca e cazul)
        Runnable eventLoop = () -> {
            while (true) {
                try {
                    CommunicationProtocol.Message message = messageQueue.take(); //ia un mesaj din coada
                    log.info("Handling {}; FromAbstractionId: {}; ToAbstractionId: {}", message.getType(), message.getFromAbstractionId(), message.getToAbstractionId());
                    //inregistrez abstractii in functie de mesajele primite
                    if (!abstractions.containsKey(message.getToAbstractionId())) { //verifica daca este deja o abstractie inregistrata pentru toAbstractionId
                        if (message.getToAbstractionId().contains(AbstractionType.NNAR.getId())) { //daca nu avem, dar mesajul e destinat unui registru atomic, il cream
                            registerAbstraction(new NNAtomicRegister(AbstractionIdUtil.getNamedAncestorAbstractionId(message.getToAbstractionId()), this));
                        }
                    }
                    //daca exista, trimite mesajul catre metoda handle din abstractie; daca handle returneaza false (adica mesajul nu a fost tratat),
                    //atunci mesajul e readaugat in coada daca e eligibil
                    if (abstractions.containsKey(message.getToAbstractionId())) {
                        if (!abstractions.get(message.getToAbstractionId()).handle(message) && requeueMessage(message)) {
                            addMessageToQueue(message);
                        }
                    } else { //daca nici dupa toate verificarile nu avem cui da mesajul, il incercam mai tarziu
                        addMessageToQueue(message);
                    }
                } catch (InterruptedException interruptedException) {
                    log.error("Error handling message.");
                }
            }
        };
        //ii dau un nume sugestiv procesului, de ex. abc-2 : 5005
        String processName = String.format("%s-%d : %d", process.getOwner(), process.getIndex(), process.getPort());
        Thread eventLoopThread = new Thread(eventLoop, processName); // ma folosesc de eventLoop-ul creat mai sus ca sa creez un thread
        eventLoopThread.start(); // pornesc bucla

        //messageReceiver asculta pe portul procesului pentru mesaje de la alti participanti din retea
        MessageReceiver messageReceiver = new MessageReceiver(process.getPort());
        messageReceiver.addObserver(this);
        Thread messageReceiverThread = new Thread(messageReceiver, processName);
        messageReceiverThread.start();

        register();

        try {
            messageReceiverThread.join();
            eventLoopThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void registerAbstraction(Abstraction abstraction) {
        abstractions.putIfAbsent(abstraction.getAbstractionId(), abstraction);
    }

    public void addMessageToQueue(CommunicationProtocol.Message message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            log.error("Error adding message to queue.");
        }
    }

    /*
    - verifica daca un mesaj trebuie repus in coada de mesaje pt a fi procesat mai tarziu
    - = a fost trimis prin perfect link si are continutul NNAR_INTERNAL_VALUE sau NNAR_INTERNAL_ACK
    - metoda iti permite sa primesti mesajele mai devreme dar sa le amani procesarea pana cand starea
      interna este corecta
    - pt a evita erorile de initializare (metoda e doar pt mesajele nnar care necesita o stare valida a registrului
       pt a fi interpretate corect
    */
    private boolean requeueMessage(CommunicationProtocol.Message message) {
        return CommunicationProtocol.Message.Type.PL_DELIVER.equals(message.getType()) &&
                (CommunicationProtocol.Message.Type.NNAR_INTERNAL_VALUE.equals(message.getPlDeliver().getMessage().getType()) ||
                        CommunicationProtocol.Message.Type.NNAR_INTERNAL_ACK.equals(message.getPlDeliver().getMessage().getType()));
    }

    //inregistreaza procesul in retea (deci un mesaj Message(NetworkMessage(Message(ProcRegistrationMessage)))
    //trimite un mesaj de inregistrare catre hub-ul central (anunta hub-ul ca s-a pornit si cine este)
    private void register() {
        CommunicationProtocol.ProcRegistration procRegistration = CommunicationProtocol.ProcRegistration
                .newBuilder()
                .setOwner(process.getOwner())
                .setIndex(process.getIndex())
                .build(); //informatii despre proces (owner si index)

        CommunicationProtocol.Message procRegistrationMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.PROC_REGISTRATION)
                .setProcRegistration(procRegistration)
                .setMessageUuid(UUID.randomUUID().toString())
                .setToAbstractionId(AbstractionType.APP.getId())
                .build(); //se creeaza un mesaj de tipul PROC_REGISTRATION, destinat abstractiei APP

        CommunicationProtocol.NetworkMessage networkMessage = CommunicationProtocol.NetworkMessage
                .newBuilder()
                .setSenderHost(process.getHost())
                .setSenderListeningPort(process.getPort())
                .setMessage(procRegistrationMessage)
                .build(); //pun mesajul intr-un NetworkMessage care contine host-ul, port-ul si mesajul

        CommunicationProtocol.Message outerMessage = CommunicationProtocol.Message
                .newBuilder()
                .setType(CommunicationProtocol.Message.Type.NETWORK_MESSAGE)
                .setNetworkMessage(networkMessage)
                .setToAbstractionId(procRegistrationMessage.getToAbstractionId())
                .setMessageUuid(UUID.randomUUID().toString())
                .build(); //creez un mesaj de retea de tipul NETWORK_MESSAGE

        MessageSender.send(outerMessage, hub.getHost(), hub.getPort()); //trimit tot mesajul catre hub
    }

    //se declanseaza automat cand MessageReceiver notifica observatorii, adica chiar clasa procesului
    //metoda primeste mesaje de la retea si decide ce sa faca cu ele
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof CommunicationProtocol.Message) {
            CommunicationProtocol.Message message = (CommunicationProtocol.Message) arg;
            CommunicationProtocol.Message innerMessage = message.getNetworkMessage().getMessage();
            log.debug("Received message: {}", innerMessage);
            if (CommunicationProtocol.Message.Type.PROC_INITIALIZE_SYSTEM.equals(innerMessage.getType())) {
                handleProcInitializeSystem(innerMessage); //daca e un mesaj de initializare, il proceseaza imediat
            } else { // se adauga mesajul in coada pentru a fi tratat mai tarziu de bucla de evenimente
                messageQueue.add(message);
            }
        }
    }

    private void handleProcInitializeSystem(CommunicationProtocol.Message message) {
        log.debug("Handling PROC_INITIALIZE_SYSTEM for process with port {}", this.process.getPort());
        CommunicationProtocol.ProcInitializeSystem procInitializeSystem = message.getProcInitializeSystem();
        this.processes = procInitializeSystem.getProcessesList();// salvez lista cu toate procesele din sistem
        //din lista de mai sus, isi cauta propriul proces, pe baza host si port si apoi inlocuieste process cu
        //obiectul complet din lista, care contine si alte detalii (de ex. owner, index etc)
        this.process = getProcessByHostAndPort(this.process.getHost(), this.process.getPort()).get();
        this.systemId = message.getSystemId(); //salveaza id-ul global al sistemului
    }

    //cauta in lista de procese un proces pe baza host-ului si al portului si il returneaza daca il gaseste
    public Optional<CommunicationProtocol.ProcessId> getProcessByHostAndPort(String host, int port) {
        return processes.stream()
                .filter(p -> host.equals(p.getHost()) && p.getPort() == port)
                .findFirst();
    }

    public String getProcessOwnerAndIndex() {
        return process.getOwner() + "-" + process.getIndex();
    }
}
