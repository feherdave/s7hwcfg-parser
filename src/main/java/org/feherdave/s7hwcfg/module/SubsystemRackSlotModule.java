package org.feherdave.s7hwcfg.module;

import org.feherdave.s7hwcfg.HWComponent;
import org.feherdave.s7hwcfg.Station;
import org.feherdave.s7hwcfg.Subnet;
import org.feherdave.s7hwcfg.cfgfile.CfgFileSection;
import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubsystemRackSlotModule extends HWComponent implements Module {

    public enum BusRole { ROLE_UNDEFINED, ROLE_MASTER, ROLE_SLAVE }
    public static String SECTION_HEADER_REGEXP = "^(?<subsystype>DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<subsysno>[0-9]+)\\s*,\\s*DPADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotno>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";

    private Integer slotNumber;
    private Map<Integer, SubSlotModule> subModules = new LinkedHashMap<>();
    private String orderNumber;
    private String name;
    private String version;
    private Map<String, String> data = new HashMap<>();
    /** In case of this slot module is a bus interface. */
    private Integer address;
    private Subnet subnet;

    private BusRole busRole = BusRole.ROLE_UNDEFINED;

    public SubsystemRackSlotModule(Integer slotNumber, String orderNumber, String version, String name) {
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
    public static SubsystemRackSlotModule fromSectionData(CfgFileSection section, Station station) throws S7HWCfgFileSectionFormatErrorException {

        Matcher m = Pattern.compile(SECTION_HEADER_REGEXP).matcher(section.getSectionHeader());

        if (m.matches()) {
            Integer subsysNumber = Integer.parseInt(m.group("subsysno"));
            Integer address = Integer.parseInt(m.group("address"));
            Integer slotNumber = Integer.parseInt(m.group("slotno"));
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String name = m.group("name");

            SubsystemRackSlotModule res = new SubsystemRackSlotModule(slotNumber, orderNumber, version, name);

            Subnet subnet = station.getSubnets().get(subsysNumber);

            if (subnet == null) {
                System.err.println("Trying to add a device to a non-existent subsystem: " + res);
            } else {
                subnet.getNode(address).addModule(slotNumber, res);
            }

            // Check additional data (e.g. the module is a bus participant)
            List<String> parameters = section.getSectionHeaderOptions();
            String busSettingsRegex = "^MASTER (DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<subsysnumber>[0-9]+)\\s*,.*DPADDRESS\\s+(?<address>[0-9]+)$";
            Optional<String> busSettingString = parameters.stream().filter(line -> line.matches(busSettingsRegex)).findFirst();
            busSettingString.ifPresent(str -> {
                Matcher m2 = Pattern.compile(busSettingsRegex).matcher(str);

                if (m2.matches()) {
                    // Yeeeaaaahhh!! Module is a master
                    Integer underlyingSubsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                    Integer underlyingSubsysAddress = Integer.parseInt(m2.group("address"));
                    res.setAddress(underlyingSubsysAddress);
                    res.setBusRole(BusRole.ROLE_MASTER);

                    Subnet underLyingSubnet = station.getSubnets().get(underlyingSubsysNumber);

                    if (underLyingSubnet == null) {
                        System.err.println("Trying to add a device to a non-existent subsystem: " + res);
                    } else {
                        underLyingSubnet.attachNode(underlyingSubsysAddress, res);
                        res.setSubnet(underLyingSubnet);
                    }
                }
            });

            res.parseConfigurationData(section.getConfigurationData());

            return res;
        } else {
            throw new S7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getSectionHeader());
        }
    }

    public void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }

    public void setAddress(Integer address) {
        this.address = address;
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
                "slotNumber=" + slotNumber +
                ", orderNumber='" + orderNumber + '\'' +
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

}
