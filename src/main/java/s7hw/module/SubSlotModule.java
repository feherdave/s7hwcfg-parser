package s7hw.module;

import s7hw.HWComponent;
import s7hw.Station;
import s7hw.Subnet;
import s7hw.cfgfile.CfgFileSection;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubSlotModule extends HWComponent implements Module {

    public enum BusRole { ROLE_UNDEFINED, ROLE_MASTER, ROLE_SLAVE }
    public static String SECTION_HEADER_REGEXP = "^RACK\\s+(?<rackNumber>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotNumber>[0-9]+)\\s*,\\s*SUBSLOT\\s+(?<subslotNumber>[0-9]+)\\s*,\\s*\\\"(?<orderNumber>.+?)\\\"\\s*(?:\\\"(?<version>.+?)\\\")?\\s*,\\s*\\\"(?<name>.+?)\\\"\\s*$";

    private Integer rackNumber;
    private Integer slotNumber;
    private Integer subslotNumber;

    /** In case of this subslot module is a bus interface. */
    private Integer address;
    private String orderNumber;
    private String name;
    private String version;
    private Map<String, String> data = new HashMap<>();

    /** In case of this subslot module is a bus participant. */
    private Subnet subnet;

    /** In case of this subslot module is a bus participant. */
    private BusRole busRole = BusRole.ROLE_UNDEFINED;

    public SubSlotModule(Integer rackNumber, Integer slotNumber, Integer subslotNumber, String orderNumber, String version, String name) {
        this.rackNumber = rackNumber;
        this.slotNumber = slotNumber;
        this.subslotNumber = subslotNumber;
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
    public static SubSlotModule fromSectionData(CfgFileSection section, Station station) throws S7HWCfgFileSectionFormatErrorException {

        Matcher m = Pattern.compile(SECTION_HEADER_REGEXP).matcher(section.getSectionHeader());

        if (m.matches()) {
            Integer rackNumber = Integer.parseInt(m.group("rackNumber"));
            Integer slotNumber = Integer.parseInt(m.group("slotNumber"));
            Integer subslotNumber = Integer.parseInt(m.group("subslotNumber"));
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String name = m.group("name");

            SubSlotModule res = new SubSlotModule(rackNumber, slotNumber, subslotNumber, orderNumber, version, name);

            // Check additional data (e.g. the module is a bus participant)
            List<String> parameters = section.getConfigurationData().stream().takeWhile(s -> !s.equals("BEGIN")).toList();
            String busSettingsRegex = "^MASTER (DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<subsysnumber>[0-9]+)\\s*,.*DPADDRESS\\s+(?<address>[0-9]+)$";
            Optional<String> busSettingString = parameters.stream().filter(line -> line.matches(busSettingsRegex)).findFirst();

            busSettingString.ifPresent(str -> {
                Matcher m2 = Pattern.compile(busSettingsRegex).matcher(str);

                if (m2.matches()) {
                    // Yeeeaaaahhh!! Module is a master
                    Integer subsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                    Integer address = Integer.parseInt(m2.group("address"));
                    res.setAddress(address);
                    res.setBusRole(BusRole.ROLE_MASTER);

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

    public Integer getSubslotNumber() {
        return subslotNumber;
    }

    public Integer getRackNumber() {
        return rackNumber;
    }

    @Override
    public String toString() {
        return "Module{" +
                "rackNumber=" + rackNumber +
                ", slotNumber=" + slotNumber +
                ", subslotNumber=" + subslotNumber +
                ", orderNumber='" + orderNumber + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    private void setAddress(Integer address) {
        this.address = address;
    }

    private void setBusRole(BusRole role) {
        this.busRole = role;
    }

    private void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }

    @Override
    public void addModule(Integer slotNumber, Module module) {
        throw new UnsupportedOperationException("No more modules can be added to a subslot.");
    }

}
