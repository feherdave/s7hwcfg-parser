package org.feherdave.s7hwcfg.s7;

import org.feherdave.s7hwcfg.s7.hw.HWComponent;
import org.feherdave.s7hwcfg.s7.hw.rack.Rack;
import org.feherdave.s7hwcfg.s7.hw.rack.SubsystemRack;
import org.feherdave.s7hwcfg.s7.system.Subsystem;

import java.util.*;

public class Station extends HWComponent {

    public enum StationType { NOT_IMPLEMENTED, S7_300, S7_400 }

    private String stationName;
    private StationType stationType;
    private Map<Integer, Rack> racks = new LinkedHashMap<>();
    private Map<Integer, Subsystem> subnets = new LinkedHashMap<>();
    private List<SubsystemRack> subsystemRacks = new ArrayList<>();

    Station(StationBuilder stationBuilder) {
        this.stationType = stationBuilder.getStationType();
        this.stationName = stationBuilder.getStationName();
        this.racks.putAll(stationBuilder.getRacks());
        this.subnets.putAll(stationBuilder.getSubnets());
        this.subsystemRacks.addAll(stationBuilder.getSubsystemRacks());
    }

    /**
     * Get racks defined in this station.
     *
     * @return
     */
    public Map<Integer, Rack> getRacks() {
        return racks;
    }

    /**
     * Get subnets.
     *
     * @return
     */
    public Map<Integer, Subsystem> getSubnets() {
        return subnets;
    }

    /**
     * Get subsystem racks.
     *
     * @return
     */
    public List<SubsystemRack> getSubsystemRacks() {
        return subsystemRacks;
    }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + stationName + '\'' +
                ", type=" + stationType +
                '}';
    }

    /**
     * Creates a new instance of StationBuilder.
     *
     * @return StationBuilder
     */
    public static StationBuilder builder() {
        return new StationBuilder();
    }
}
