package org.feherdave.s7hwcfg.module;

import org.feherdave.s7hwcfg.HWComponent;
import org.feherdave.s7hwcfg.Station;
import org.feherdave.s7hwcfg.cfgfile.CfgFileSection;
import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileSectionFormatErrorException;
import org.feherdave.s7hwcfg.Subnet;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlotModule extends HWComponent implements Module {

    public enum BusRole { ROLE_UNDEFINED, ROLE_MASTER, ROLE_SLAVE }
    public static String SECTION_HEADER_REGEXP = "^RACK\\s+(?<rackNumber>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotNumber>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";

    private Integer address;
    private Integer rackNumber;
    private Integer slotNumber;
    private Map<Integer, SubSlotModule> subModules = new LinkedHashMap<>();
    private String orderNumber;
    private String name;
    private String version;
    private Map<String, String> data = new HashMap<>();
    private Subnet subnet;

    private BusRole busRole = BusRole.ROLE_UNDEFINED;

    public SlotModule(Integer rackNumber, Integer slotNumber, String orderNumber, String version, String name) {
        this.rackNumber = rackNumber;
        this.slotNumber = slotNumber;
        this.orderNumber = orderNumber;
        this.version = version;
        this.name = name;
    }

    /**
     * Creates a Module object based on an adequate config file section.
     *
     * @param section
     * @return
     * @throws S7HWCfgFileSectionFormatErrorException
     */
    public static SlotModule fromSectionData(CfgFileSection section, Station station) throws S7HWCfgFileSectionFormatErrorException {

        Matcher m = Pattern.compile(SECTION_HEADER_REGEXP).matcher(section.getSectionHeader());

        if (m.matches()) {
            Integer rackNumber = Integer.parseInt(m.group("rackNumber"));
            Integer slotNumber = Integer.parseInt(m.group("slotNumber"));
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String name = m.group("name");

            SlotModule res = new SlotModule(rackNumber, slotNumber, orderNumber, version, name);

            // Check additional data (e.g. the module is a bus participant)
            List<String> parameters = section.getSectionHeaderOptions();
            String busSettingsRegex = "^MASTER (DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<subsysnumber>[0-9]+)\\s*,.*DPADDRESS\\s+(?<address>[0-9]+)$";
            Optional<String> busSettingString = parameters.stream().filter(line -> line.matches(busSettingsRegex)).findFirst();

            busSettingString.ifPresent(str -> {
                    Matcher m2 = Pattern.compile(busSettingsRegex).matcher(str);

                    if (m2.matches()) {
                        // Yeeeaaaahhh!! Module is a master
                        Integer subsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                        Integer address = Integer.parseInt(m2.group("address"));
                        res.setBusRole(BusRole.ROLE_MASTER);
                        res.setAddress(address);

                        Subnet subnet = station.getSubnets().get(subsysNumber);

                        if (subnet == null) {
                            System.err.println("Trying to add a device to a non-existent subsystem: " + res.toString());
                        } else {
                            subnet.attachNode(address, res);
                            res.setSubnet(subnet);
                        }
                    }
            });

            // Parse configuration data
            res.parseConfigurationData(section.getConfigurationData());

            return res;
        } else {
            throw new S7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getSectionHeader());
        }
    }

    /**
     * Adds a key-value pair to data section.
     *
     * @param key
     * @param value
     */
    public void putData(String key, String value) {
        data.put(key, value);
    }

    public Integer getSlotNumber() {
        return slotNumber;
    }

    public Integer getRackNumber() {
        return rackNumber;
    }

    public void setAddress(Integer address) {
        this.address = address;
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
    public SubSlotModule getModule(Integer slotNumber) {
        return subModules.get(slotNumber);
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

    private void setBusRole(BusRole role) {
        this.busRole = role;
    }

    public Map<Integer, SubSlotModule> getSubModules() {
        return subModules;
    }

    private void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }
}
