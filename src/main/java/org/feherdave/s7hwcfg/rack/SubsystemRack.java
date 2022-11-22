package org.feherdave.s7hwcfg.rack;

import org.feherdave.s7hwcfg.HWComponent;
import org.feherdave.s7hwcfg.Station;
import org.feherdave.s7hwcfg.cfgfile.CfgFileSection;
import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileSectionFormatErrorException;
import org.feherdave.s7hwcfg.module.Module;
import org.feherdave.s7hwcfg.module.SlotModule;
import org.feherdave.s7hwcfg.Subnet;
import org.feherdave.s7hwcfg.module.SubsystemRackSlotModule;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubsystemRack extends HWComponent implements Module {

    public enum BusRole { ROLE_UNDEFINED, ROLE_MASTER, ROLE_SLAVE }
    public static String SECTION_HEADER_REGEXP = "^(?<subsystype>DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<subsysno>[0-9]+)\\s*,\\s*DPADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<designation>.+?)\"\\s*$";

    private Integer address;
    private String orderNumber;
    private String designation;
    private String version;
    private Map<Integer, SubsystemRackSlotModule> modules = new LinkedHashMap<>();
    private Map<String, String> data = new HashMap<>();
    private Subnet subnet;

    private SlotModule.BusRole busRole = SlotModule.BusRole.ROLE_SLAVE;

    public SubsystemRack(Integer address, String orderNumber, String version, String designation) {
        this.address = address;
        this.orderNumber = orderNumber;
        this.version = version;
        this.designation = designation;
    }

    public Integer getAddress() {
        return address;
    }

    /**
     * Creates a Module object based on an adequate config file section.
     *
     * @param section
     * @return
     * @throws S7HWCfgFileSectionFormatErrorException
     */
    public static SubsystemRack fromSectionData(CfgFileSection section, Station station) throws S7HWCfgFileSectionFormatErrorException {

        Matcher m = Pattern.compile(SECTION_HEADER_REGEXP).matcher(section.getSectionHeader());

        if (m.matches()) {
            Integer subsysNumber = Integer.parseInt(m.group("subsysno"));
            Integer address = Integer.parseInt(m.group("address"));
            String subsystype = m.group("subsystype");
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String designation = m.group("designation");

            SubsystemRack res = new SubsystemRack(address, orderNumber, version, designation);

            Subnet subnet = station.getSubnets().get(subsysNumber);

            if (subnet == null) {
                System.err.println("Trying to add a device to a non-existent subsystem: " + res);
            } else {
                subnet.attachNode(address, res);
                res.setSubnet(subnet);
            }

            // Parse configuration data
            res.parseConfigurationData(section.getConfigurationData());

            return res;
        } else {
            throw new S7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getSectionHeader());
        }
    }


    @Override
    public void addModule(Integer slotNumber, Module module) {
        if (module instanceof SubsystemRackSlotModule) {
            modules.put(slotNumber, (SubsystemRackSlotModule) module);
        }
    }

    @Override
    public Module getModule(Integer slotNumber) {
        return modules.get(slotNumber);
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

    private void setSubnet(Subnet subnet) {
        this.subnet = subnet;
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
