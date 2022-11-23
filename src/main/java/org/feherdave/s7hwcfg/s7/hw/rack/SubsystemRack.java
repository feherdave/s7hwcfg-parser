package org.feherdave.s7hwcfg.s7.hw.rack;

import org.feherdave.s7hwcfg.s7.hw.HWComponent;
import org.feherdave.s7hwcfg.s7.hw.module.Module;
import org.feherdave.s7hwcfg.s7.hw.module.SubsystemRackSlotModule;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SubsystemRack extends HWComponent implements Module {

    private Integer subsystemNumber;
    private Integer address;
    private String orderNumber;
    private String designation;
    private String version;
    private Map<Integer, SubsystemRackSlotModule> modules = new LinkedHashMap<>();

    public SubsystemRack(Integer subsystemNumber, Integer address, String orderNumber, String version, String designation) {
        this.subsystemNumber = subsystemNumber;
        this.address = address;
        this.orderNumber = orderNumber;
        this.version = version;
        this.designation = designation;
    }

    @Override
    public void addModule(Integer slotNumber, Module module) {
        if (module instanceof SubsystemRackSlotModule) {
            modules.put(slotNumber, (SubsystemRackSlotModule) module);
        }
    }

    @Override
    public Optional<Module> getModule(Integer slotNumber) {
        return Optional.ofNullable(modules.get(slotNumber));
    }

    @Override
    public String toString() {
        return "SubsystemRack{" +
                "orderNumber='" + orderNumber + '\'' +
                ", designation='" + designation + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
