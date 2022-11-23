package org.feherdave.s7hwcfg.s7.hw.module;

import org.feherdave.s7hwcfg.s7.system.Subsystem;

public class DPSubsystemMemberShip extends SubsystemMemberShip {

    public enum Role { MASTER, SLAVE }

    private Role role;

    public DPSubsystemMemberShip(Subsystem subsystem, Integer address, Role role) {
        this.subsystem = subsystem;
        this.address = address;
        this.role = role;
    }

    public Role getRole() {
        return role;
    }
}
