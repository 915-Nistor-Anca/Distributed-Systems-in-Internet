package org.example.abstractions;

import lombok.Getter;
import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public abstract class Abstraction { //Application, BestEffortBroadcast, NNAtomicRegister etc.
    private static final Logger log = LoggerFactory.getLogger(Abstraction.class);
    protected String abstractionId; //poate fi app, app.beb, app.beb.pl, app.pl
    protected Process process; //abc-1, abc-2, abc-3

    protected Abstraction(String abstractionId, Process process) {
        this.abstractionId = abstractionId;
        this.process = process;
        log.info("Abstraction with id {} created for process {}.", abstractionId, process.getProcessOwnerAndIndex());
    }

    public abstract boolean handle(CommunicationProtocol.Message message); //e implementata in functie de fiecare tip de abstractie
}
