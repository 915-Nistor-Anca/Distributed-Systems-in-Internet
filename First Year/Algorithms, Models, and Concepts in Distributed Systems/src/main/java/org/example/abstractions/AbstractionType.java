package org.example.abstractions;

import lombok.Getter;

@Getter
public enum AbstractionType { //toate tipurile posibile de abstractii din sistem
    PL("pl"), //Perfect Link
    BEB("beb"), //BestEffortBroadcast
    APP("app"), //Application
    NNAR("nnar"), //(N, N) Atomic Register
    EPFD("epfd"), //EventuallyPerfectFailureDetector
    ELD("eld"), //EventuallyLeaderDetector
    EP("ep"), //EpochConsensus
    EC("ec"), //EpochChange
    UC("uc"); //UniformConsensus

    private final String id;

    AbstractionType(String id) {
        this.id = id;
    }
}
