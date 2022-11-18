package s7hw.rack;

import s7hw.HWComponent;
import s7hw.module.Module;
import s7hw.module.SlotModule;
import s7hw.Station;
import s7hw.cfgfile.CfgFileSection;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rack extends HWComponent {

    public static String SECTION_HEADER_REGEXP = "^RACK\\s+(?<rackNumber>[0-9]+)\\s*,\\s*\\\"(?<orderNumber>.+?)\\\"\\s*,\\s*\\\"(?<name>.+?)\\\"\\s*$";

    private Integer rackNumber;
    private String rackName;
    private String orderNumber;
    private Map<Integer, SlotModule> slots = new LinkedHashMap<>();
    private Map<String, String> rackData = new HashMap<>();

    public Rack(int rackNumber, String orderNumber, String rackName) {
        this.rackNumber = rackNumber;
        this.orderNumber = orderNumber;
        this.rackName = rackName;
    }

    /**
     * Adds a module to the given slot.
     *
     * @param module
     */
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
            Integer rackNumber = Integer.parseInt(m.group("rackNumber"));
            String orderNumber = m.group("orderNumber");
            String name = m.group("name");

            Rack res = new Rack(rackNumber, orderNumber, name);

            // Parse configuration data
            res.parseConfigurationData(section.getConfigurationData());

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

    /**
     * Gets the module inserted into a slot.
     *
     * @param slotNumber
     * @return Module or null if there is no module in the specified slot.
     */
    public Module getModule(Integer slotNumber) {
        return slots.get(slotNumber);
    }

    @Override
    public String toString() {
        return "Rack{" +
                "number=" + rackNumber +
                ", name='" + rackName + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                '}';
    }
}
