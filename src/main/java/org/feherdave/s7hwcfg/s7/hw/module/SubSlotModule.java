package org.feherdave.s7hwcfg.s7.hw.module;

import org.feherdave.s7hwcfg.s7.hw.HWComponent;

import java.util.Optional;

public class SubSlotModule extends HWComponent implements Module {

    private Integer rackNumber;
    private Integer slotNumber;
    private Integer subslotNumber;
    private String orderNumber;
    private String name;
    private String version;

    public SubSlotModule(Integer rackNumber, Integer slotNumber, Integer subslotNumber, String orderNumber, String version, String name) {
        this.rackNumber = rackNumber;
        this.slotNumber = slotNumber;
        this.subslotNumber = subslotNumber;
        this.orderNumber = orderNumber;
        this.version = version;
        this.name = name;
    }

    /**
     * Adds a key-value pair to data section.
     *
     * @param key
     * @param value
     */
    public void putData(String key, String value) {
        data.put(key, value);
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public Integer getSubslotNumber() {
        return subslotNumber;
    }

    public Integer getRackNumber() {
        return rackNumber;
    }

    @Override
    public String toString() {
        return "Module{" +
                "rackNumber=" + rackNumber +
                ", slotNumber=" + slotNumber +
                ", subslotNumber=" + subslotNumber +
                ", orderNumber='" + orderNumber + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    @Override
    public void addModule(Integer slotNumber, Module module) {
        throw new UnsupportedOperationException("No more modules can be added.");
    }

    @Override
    public Optional<Module> getModule(Integer slotNumber) {
        return Optional.empty();
    }

    public void setSubsystemMemberShip(SubsystemMemberShip subsystemMemberShip) {
        this.subsystemMemberShip = subsystemMemberShip;
    }

}
