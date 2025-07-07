package org.example.abstractions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.communication.CommunicationProtocol;
import org.example.util.ValueUtil;

@Getter
@Setter
@AllArgsConstructor
public class NNARValue {
    /* Clasa reprezinta valoarea interna stocata intr-un registru
    atomic de tip NNAR.
    */
    private int timestamp; // versiunea valorii (cu cat e mai mare, cu atat e mai recenta)
    private int writerRank; // prioritatea procesului care a scris valoarea (folosit pentru egalitati de timestamp)
    private CommunicationProtocol.Value value; // valoarea efectiva stocata

    public NNARValue() {
        timestamp = 0;
        writerRank = 0;
        value = ValueUtil.buildUndefinedValue(); //initializez registrul, inainte ca cineva sa fi scris ceva
    }
}