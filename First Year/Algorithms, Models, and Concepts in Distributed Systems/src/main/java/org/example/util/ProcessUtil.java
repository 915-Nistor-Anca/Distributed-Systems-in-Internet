package org.example.util;

import org.example.communication.CommunicationProtocol;

import java.util.Collection;
import java.util.Comparator;

public class ProcessUtil {
    // primeste o lista de procese si returneaza proocesul cu cel mai mare rank din grup
    public static CommunicationProtocol.ProcessId getMaxRankedProcess(Collection<CommunicationProtocol.ProcessId> processes) {
        return processes.stream()
                .max(Comparator.comparing(CommunicationProtocol.ProcessId::getRank))
                .orElse(null);
    }
}
