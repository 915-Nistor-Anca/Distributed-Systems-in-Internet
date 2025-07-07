package org.example;

import org.example.communication.CommunicationProtocol;
import org.example.process.Process;
import org.example.util.AbstractionIdUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        String hubHost = args[0]; //127.0.0.1
        int hubPort = Integer.parseInt(args[1]); //5000
        CommunicationProtocol.ProcessId hubInfo = CommunicationProtocol.ProcessId
                .newBuilder()
                .setHost(hubHost)
                .setPort(hubPort)
                .setOwner(AbstractionIdUtil.HUB_ID)
                .build(); //hubInfo va fi 127.0.0.1:5000

        final String processHost = args[2]; //127.0.0.1
        final List<Integer> processPorts = Arrays.asList(Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5])); //5004, 5005, 5006
        final String processOwner = args[6]; //abc

        List<CommunicationProtocol.ProcessId> processIds = processPorts.stream()
                .map(port -> CommunicationProtocol.ProcessId.newBuilder()
                        .setHost(processHost)
                        .setPort(port)
                        .setOwner(processOwner)
                        .setIndex(processPorts.indexOf(port) + 1)
                        .build())
                .collect(Collectors.toList()); //creeaza o lista cu 3 procese id, cu host 127.0.0.1, owner-ul abc si porturile 5004, 5005, 5006

        Thread process1 = new Thread(new Process(processIds.get(0), hubInfo), processHost + ":" + processPorts.get(0));
        Thread process2 = new Thread(new Process(processIds.get(1), hubInfo), processHost + ":" + processPorts.get(1));
        Thread process3 = new Thread(new Process(processIds.get(2), hubInfo), processHost + ":" + processPorts.get(2));

        process1.start();
        process2.start();
        process3.start();

        process1.join();
        process2.join();
        process3.join();
    }
}
