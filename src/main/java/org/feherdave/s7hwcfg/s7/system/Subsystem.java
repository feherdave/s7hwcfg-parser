package org.feherdave.s7hwcfg.s7.system;

import org.feherdave.s7hwcfg.s7.HWConfigElement;
import org.feherdave.s7hwcfg.s7.hw.module.Module;

import java.util.LinkedHashMap;
import java.util.Map;

public class Subsystem extends HWConfigElement {

    public enum SubnetType { NOT_IMPLEMENTED, MPI, PROFIBUS_DP, PROFINET}

    private SubnetType subnetType;
    private String name;
    private Integer number;
    private Map<Integer, Module> nodes = new LinkedHashMap<>();

    public Subsystem(SubnetType type, String name, Integer number) {
        this.subnetType = type;
        this.name = name;
        this.number = number;
    }

    /**
     * Attach a node to the subsystem.
     *
     * @param address
     * @param node
     */
    public void attachNode(Integer address, Module node) {
        this.nodes.put(address, node);
    }

    public Module getNode(Integer address) {
        return nodes.get(address);
    }

    public String getName() {
        return name;
    }

    public Integer getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "Subnet{" +
                "subnetType=" + subnetType +
                ", name='" + name + '\'' +
                ", number=" + number +
                '}';
    }
}
