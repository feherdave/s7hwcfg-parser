package org.feherdave.s7hwcfg.s7;

import org.feherdave.s7hwcfg.s7.hw.rack.Rack;
import org.feherdave.s7hwcfg.s7.hw.rack.SubsystemRack;
import org.feherdave.s7hwcfg.s7.system.Subsystem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StationBuilder {

    private String stationName;
    private Station.StationType stationType;
    private Map<Integer, Rack> racks = new LinkedHashMap<>();
    private Map<Integer, Subsystem> subnets = new LinkedHashMap<>();
    private List<SubsystemRack> subsystemRacks = new ArrayList<>();
    private List<String> configData = new ArrayList<>();

    public Station.StationType getStationType() {
        return stationType;
    }

    public Map<Integer, Rack> getRacks() {
        return racks;
    }

    public Map<Integer, Subsystem> getSubnets() {
        return subnets;
    }

    public String getStationName() {
        return stationName;
    }

    /**
     * Sets station type.
     *
     * @return StationBuilder
     */
    public StationBuilder type(Station.StationType stationType) {
        this.stationType = stationType;

        return this;
    }

    /**
     * Sets station name.
     *
     * @param stationName Name of the station.
     * @return StationBuilder
     */
    public StationBuilder name(String stationName) {
        this.stationName = stationName;

        return this;
    }

    /**
     * Adds a rack to the station.
     *
     * @param rack Rack object.
     * @return StationBuilder
     */
    public StationBuilder addRack(Rack rack) {
        racks.put(rack.getRackNumber(), rack);

        return this;
    }

    /**
     * Adds a subsystem rack to the station.
     *
     * @param subsystemRack
     * @return
     */
    public StationBuilder addSubsystemRack(SubsystemRack subsystemRack) {
        subsystemRacks.add(subsystemRack);

        return this;
    }

    /**
     * Adds racks to the station.
     *
     * @param rackList List of Rack objects.
     * @return StationBuilder
     */
    public StationBuilder addRacks(List<Rack> rackList) {
        rackList.forEach(rack -> racks.put(rack.getRackNumber(), rack));

        return this;
    }

    /**
     * Adds a subnet to the station.
     *
     * @param subsystem Subnet object.
     * @return StationBuilder
     */
    public StationBuilder addSubnet(Subsystem subsystem) {
        subnets.put(subsystem.getNumber(), subsystem);

        return this;
    }

    /**
     * Adds subnets to the station.
     *
     * @param subsystemList List of Subnet objects.
     * @return StationBuilder
     */
    public StationBuilder addSubnets(List<Subsystem> subsystemList) {
        subsystemList.forEach(subnet -> subnets.put(subnet.getNumber(), subnet));

        return this;
    }

    /**
     * Builds a Station object.
     *
     * @return Newly built Station.
     */
    public Station build() {
        return new Station(this);
    }

    /**
     * Adds configuration data (from HW config).
     *
     * @param configData
     * @return
     */
    public StationBuilder configData(List<String> configData) {
        this.configData.addAll(configData);
        return this;
    }

    /**
     * Gets subsystem racks.
     *
     * @return
     */
    public List<SubsystemRack> getSubsystemRacks() {
        return subsystemRacks;
    }
}
