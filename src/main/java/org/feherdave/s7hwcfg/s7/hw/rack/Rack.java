package org.feherdave.s7hwcfg.s7.hw.rack;

import org.feherdave.s7hwcfg.s7.hw.HWComponent;
import org.feherdave.s7hwcfg.s7.hw.module.Module;
import org.feherdave.s7hwcfg.s7.hw.module.SlotModule;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Rack extends HWComponent {

    private Integer rackNumber;
    private String rackName;
    private String orderNumber;
    private Map<Integer, SlotModule> slots = new LinkedHashMap<>();

    public Rack(int rackNumber, String orderNumber, String rackName) {
        this.rackNumber = rackNumber;
        this.orderNumber = orderNumber;
        this.rackName = rackName;
    }

    /**
     * Adds a module to the given slot.
     *
     * @param module
     */
    public void addModule(SlotModule module) {
        slots.put(module.getSlotNumber(), module);
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
    public void addModule(Integer slotNumber, SlotModule module) {
        slots.put(slotNumber, module);
    }

    /**
     * Gets the module inserted into a slot.
     *
     * @param slotNumber
     * @return Module or null if there is no module in the specified slot.
     */
    public Module getModule(Integer slotNumber) {
        return slots.get(slotNumber);
    }

    @Override
    public String toString() {
        return "Rack{" +
                "number=" + rackNumber +
                ", name='" + rackName + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                '}';
    }
}
