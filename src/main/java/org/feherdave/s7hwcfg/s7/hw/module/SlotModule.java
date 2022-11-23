package org.feherdave.s7hwcfg.s7.hw.module;

import org.feherdave.s7hwcfg.s7.hw.HWComponent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SlotModule extends HWComponent implements Module {

    private Integer rackNumber;
    private Integer slotNumber;
    private Map<Integer, SubSlotModule> subModules = new LinkedHashMap<>();
    private String orderNumber;
    private String name;
    private String version;

    public SlotModule(Integer rackNumber, Integer slotNumber, String orderNumber, String version, String name) {
        this.rackNumber = rackNumber;
        this.slotNumber = slotNumber;
        this.orderNumber = orderNumber;
        this.version = version;
        this.name = name;
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public Integer getRackNumber() {
        return rackNumber;
    }

    /**
     * Inserts a module into the given slot.
     *
     * @param slotNumber
     * @param module
     */
    public void addModule(Integer slotNumber, Module module) {
        if (module instanceof SubSlotModule) {
            subModules.put(slotNumber, (SubSlotModule) module);
        }
    }

    @Override
    public Optional<Module> getModule(Integer slotNumber) {
        return Optional.ofNullable(subModules.get(slotNumber));
    }

    @Override
    public String toString() {
        return "Module{" +
                "rackNumber=" + rackNumber +
                ", slotNumber=" + slotNumber +
                ", typeCode='" + orderNumber + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public Map<Integer, SubSlotModule> getSubModules() {
        return subModules;
    }

    public void setSubsystemMemberShip(SubsystemMemberShip subsystemMemberShip) {
        this.subsystemMemberShip = subsystemMemberShip;
    }
}
