package org.feherdave.s7hwcfg.s7.hw.module;

import org.feherdave.s7hwcfg.s7.system.Subsystem;

public class PNIOSubsystemMemberShip extends SubsystemMemberShip {

    public enum Role { CONTROLLER, DEVICE }

    private PNIOSubsystemMemberShip.Role role;

    public PNIOSubsystemMemberShip(Subsystem subsystem, Integer address, PNIOSubsystemMemberShip.Role role) {
        this.subsystem = subsystem;
        this.address = address;
        this.role = role;
    }

    public PNIOSubsystemMemberShip.Role getRole() {
        return role;
    }
}
