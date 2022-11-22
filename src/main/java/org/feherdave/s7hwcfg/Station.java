package org.feherdave.s7hwcfg;

import org.feherdave.s7hwcfg.cfgfile.CfgFileSection;
import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileFormatException;
import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileSectionFormatErrorException;
import org.feherdave.s7hwcfg.module.SlotModule;
import org.feherdave.s7hwcfg.module.SubSlotModule;
import org.feherdave.s7hwcfg.module.SubsystemRackSlotModule;
import org.feherdave.s7hwcfg.rack.Rack;
import org.feherdave.s7hwcfg.rack.SubsystemRack;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Station extends HWComponent {

    public static String SECTION_HEADER_REGEXP = "^STATION\\s+(?<stationtype>[A-Z0-9]+)\\s*,\\s*\"(?<stationname>.*?)\"\\s*$";

    public enum StationType { NOT_IMPLEMENTED, S7_300, S7_400 }

    private String stationName;
    private StationType stationType = StationType.NOT_IMPLEMENTED;
    private Map<Integer, Rack> racks = new LinkedHashMap<>();
    private Map<Integer, Subnet> subnets = new LinkedHashMap<>();

    public Station() {
    }

    public void addRack(Rack rack) {
        racks.put(rack.getRackNumber(), rack);
    }

    public void putStationData(String key, String value) {
        stationData.put(key, value);
    }

    public String getStationData(String key) {
        return stationData.get(key);
    }

    /**
     * Get subnets.
     *
     * @return
     */
    public Map<Integer, Subnet> getSubnets() {
        return subnets;
    }

    /**
     * Creates a station from config file sections.
     *
     * @param sectionData
     * @return
     * @throws S7HWCfgFileSectionFormatErrorException
     * @throws S7HWCfgFileFormatException
     */
    public static Station fromSectionData(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException, S7HWCfgFileFormatException {
        Station res = new Station();

        res.parseSectionData(sectionData);

        return res;
    }

    /**
     * Parse a list of section data.
     *
     * @param sectionData
     * @throws S7HWCfgFileSectionFormatErrorException
     * @throws S7HWCfgFileFormatException
     */
    private void parseSectionData(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException, S7HWCfgFileFormatException {

        // Parse section data
        CfgFileSection stationSection = sectionData.stream().filter(sect -> sect.getSectionHeader().startsWith("STATION")).findFirst().orElseThrow(() -> new S7HWCfgFileFormatException("STATION section doesn't exist"));

        Pattern p = Pattern.compile(SECTION_HEADER_REGEXP);

        Matcher m = p.matcher(stationSection.getSectionHeader());

        // Check header
        if (m.matches()) {

            // Store data from header
            switch (m.group("stationtype")) {
                case "S7400":
                    this.stationType = StationType.S7_400;
                    break;
                case "S7300":
                    this.stationType = StationType.S7_300;
                    break;
                default:
                    this.stationType = StationType.NOT_IMPLEMENTED;
            }
            this.stationName = m.group("stationname");

            // Parse configuration data
            super.parseConfigurationData(stationSection.getConfigurationData());
        } else {
            throw new S7HWCfgFileSectionFormatErrorException("Format error in STATION section.");
        }

        // Add racks
        sectionData.removeAll(parseRacks(sectionData));

        // Add subnets
        sectionData.removeAll(parseSubnets(sectionData));

        // Add modules to racks
        sectionData.removeAll(parseSlotModules(sectionData));

        // Add submodules to modules
        sectionData.removeAll(parseSubSlotModules(sectionData));

        // Add subsystem racks
        sectionData.removeAll(parseSubsystemRacks(sectionData));

        // Add modules to subsystem racks
        sectionData.removeAll(parseSubsystemRackSlotModules(sectionData));
    }

    /**
     * Parses section data list for racks.
     *
     * @param sectionData
     * @return List<CfgFileSection> Sublist of processed module sections for further use.
     */
    private List<CfgFileSection> parseRacks(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException {
        List<CfgFileSection> rackSections = sectionData.stream()
                .filter(data -> data.getSectionHeader().matches(Rack.SECTION_HEADER_REGEXP))
                .collect(Collectors.toList());

        // This is only for exception handling
        for (CfgFileSection sect : rackSections) {
            addRack(Rack.fromSectionData(sect, this));
        }

        return rackSections;
    }

    /**
     * Parses section data list for modules.
     *
     * @param sectionData
     * @return List<CfgFileSection> Sublist of processed module sections for further use.
     */
    private List<CfgFileSection> parseSlotModules(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException {
        List<CfgFileSection> slotModuleSections = sectionData.stream()
                .filter(data -> data.getSectionHeader().matches(SlotModule.SECTION_HEADER_REGEXP))
                .collect(Collectors.toList());

        // This is only for exception handling
        for (CfgFileSection sect : slotModuleSections) {
            SlotModule slotModule = SlotModule.fromSectionData(sect, this);

            racks.get(slotModule.getRackNumber()).addModule(slotModule);
        }

        return slotModuleSections;
    }

    /**
     * Parses section data list for slot submodules.
     *
     * @param sectionData
     * @return List<CfgFileSection> Sublist of processed module sections for further use.
     */
    private List<CfgFileSection> parseSubSlotModules(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException {
        List<CfgFileSection> subSlotModuleSections = sectionData.stream()
                .filter(data -> data.getSectionHeader().matches(SubSlotModule.SECTION_HEADER_REGEXP))
                .collect(Collectors.toList());

        // This is only for exception handling
        for (CfgFileSection sect : subSlotModuleSections) {
            SubSlotModule subSlotModule = SubSlotModule.fromSectionData(sect, this);

            racks
                    .get(subSlotModule.getRackNumber())
                    .getModule(subSlotModule.getSlotNumber())
                    .addModule(subSlotModule.getSubslotNumber(), subSlotModule);
        }

        return subSlotModuleSections;
    }

    /**
     * Parses section data list for subsystems (subnets).
     *
     * @param sectionData
     * @return List<CfgFileSection> Sublist of processed subnet subsystems (subnets) for further use.
     */
    private List<CfgFileSection> parseSubnets(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException {
        List<CfgFileSection> subnetSections = sectionData.stream()
                .filter(data -> data.getSectionHeader().matches(Subnet.SECTION_HEADER_REGEXP))
                .collect(Collectors.toList());

        // This is only for exception handling
        for (CfgFileSection sect : subnetSections) {
            Subnet subnet = Subnet.fromSectionData(sect, this);

            subnets.put(subnet.getNumber(), subnet);
        }

        return subnetSections;
    }

    /**
     * Parses section data list of subsystem racks.
     *
     * @param sectionData
     * @return List<CfgFileSection> Sublist of processed subsystem racks for further use.
     */
    private List<CfgFileSection> parseSubsystemRacks(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException {
        List<CfgFileSection> subsystemModuleSections = sectionData.stream()
                .filter(data -> data.getSectionHeader().matches(SubsystemRack.SECTION_HEADER_REGEXP))
                .collect(Collectors.toList());

        // This is only for exception handling
        for (CfgFileSection sect : subsystemModuleSections) {
            SubsystemRack.fromSectionData(sect, this);
        }

        return subsystemModuleSections;
    }

    /**
     * Parses section data list of subsystem rack slot modules.
     *
     * @param sectionData
     * @return List<CfgFileSection> Sublist of processed subsystem racks for further use.
     */
    private List<CfgFileSection> parseSubsystemRackSlotModules(List<CfgFileSection> sectionData) throws S7HWCfgFileSectionFormatErrorException {
        List<CfgFileSection> subsystemRackSlotModuleSections = sectionData.stream()
                .filter(data -> data.getSectionHeader().matches(SubsystemRackSlotModule.SECTION_HEADER_REGEXP))
                .collect(Collectors.toList());

        // This is only for exception handling
        for (CfgFileSection sect : subsystemRackSlotModuleSections) {
            SubsystemRackSlotModule.fromSectionData(sect, this);
        }

        return subsystemRackSlotModuleSections;
    }

    @Override
    public String toString() {
        return "Station{" +
                "name='" + stationName + '\'' +
                ", type=" + stationType +
                '}';
    }


}
