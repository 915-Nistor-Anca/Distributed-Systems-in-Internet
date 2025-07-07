package org.example.util;

import org.example.communication.CommunicationProtocol;

/*message Value {
    bool defined = 1;
    int32 v = 2;
}*/
public class ValueUtil {
    //creeaza un Value in care defined = false.
    public static CommunicationProtocol.Value buildUndefinedValue() {
        return CommunicationProtocol.Value
                .newBuilder()
                .setDefined(false)
                .build();
    }
}
