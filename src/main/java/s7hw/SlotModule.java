package s7hw;

import s7hw.cfgfile.CfgFileSection;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SlotModule implements Module {

    public enum BusRole { ROLE_UNDEFINED, ROLE_MASTER, ROLE_SLAVE }
    public static String SECTION_HEADER_REGEXP = "^RACK\\s+(?<rackno>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotno>[0-9]+)\\s*,\\s*\\\"(?<type>.+?)\\\"\\s*(?:\\\"(?<version>.+?)\\\")?\\s*,\\s*\\\"(?<name>.+?)\\\"\\s*$";

    private Integer rackNumber;
    private Integer slotNumber;
    private Map<Integer, SlotModule> subModules = new LinkedHashMap<>();
    private String typeCode;
    private String name;
    private String version;
    private Map<String, String> data = new HashMap<>();
    private Subnet subnet;

    private BusRole busRole = BusRole.ROLE_UNDEFINED;

    public SlotModule(Integer rackNumber, Integer slotNumber, String typeCode, String version, String name) {
        this.rackNumber = rackNumber;
        this.slotNumber = slotNumber;
        this.typeCode = typeCode;
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
            Integer rackNumber = Integer.parseInt(m.group("rackno"));
            Integer slotNumber = Integer.parseInt(m.group("slotno"));
            String type = m.group("type");
            String version = m.group("version");
            String name = m.group("name");

            SlotModule res = new SlotModule(rackNumber, slotNumber, type, version, name);

            // Check additional data (e.g. the module is a bus participant)
            Matcher m2 = Pattern.compile("^MASTER (?<subsystype>DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<subsysnumber>[0-9]+)\\s*,.*DPADDRESS\\s+(?<address>[0-9])$").matcher(section.getSectionData().get(0));

            if (m2.matches()) {
                // Yeeeaaaahhh!! Module is a master
                String subsysType = m2.group("subsystype");
                Integer subsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                Integer address = Integer.parseInt(m2.group("address"));
                res.setBusRole(BusRole.ROLE_MASTER);

                Subnet subnet = station.getSubnets().get(subsysNumber);

                if (subnet == null) {
                    System.err.println("Trying to add a device to a non-existent subsystem: " + res.toString());
                } else {
                    subnet.attachNode(address, res);
                    res.setSubnet(subnet);
                }
            }

            // Store data part
            section
                    .getSectionData()
                    .stream()
                    .dropWhile(data -> data.matches("BEGIN"))
                    .takeWhile(data -> !data.matches("END"))
                    .forEach(line -> {
                        Pattern dataKeyValuePair = Pattern.compile("^(?<key>\\w+)\\s+\\\"(?<value>\\w*)\\\"$");
                        Matcher dataKeyValuePairMatcher = dataKeyValuePair.matcher(line);

                        if (dataKeyValuePairMatcher.matches()) {
                            res.putData(dataKeyValuePairMatcher.group("key"), dataKeyValuePairMatcher.group("value"));
                        }
                    });

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

    @Override
    public String toString() {
        return "Module{" +
                "rackNumber=" + rackNumber +
                ", slotNumber=" + slotNumber +
                ", typeCode='" + typeCode + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                '}';
    }

    private void setBusRole(BusRole role) {
        this.busRole = role;
    }

    public Map<Integer, SlotModule> getSubModules() {
        return subModules;
    }

    private void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }
}
