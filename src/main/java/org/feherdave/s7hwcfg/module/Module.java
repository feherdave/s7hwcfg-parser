package org.feherdave.s7hwcfg.module;

/**
 *  Module interface.
 */
public interface Module {

    /**
     * Adds a module to the given slot.
     *
     * @param slotNumber
     * @param module
     */
    void addModule(Integer slotNumber, Module module);

    Module getModule(Integer slotNumber);
}
