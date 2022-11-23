package org.feherdave.s7hwcfg.s7.hw.module;

import org.feherdave.s7hwcfg.s7.system.Subsystem;

public abstract class SubsystemMemberShip {

    protected Subsystem subsystem;
    protected Integer address;

    public Subsystem getSubsystem() {
        return subsystem;
    }

    public Integer getAddress() {
        return address;
    }
}
