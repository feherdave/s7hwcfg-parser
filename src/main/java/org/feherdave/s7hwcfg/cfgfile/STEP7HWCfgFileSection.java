package org.feherdave.s7hwcfg.cfgfile;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class representing a section in the config file.
 * The following sections are recognized:
 *
 *      STATION
 *      RACK
 *      RACK A, SLOT B
 *      RACK A, SLOT B, SUBSLOT C
 *      DPSUBSYSTEM A
 *      DPSUBSYSTEM A, DPADDRESS B
 *      DPSUBSYSTEM A, DPADDRESS B, SLOT C
 *      DPSUBSYSTEM A, DPADDRESS B, SLOT C, SUBSLOT D
 *      IOSUBSYSTEM A, IOADDRESS B
 *      IOSUBSYSTEM A, IOADDRESS B, SLOT C
 *      IOSUBSYSTEM A, IOADDRESS B, SLOT C, SUBSLOT D
 *
 * The section data is interpreted as follows (e.g.):
 *
 * +----------------+-----------+-----------------------------------------------------------+
 * | Section head   | Title     | RACK 0, SLOT 4, "6GK7 443-5DX03-0XE0", "CP 443-5 Ext"     |
 * |                | Options   | MASTER DPSUBSYSTEM 1, "PROFIBUS(1)", DPADDRESS 2          |
 * |                |           | ...                                                       |
 * +----------------+-----------+-----------------------------------------------------------+
 * |                            | BEGIN                                                     |
 * +----------------------------+-----------------------------------------------------------+
 * | Section body               |   ASSET_ID "..."                                          |
 * |                            |   PROFIBUSADDRESS "2"                                     |
 * |                            |   LINKED_SUBNETNAME "PROFIBUS(1)"                         |
 * |                            |                                                           |
 * |                            |   ...                                                     |
 * |                            |                                                           |
 * |                            |   COMMENT ""                                              |
 * +----------------------------+-----------------------------------------------------------+
 * |                            | END                                                       |
 * +----------------------------+-----------------------------------------------------------+
 *
 */
public class STEP7HWCfgFileSection {

    public enum SectionType {
        UNKNOWN,
        STATION,
        RACK,
        RACK_SLOT,
        RACK_SLOT_SUBSLOT,
        DPSUBSYS,
        DPSUBSYS_DPADDR,
        DPSUBSYS_DPADDR_SLOT,
        DPSUBSYS_DPADDR_SLOT_SUBSLOT,
        IOSUBSYS,
        IOSUBSYS_IOADDR,
        IOSUBSYS_IOADDR_SLOT,
        IOSUBSYS_IOADDR_SLOT_SUBSLOT
    }

