package s7hw;

import com.google.gson.internal.LinkedTreeMap;
import s7hw.cfgfile.CfgFileSection;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Subnet {

    public static String SECTION_HEADER_REGEXP = "^(?<type>DPSUBSYSTEM|IOSUBSYSTEM)\\s+(?<number>[0-9]+)\\s*,\\s*\\\"(?<name>.+?)\\\"\\s*$";

    public enum SubnetType { NOT_IMPLEMENTED, PROFIBUS_DP, INDUSTRIAL_ETHERNET }

    private SubnetType subnetType = SubnetType.NOT_IMPLEMENTED;
    private String name;
    private Integer number;
    private Map<String, String> subnetData = new HashMap<>();
    private Map<Integer, Module> nodes = new LinkedTreeMap<>();

    public Subnet(SubnetType type, String name, Integer number) {
        this.subnetType = type;
        this.name = name;
        this.number = number;
    }

    /**
     * Creates a Module object based on an adequate config file section.
     *
     * @param section
     * @return
     * @throws S7HWCfgFileSectionFormatErrorException
     */
    public static Subnet fromSectionData(CfgFileSection section, Station station) throws S7HWCfgFileSectionFormatErrorException {

        Matcher m = Pattern.compile(SECTION_HEADER_REGEXP).matcher(section.getSectionHeader());

        if (m.matches()) {
            String typeString = m.group("type");
            Integer number = Integer.parseInt(m.group("number"));
            String name = m.group("name");

            SubnetType type = switch(typeString) {
                case "DPSUBSYSTEM" -> SubnetType.PROFIBUS_DP;
                case "IOSUBSYSTEM" -> SubnetType.INDUSTRIAL_ETHERNET;
                default -> SubnetType.NOT_IMPLEMENTED;
            };

            Subnet res = new Subnet(type, name, number);

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
     * Adds a key-value pair to rack data section.
     *
     * @param key
     * @param value
     */
    public void putData(String key, String value) {
        subnetData.put(key, value);
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
