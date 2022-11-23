package org.feherdave.s7hwcfg.s7.hw.module;

import java.util.Optional;

/**
 *  Module interface.
 */
public interface Module {

    /**
     * Adds a module to the given slot.
     *
     * @param slotNumber Number of slot to add.
     * @param module Module object to add.
     */
    void addModule(Integer slotNumber, Module module);

    /**
     * Gets the module inserted in the given slot.
     *
     * @param slotNumber Number of slot.
     * @return Optional<Module> Optional containing the Module object or an empty optional is there is no module in the given slot.
     */
    Optional<Module> getModule(Integer slotNumber);
}
