package s7hw;

import s7hw.cfgfile.CfgFileSection;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rack {

    public static String SECTION_HEADER_REGEXP = "^RACK\\s+(?<no>[0-9]+)\\s*,\\s*\\\"(?<type>.+?)\\\"\\s*,\\s*\\\"(?<name>.+?)\\\"\\s*$";

    private Integer rackNumber;
    private String rackName;
    private String typeCode;
    private Map<Integer, SlotModule> slots = new LinkedHashMap<>();
    private Map<String, String> rackData = new HashMap<>();

    public Rack(int rackNumber, String typeCode, String rackName) {
        this.rackNumber = rackNumber;
        this.typeCode = typeCode;
        this.rackName = rackName;
    }

    public void addModule(SlotModule module) {
        slots.put(module.getSlotNumber(), module);
    }

    public Integer getRackNumber() {
        return rackNumber;
    }

    /**
     * Creates a Rack object based on an adequate config file section.
     *
     * @param section
     * @return
     * @throws S7HWCfgFileSectionFormatErrorException
     */
    public static Rack fromSectionData(CfgFileSection section, Station station) throws S7HWCfgFileSectionFormatErrorException {

        Matcher m = Pattern.compile(SECTION_HEADER_REGEXP).matcher(section.getSectionHeader());

        if (m.matches()) {
            Integer rackNumber = Integer.parseInt(m.group("no"));
            String type = m.group("type");
            String name = m.group("name");

            Rack res = new Rack(rackNumber, type, name);

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
        rackData.put(key, value);
    }

    /**
     * Inserts a module into the given slot.
     *
     * @param slotNumber
     * @param module
     */
    public void addModule(Integer slotNumber, SlotModule module) {
        slots.put(slotNumber, module);
    }

    @Override
    public String toString() {
        return "Rack{" +
                "number=" + rackNumber +
                ", name='" + rackName + '\'' +
                ", type='" + typeCode + '\'' +
                '}';
    }
}
