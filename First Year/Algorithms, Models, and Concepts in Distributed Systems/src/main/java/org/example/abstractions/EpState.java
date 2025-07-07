package org.example.abstractions;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.communication.CommunicationProtocol;

@Getter
@Setter
@AllArgsConstructor
/* Reprezinta starea locala a unui proces intr-un anumit epoch
(moment logic).
* */
public class EpState {
    /*
    - marcaj de timp care indica cat de noua e valoarea.
    - folosit pentru a compara si decide ce valoare e mai recenta
      intre doua instante de EpState
    * */
    private int valTimestamp;
    /*
    - valoarea propriu zisa pe care procesul o propune sau o considera
    acceptata in acel epoch
    * */
    private CommunicationProtocol.Value val;
}
