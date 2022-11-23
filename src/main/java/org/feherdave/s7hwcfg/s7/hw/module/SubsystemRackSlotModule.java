package org.feherdave.s7hwcfg.s7.hw.module;

import org.feherdave.s7hwcfg.s7.hw.HWComponent;

import java.util.*;

public class SubsystemRackSlotModule extends HWComponent implements Module {

    private Integer address;
    private Integer subsystemNumber;
    private Integer slotNumber;
    private Map<Integer, SubSlotModule> subModules = new LinkedHashMap<>();
    private String orderNumber;
    private String name;
    private String version;

    public SubsystemRackSlotModule(Integer subsystemNumber, Integer address, Integer slotNumber, String orderNumber, String version, String name) {
        this.address = address;
        this.subsystemNumber = subsystemNumber;
        this.slotNumber = slotNumber;
        this.orderNumber = orderNumber;
        this.version = version;
        this.name = name;
    }

    public Integer getSubsystemNumber() {
        return subsystemNumber;
    }

    public Integer getAddress() {
        return address;
    }

    public Integer getSlotNumber() {
        return slotNumber;
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

    /**
     * Gets the module inserted into a slot.
     *
     * @param slotNumber
     * @return Module or null if there is no module in the specified slot.
     */
    public Optional<Module> getModule(Integer slotNumber) {
        return Optional.ofNullable(subModules.get(slotNumber));
    }

    @Override
    public String toString() {
        return "Module{" +
                "slotNumber=" + slotNumber +
                ", orderNumber='" + orderNumber + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    public Map<Integer, SubSlotModule> getSubModules() {
        return subModules;
    }

}
