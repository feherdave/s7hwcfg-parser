package s7hw;

import s7hw.cfgfile.CfgFileSection;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubsystemRack implements Module {

    public enum BusRole { ROLE_UNDEFINED, ROLE_MASTER, ROLE_SLAVE }
    public static String SECTION_HEADER_REGEXP = "^(?<subsystype>DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<subsysno>[0-9]+)\\s*,\\s*DPADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*\\\"(?<type>.+?)\\\"\\s*(?:\\\"(?<version>.+?)\\\")?\\s*,\\s*\\\"(?<comment>.+?)\\\"\\s*$";

    private Integer address;
    private String typeCode;
    private String comment;
    private String version;
    private Map<Integer, SlotModule> modules = new LinkedHashMap<>();
    private Map<String, String> data = new HashMap<>();
    private Subnet subnet;

    private SlotModule.BusRole busRole = SlotModule.BusRole.ROLE_SLAVE;

    public SubsystemRack(Integer address, String typeCode, String version, String comment) {
        this.address = address;
        this.typeCode = typeCode;
        this.version = version;
        this.comment = comment;
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
            String type = m.group("type");
            String version = m.group("version");
            String comment = m.group("comment");

            SubsystemRack res = new SubsystemRack(address, type, version, comment);

            Subnet subnet = station.getSubnets().get(subsysNumber);

            if (subnet == null) {
                System.err.println("Trying to add a device to a non-existent subsystem: " + res.toString());
            } else {
                subnet.attachNode(address, res);
                res.setSubnet(subnet);
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

    private void setSubnet(Subnet subnet) {
        this.subnet = subnet;
    }
}