    public static String SECTHEAD_REGEXP_STATION = "^STATION\\s+(?<stationtype>[A-Z0-9]+)\\s*,\\s*\"(?<stationname>.*?)\"\\s*$";
    public static String SECTHEAD_REGEXP_RACK = "^RACK\\s+(?<rackNumber>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_RACK_SLOT = "^RACK\\s+(?<rackNumber>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotNumber>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_RACK_SLOT_SUBSLOT = "^RACK\\s+(?<rackNumber>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotNumber>[0-9]+)\\s*,\\s*SUBSLOT\\s+(?<subslotNumber>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_DPSUBSYSTEM = "^DPSUBSYSTEM\\s+(?<number>[0-9]+)\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_DPSUBSYS_DPADDR = "^DPSUBSYSTEM\\s+(?<subsysno>[0-9]+)\\s*,\\s*DPADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<designation>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_DPSUBSYS_DPADDR_SLOT = "^DPSUBSYSTEM\\s+(?<subsysno>[0-9]+)\\s*,\\s*DPADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotno>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_DPSUBSYS_DPADDR_SLOT_SUBSLOT = "^DPSUBSYSTEM\\s+(?<subsysno>[0-9]+)\\s*,\\s*DPADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotno>[0-9]+)\\s*,\\s*SUBSLOT\\s+(?<subslotNumber>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_IOSUBSYSTEM = "^IOSUBSYSTEM\\s+(?<number>[0-9]+)\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_IOSUBSYS_IOADDR = "^IOSUBSYSTEM\\s+(?<subsysno>[0-9]+)\\s*,\\s*IOADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<designation>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_IOSUBSYS_IOADDR_SLOT = "^IOSUBSYSTEM\\s+(?<subsysno>[0-9]+)\\s*,\\s*IOADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotno>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";
    public static String SECTHEAD_REGEXP_IOSUBSYS_DPADDR_SLOT_SUBSLOT = "^IOSUBSYSTEM\\s+(?<subsysno>[0-9]+)\\s*,\\s*DPADDRESS\\s+(?<address>[0-9]+)\\s*,\\s*SLOT\\s+(?<slotno>[0-9]+)\\s*,\\s*SUBSLOT\\s+(?<subslotNumber>[0-9]+)\\s*,\\s*\"(?<orderNumber>.+?)\"\\s*(?:\"(?<version>.+?)\")?\\s*,\\s*\"(?<name>.+?)\"\\s*$";

    private SectionType sectionType = SectionType.UNKNOWN;
    private List<String> sectionHead;
    private List<String> sectionBody;

    /**
     * Creates a new configuration data section.
     *
     * @param configurationData
     */
    public STEP7HWCfgFileSection(List<String> configurationData) {
        List<String> data = configurationData.stream()
                .dropWhile(line -> line.isEmpty() || line.isBlank())
                .takeWhile(line -> !line.isEmpty() && !line.isBlank())
                .collect(Collectors.toList());

        // Extract section header (line until BEGIN)
        this.sectionHead = data.stream()
                .takeWhile(line -> !line.equals("BEGIN"))
                .collect(Collectors.toList());

        // Extract section config data (lines between BEGIN and END)
        this.sectionBody = data.stream()
                .dropWhile(line -> !line.equals("BEGIN"))
                .collect(Collectors.toList());

        // Determine section type
        Pattern typeCheckPattern = Pattern.compile("^(?<type>STATION|RACK|DPSUBSYSTEM|IOSUBSYSTEM)\\s+\\w+\\s*(?:,\\s*(?<para1>DPADDRESS|IOADDRESS)\\s+[0-9]+\\s*)?(?:,\\s*(?<para2>SLOT)\\s+[0-9]+\\s*)?(?:,\\s*(?<para3>SUBSLOT)\\s+[0-9]+)?.*");
        Matcher m = typeCheckPattern.matcher(getTitle());

        if (m.matches()) {
            switch (m.group("type")) {
                case "STATION":
                    sectionType = SectionType.STATION;
                    break;

                case "RACK":
                    if (m.group("para1") == null && m.group("para2") == null && m.group("para3") == null) {
                        sectionType = SectionType.RACK;
                    } else {
                        if (m.group("para3") == null) {
                            sectionType = SectionType.RACK_SLOT;
                        } else {
                            sectionType = SectionType.RACK_SLOT_SUBSLOT;
                        }
                    }
                    break;

                case "DPSUBSYSTEM":
                    if (m.group("para1") != null) {
                        if (m.group("para2") != null) {
                            if (m.group("para3") != null) {
                                sectionType = SectionType.DPSUBSYS_DPADDR_SLOT_SUBSLOT;
                            } else {
                                sectionType = SectionType.DPSUBSYS_DPADDR_SLOT;
                            }
                        } else {
                            sectionType = SectionType.DPSUBSYS_DPADDR;
                        }
                    } else {
                        sectionType = SectionType.DPSUBSYS;
                    }
                    break;

                case "IOSUBSYSTEM":
                    if (m.group("para1") != null) {
                        if (m.group("para2") != null) {
                            if (m.group("para3") != null) {
                                sectionType = SectionType.IOSUBSYS_IOADDR_SLOT_SUBSLOT;
                            } else {
                                sectionType = SectionType.IOSUBSYS_IOADDR_SLOT;
                            }
                        } else {
                            sectionType = SectionType.IOSUBSYS_IOADDR;
                        }
                    } else {
                        sectionType = SectionType.IOSUBSYS;
                    }
                    break;
            }
        }

    }

    /**
     * Get all lines of section body.
     *
     * @return
     */
    public List<String> getBody() {
        return sectionBody;
    }

    /**
     * Get title of section (first line of section head).
     *
     * @return
     */
    public String getTitle() {
        return sectionHead.get(0);
    }

    /**
     * Get all lines of section head.
     *
     * @return
     */
    public List<String> getHead() {
        return sectionHead;
    }

    /**
     * Get options data of section head (section header lines excluding the first line).
     *
     * @return
     */
    public List<String> getHeadOptions() {
        return sectionHead.subList(1, sectionHead.size());
    }

    /**
     * Get section type.
     *
     * @return
     */
    public SectionType getSectionType() {
        return sectionType;
    }
}
