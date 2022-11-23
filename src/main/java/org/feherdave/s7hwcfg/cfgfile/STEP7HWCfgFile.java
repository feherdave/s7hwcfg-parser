package org.feherdave.s7hwcfg.cfgfile;

import org.feherdave.s7hwcfg.s7.Station;
import org.feherdave.s7hwcfg.s7.StationBuilder;
import org.feherdave.s7hwcfg.s7.hw.module.*;
import org.feherdave.s7hwcfg.s7.system.Subsystem;
import org.feherdave.s7hwcfg.s7.hw.rack.Rack;
import org.feherdave.s7hwcfg.s7.hw.rack.SubsystemRack;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class STEP7HWCfgFile {

    enum FileFormat { READABLE, COMPACT }

    private String fileVersion;
    private FileFormat format = FileFormat.READABLE;
    private Map<String, String> metaData = new HashMap<>();
    private List<STEP7HWCfgFileSection> sections = new ArrayList<>();
    private StationBuilder actualStationBuilder;

    public STEP7HWCfgFile(File file) throws STEP7HWCfgFileFormatException, IOException {

        try (Scanner scanner = new Scanner(file)) {

            if (!scanner.hasNext()) {
                throw new STEP7HWCfgFileFormatException("File too short (line count 0).");
            }

            List<String> headerLines = new ArrayList<>();
            List<String> sectionLines = new ArrayList<>();

            // Read lines
            if (scanner.hasNext()) {

                // Read header
                boolean contentStartFound = false;
                String line;

                do {
                    line = scanner.nextLine();

                    contentStartFound = line.startsWith("STATION");

                    if (!contentStartFound) {
                        headerLines.add(line);
                    }
                } while (scanner.hasNext() && !contentStartFound);

                if (!contentStartFound) {
                    throw new STEP7HWCfgFileFormatException("STATION section missing.");
                }

                // Read content

                do {
                    sectionLines.add(line);

                    if (scanner.hasNext()) {
                        line = scanner.nextLine();
                    }
                } while (scanner.hasNext());

                parseHeader(headerLines);
                readSections(sectionLines);
            }
        }
    }

    /**
     * Parse header lines.
     *
     * @param headerLines
     * @throws STEP7HWCfgFileFormatException
     */
    private void parseHeader(List<String> headerLines) throws STEP7HWCfgFileFormatException {
        String fileVersionRegex = "^FILEVERSION \\\"([a-zA-Z0-9]+\\.[a-zA-Z0-9]+)\\\"$";
        String metaDataRegex = "^\\#(?<metatag>[A-Z0-9_]+)\\s(?<metadata>.+)$";
        String compactFormatRegex = "^FORMAT\\sCOMPACT$";

        // Read file version
        Pattern fileVersionPattern = Pattern.compile(fileVersionRegex);
        String fileVersionLine = headerLines.stream().filter(line -> line.matches(fileVersionRegex)).findFirst().orElseThrow(() -> new STEP7HWCfgFileFormatException("FILEVERSION entry missing from file"));

        Matcher ma =  fileVersionPattern.matcher(fileVersionLine);
        ma.matches();
        this.fileVersion = ma.group(1);

        // Collect metadata
        Pattern p = Pattern.compile(metaDataRegex);

        headerLines.stream()
                .filter(line -> line.matches(metaDataRegex))
                .forEach(line -> {
                            Matcher m = p.matcher(line);

                            if (m.matches()) {
                                metaData.put(m.group("metatag"), m.group("metadata"));
                            }
                        }
                    );

        // Check format
        if (headerLines.stream().anyMatch(line -> line.matches(compactFormatRegex))) {
            this.format = FileFormat.COMPACT;
        }
    }

    /**
     * Read sections.
     *
     * @param sectionLines
     * @throws STEP7HWCfgFileFormatException
     */
    private void readSections(List<String> sectionLines) throws STEP7HWCfgFileFormatException {
        Iterator<String> iter = sectionLines.iterator();

        if (iter.hasNext()) {
            List<String> sectionStringData = new ArrayList<>();

            do {
                String line = iter.next();

                if (!line.isEmpty()) {
                    sectionStringData.add(line.trim());
                } else {
                    if (!sectionStringData.get(sectionStringData.size() - 1).equals("END")) {
                        throw new STEP7HWCfgFileFormatException("END missing in the following section: " + String.join("\n", sectionStringData));
                    }

                    sections.add(new STEP7HWCfgFileSection(sectionStringData));
                    sectionStringData = new ArrayList<>();
                }
            } while (iter.hasNext());
        } else {
            throw new STEP7HWCfgFileFormatException("STATION section doesn't follow rules");
        }
    }

    /**
     * Get file version.
     *
     * @return
     */
    public String getFileVersion() {
        return fileVersion;
    }

    /**
     * Get file format (compact or readable).
     *
     * @return FileFormat
     */
    public FileFormat getFileFormat() {
        return format;
    }

    /**
     * Get metadata of HW config file (line starting with #).
     *
     * @return Map<String, String> Metadata converted to a map.
     */
    public Map<String, String> getMetaData() {
        return metaData;
    }

    /**
     * Get list of sections.
     *
     * @return List<CfgFileSection>
     */
    public List<STEP7HWCfgFileSection> getSections() {
        return sections;
    }

    /**
     * Parses sections and builds HW config objects.
     *
     * @return Station The main object of S7 hardware configuration.
     */
    public Station parseSections() throws STEP7HWCfgFileSectionFormatErrorException {

        // Parse station data
        for (STEP7HWCfgFileSection section : sections) {
            if (section.getSectionType() == STEP7HWCfgFileSection.SectionType.STATION) {
                actualStationBuilder = parseStationSection(section);
                break;
            }
        }

        // Add racks
        for (STEP7HWCfgFileSection section : sections) {
            if (section.getSectionType() == STEP7HWCfgFileSection.SectionType.RACK) {
                actualStationBuilder.addRack(parseRackSection(section));
            }
        }

        // Add subnets
        for (STEP7HWCfgFileSection section : sections) {
            if (section.getSectionType() == STEP7HWCfgFileSection.SectionType.DPSUBSYS ||
                section.getSectionType() == STEP7HWCfgFileSection.SectionType.IOSUBSYS) {
                actualStationBuilder.addSubnet(parseSubnetSection(section));
            }
        }

        // Add modules to racks
        for (STEP7HWCfgFileSection section : sections) {
            if (section.getSectionType() == STEP7HWCfgFileSection.SectionType.RACK_SLOT) {
                SlotModule sm = parseRackSlotModuleSection(section);

                // Add module to parent rack
                actualStationBuilder.getRacks().get(sm.getRackNumber()).addModule(sm);
            }
        }

        // Add submodules to modules
        for (STEP7HWCfgFileSection section : sections) {
            if (section.getSectionType() == STEP7HWCfgFileSection.SectionType.RACK_SLOT_SUBSLOT) {
                SubSlotModule ssm = parseRackSubSlotModuleSection(section);

                // Add submodule to parent module
                actualStationBuilder.getRacks().get(ssm.getRackNumber()).getModule(ssm.getSlotNumber()).addModule(ssm.getSubslotNumber(), ssm);
            }
        }

        // Add subsystem racks
        for (STEP7HWCfgFileSection section : sections) {
            if (section.getSectionType() == STEP7HWCfgFileSection.SectionType.DPSUBSYS_DPADDR ||
                    section.getSectionType() == STEP7HWCfgFileSection.SectionType.IOSUBSYS_IOADDR) {
                SubsystemRack ssr = parseSubsystemRackSection(section);

                actualStationBuilder.addSubsystemRack(ssr);
            }
        }

        // Add modules to subsystem racks
        for (STEP7HWCfgFileSection section : sections) {
            if (section.getSectionType() == STEP7HWCfgFileSection.SectionType.DPSUBSYS_DPADDR_SLOT ||
                    section.getSectionType() == STEP7HWCfgFileSection.SectionType.IOSUBSYS_IOADDR_SLOT) {
                SubsystemRackSlotModule ssrsm = parseSubsystemRackSlotModuleSection(section);

                // Add module to corresponding subsystem rack
                actualStationBuilder.getSubsystemRacks().stream()
                        .filter(subsystemRack ->
                                (subsystemRack.getSubsystemMemberShip().getSubsystem().getNumber() == ssrsm.getSubsystemNumber() &&
                                        subsystemRack.getSubsystemMemberShip().getAddress() == ssrsm.getAddress()
                                 )).findFirst().ifPresent(subsystemRack -> subsystemRack.addModule(ssrsm.getSlotNumber(), ssrsm));
            }
        }


        return actualStationBuilder.build();
    }

    /**
     * Parse station data.
     *
     * @param section
     * @throws STEP7HWCfgFileSectionFormatErrorException
     */
    private StationBuilder parseStationSection(STEP7HWCfgFileSection section) throws STEP7HWCfgFileSectionFormatErrorException {

        StationBuilder res = Station.builder();

        Pattern p = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_STATION);

        Matcher m = p.matcher(section.getTitle());

        // Check header
        if (m.matches()) {

            Station.StationType stationType;

            // Store data from header
            switch (m.group("stationtype")) {
                case "S7400":
                    stationType = Station.StationType.S7_400;
                    break;
                case "S7300":
                    stationType = Station.StationType.S7_300;
                    break;
                default:
                    stationType = Station.StationType.NOT_IMPLEMENTED;
            }

            res.type(stationType).name(m.group("stationname")).configData(section.getBody());
        } else {
            throw new STEP7HWCfgFileSectionFormatErrorException("Format error in STATION section.");
        }

        return res;
    }

    /**
     * Parses a section containing rack data.
     *
     * @param section
     * @return Rack object.
     */
    private Rack parseRackSection(STEP7HWCfgFileSection section) throws STEP7HWCfgFileSectionFormatErrorException {

        Matcher m = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_RACK).matcher(section.getTitle());

        if (m.matches()) {
            Integer rackNumber = Integer.parseInt(m.group("rackNumber"));
            String orderNumber = m.group("orderNumber");
            String name = m.group("name");

            Rack res = new Rack(rackNumber, orderNumber, name);

            // Parse configuration data
            try {
                res.parseConfigurationData(section.getBody());
            } catch (STEP7HWCfgFileSectionFormatErrorException e) {
                throw new STEP7HWCfgFileSectionFormatErrorException("Format error in section '" + section.getTitle() + "': " + e.getMessage());
            }

            return res;
        } else {
            throw new STEP7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getTitle());
        }
    }

    /**
     * Parses a section containing rack slotmodule data.
     *
     * @param section
     * @return SlotModule
     */
    private SlotModule parseRackSlotModuleSection(STEP7HWCfgFileSection section) throws STEP7HWCfgFileSectionFormatErrorException {
        Matcher m = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_RACK_SLOT).matcher(section.getTitle());

        if (m.matches()) {
            Integer rackNumber = Integer.parseInt(m.group("rackNumber"));
            Integer slotNumber = Integer.parseInt(m.group("slotNumber"));
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String name = m.group("name");

            SlotModule res = new SlotModule(rackNumber, slotNumber, orderNumber, version, name);

            // Check additional data (e.g. the module is a bus participant)
            List<String> parameters = section.getHeadOptions();
            String dpBusSettingsRegex = "^MASTER DPSUBSYSTEM\\s+(?<subsysnumber>[0-9]+)\\s*,.*DPADDRESS\\s+(?<address>[0-9]+)$";
            Optional<String> dpBusSettingString = parameters.stream().filter(line -> line.matches(dpBusSettingsRegex)).findFirst();

            dpBusSettingString.ifPresentOrElse(str -> {
                Matcher m2 = Pattern.compile(dpBusSettingsRegex).matcher(str);

                if (m2.matches()) {
                    // Yeeeaaaahhh!! Module is a master
                    Integer subsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                    Integer address = Integer.parseInt(m2.group("address"));

                    Subsystem subsystem = actualStationBuilder.getSubnets().get(subsysNumber);

                    if (subsystem == null) {
                        System.err.println("Trying to add a device to a non-existent subsystem: " + res);
                    } else {
                        res.setSubsystemMemberShip(new DPSubsystemMemberShip(subsystem, address, DPSubsystemMemberShip.Role.MASTER));

                        subsystem.attachNode(address, res);
                    }
                }
            }, () -> {

                        String ioBusSettingsRegex = "^CONTROLLER IOSUBSYSTEM\\s+(?<subsysnumber>[0-9]+)\\s*,.*IOADDRESS\\s+(?<address>[0-9]+)$";
                        Optional<String> ioBusSettingString = parameters.stream().filter(line -> line.matches(ioBusSettingsRegex)).findFirst();

                        ioBusSettingString.ifPresent(str -> {
                            Matcher m2 = Pattern.compile(dpBusSettingsRegex).matcher(str);

                            if (m2.matches()) {
                                // Yeeeaaaahhh!! Module is a controller
                                Integer subsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                                Integer address = Integer.parseInt(m2.group("address"));

                                Subsystem subsystem = actualStationBuilder.getSubnets().get(subsysNumber);

                                if (subsystem == null) {
                                    System.err.println("Trying to add a device to a non-existent subsystem: " + res);
                                } else {
                                    res.setSubsystemMemberShip(new PNIOSubsystemMemberShip(subsystem, address, PNIOSubsystemMemberShip.Role.CONTROLLER));

                                    subsystem.attachNode(address, res);
                                }
                            }
                        });
                    });

            // Parse configuration data
            res.parseConfigurationData(section.getBody());

            return res;
        } else {
            throw new STEP7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getTitle());
        }
    }

    /**
     * Parses a section containing rack subslot module data.
     *
     * @param section
     * @return SubSlotModule
     */
    private SubSlotModule parseRackSubSlotModuleSection(STEP7HWCfgFileSection section) throws STEP7HWCfgFileSectionFormatErrorException {
        Matcher m = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_RACK_SLOT_SUBSLOT).matcher(section.getTitle());

        if (m.matches()) {
            Integer rackNumber = Integer.parseInt(m.group("rackNumber"));
            Integer slotNumber = Integer.parseInt(m.group("slotNumber"));
            Integer subSlotNumber = Integer.parseInt(m.group("subslotNumber"));
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String name = m.group("name");

            SubSlotModule res = new SubSlotModule(rackNumber, slotNumber, subSlotNumber, orderNumber, version, name);

            // Check additional data (e.g. the module is a bus participant)
            List<String> headOptions = section.getHeadOptions();
            String dpBusSettingsRegex = "^MASTER DPSUBSYSTEM\\s+(?<subsysnumber>[0-9]+)\\s*,.*DPADDRESS\\s+(?<address>[0-9]+)$";
            Optional<String> dpBusSettingString = headOptions.stream().filter(line -> line.matches(dpBusSettingsRegex)).findFirst();

            dpBusSettingString.ifPresentOrElse(str -> {
                Matcher m2 = Pattern.compile(dpBusSettingsRegex).matcher(str);

                if (m2.matches()) {
                    // Yeeeaaaahhh!! Module is a master
                    Integer subsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                    Integer address = Integer.parseInt(m2.group("address"));

                    Subsystem subsystem = actualStationBuilder.getSubnets().get(subsysNumber);

                    if (subsystem == null) {
                        System.err.println("Trying to add a device to a non-existent subsystem: " + res);
                    } else {
                        res.setSubsystemMemberShip(new DPSubsystemMemberShip(subsystem, address, DPSubsystemMemberShip.Role.MASTER));

                        subsystem.attachNode(address, res);
                    }
                }
            }, () -> {

                String ioBusSettingsRegex = "^CONTROLLER IOSUBSYSTEM\\s+(?<subsysnumber>[0-9]+)\\s*,.*IOADDRESS\\s+(?<address>[0-9]+)$";
                Optional<String> ioBusSettingString = headOptions.stream().filter(line -> line.matches(ioBusSettingsRegex)).findFirst();

                ioBusSettingString.ifPresent(str -> {
                    Matcher m2 = Pattern.compile(ioBusSettingsRegex).matcher(str);

                    if (m2.matches()) {
                        // Yeeeaaaahhh!! Module is a controller
                        Integer subsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                        Integer address = Integer.parseInt(m2.group("address"));

                        Subsystem subsystem = actualStationBuilder.getSubnets().get(subsysNumber);

                        if (subsystem == null) {
                            System.err.println("Trying to add a device to a non-existent subsystem: " + res);
                        } else {
                            res.setSubsystemMemberShip(new PNIOSubsystemMemberShip(subsystem, address, PNIOSubsystemMemberShip.Role.CONTROLLER));

                            subsystem.attachNode(address, res);
                        }
                    }
                });
            });

            // Parse configuration data
            res.parseConfigurationData(section.getBody());

            return res;
        } else {
            throw new STEP7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getTitle());
        }
    }

    /**
     * Parses a section containing subnet data.
     *
     * @param section
     * @return Subnet
     */
    private Subsystem parseSubnetSection(STEP7HWCfgFileSection section) throws STEP7HWCfgFileSectionFormatErrorException {
        Matcher mDP = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_DPSUBSYSTEM).matcher(section.getTitle());

        if (mDP.matches()) {
            Integer number = Integer.parseInt(mDP.group("number"));
            String name = mDP.group("name");
            Subsystem res = new Subsystem(Subsystem.SubnetType.PROFIBUS_DP, name, number);

            // Parse configuration data
            res.parseConfigurationData(section.getBody());

            return res;
        }

        Matcher mPNIO = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_IOSUBSYSTEM).matcher(section.getTitle());

        if (mPNIO.matches()) {
            Integer number = Integer.parseInt(mPNIO.group("number"));
            String name = mPNIO.group("name");
            Subsystem res = new Subsystem(Subsystem.SubnetType.PROFINET, name, number);

            // Parse configuration data
            res.parseConfigurationData(section.getBody());

            return res;
        }

        throw new STEP7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getTitle());
    }

    /**
     * Parses a section containing subsystem rack data.
     *
     * @param section
     * @return SubsystemRack
     */
    private SubsystemRack parseSubsystemRackSection(STEP7HWCfgFileSection section) throws STEP7HWCfgFileSectionFormatErrorException {
        Matcher m = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_DPSUBSYS_DPADDR).matcher(section.getTitle());

        if (m.matches()) {
            Integer subsysNumber = Integer.parseInt(m.group("subsysno"));
            Integer address = Integer.parseInt(m.group("address"));
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String designation = m.group("designation");

            SubsystemRack res = new SubsystemRack(subsysNumber, address, orderNumber, version, designation);

            Subsystem subsystem = actualStationBuilder.getSubnets().get(subsysNumber);

            if (subsystem == null) {
                System.err.println("Trying to add a device to a non-existent subsystem: " + res);
            } else {
                subsystem.attachNode(address, res);
                res.setSubsystemMemberShip(new DPSubsystemMemberShip(subsystem, address, DPSubsystemMemberShip.Role.SLAVE));
            }

            // Parse configuration data
            res.parseConfigurationData(section.getBody());

            return res;
        } else {
            m = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_IOSUBSYS_IOADDR).matcher(section.getTitle());

            if (m.matches()) {
                Integer subsysNumber = Integer.parseInt(m.group("subsysno"));
                Integer address = Integer.parseInt(m.group("address"));
                String orderNumber = m.group("orderNumber");
                String version = m.group("version");
                String designation = m.group("designation");

                SubsystemRack res = new SubsystemRack(subsysNumber, address, orderNumber, version, designation);

                Subsystem subsystem = actualStationBuilder.getSubnets().get(subsysNumber);

                if (subsystem == null) {
                    System.err.println("Trying to add a device to a non-existent subsystem: " + res);
                } else {
                    subsystem.attachNode(address, res);
                    res.setSubsystemMemberShip(new PNIOSubsystemMemberShip(subsystem, address, PNIOSubsystemMemberShip.Role.DEVICE));
                }

                // Parse configuration data
                res.parseConfigurationData(section.getBody());

                return res;
            }
        }

        throw new STEP7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getTitle());
    }

    /**
     * Parses section data list of subsystem rack slot modules.
     *
     * @param section
     * @return SubsystemRackSlotModule
     */
    private SubsystemRackSlotModule parseSubsystemRackSlotModuleSection(STEP7HWCfgFileSection section) throws STEP7HWCfgFileSectionFormatErrorException {

        SubsystemRackSlotModule res = null;

        Matcher m = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_DPSUBSYS_DPADDR_SLOT).matcher(section.getTitle());

        if (m.matches()) {
            Integer subSysNumber = Integer.parseInt(m.group("subsysno"));
            Integer address = Integer.parseInt(m.group("address"));
            Integer slotNumber = Integer.parseInt(m.group("slotno"));
            String orderNumber = m.group("orderNumber");
            String version = m.group("version");
            String name = m.group("name");

            res = new SubsystemRackSlotModule(subSysNumber, address, slotNumber, orderNumber, version, name);
        } else {
            m = Pattern.compile(STEP7HWCfgFileSection.SECTHEAD_REGEXP_IOSUBSYS_IOADDR_SLOT).matcher(section.getTitle());

            if (m.matches()) {
                Integer subSysNumber = Integer.parseInt(m.group("subsysno"));
                Integer address = Integer.parseInt(m.group("address"));
                Integer slotNumber = Integer.parseInt(m.group("slotno"));
                String orderNumber = m.group("orderNumber");
                String version = m.group("version");
                String name = m.group("name");

                res = new SubsystemRackSlotModule(subSysNumber, address, slotNumber, orderNumber, version, name);
            }
        }

        if (res != null) {
            // Parse configuration data
            res.parseConfigurationData(section.getBody());

            // Check additional data (e.g. the module is a bus participant)
            List<String> parameters = section.getHeadOptions();
            String dpBusSettingsRegex = "^MASTER DPSUBSYSTEM\\s+(?<subsysnumber>[0-9]+)\\s*,.*DPADDRESS\\s+(?<address>[0-9]+)$";
            Optional<String> dpBusSettingString = parameters.stream().filter(line -> line.matches(dpBusSettingsRegex)).findFirst();

            SubsystemRackSlotModule efRes = res;    // "effectively final"
            dpBusSettingString.ifPresentOrElse(str -> {
                Matcher m2 = Pattern.compile(dpBusSettingsRegex).matcher(str);

                if (m2.matches()) {
                    // Yeeeaaaahhh!! Module is a master
                    Integer underlyingSubsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                    Integer underlyingAddress = Integer.parseInt(m2.group("address"));

                    Subsystem subsystem = actualStationBuilder.getSubnets().get(underlyingSubsysNumber);

                    if (subsystem == null) {
                        System.err.println("Trying to add a device to a non-existent subsystem: " + efRes);
                    } else {
                        efRes.setSubsystemMemberShip(new DPSubsystemMemberShip(subsystem, underlyingAddress, DPSubsystemMemberShip.Role.MASTER));

                        subsystem.attachNode(underlyingAddress, efRes);
                    }
                }
            }, () -> {
                String ioBusSettingsRegex = "^CONTROLLER IOSUBSYSTEM\\s+(?<subsysnumber>[0-9]+)\\s*,.*IOADDRESS\\s+(?<address>[0-9]+)$";
                Optional<String> ioBusSettingString = parameters.stream().filter(line -> line.matches(ioBusSettingsRegex)).findFirst();

                ioBusSettingString.ifPresent(str -> {
                    Matcher m2 = Pattern.compile(dpBusSettingsRegex).matcher(str);

                    if (m2.matches()) {
                        // Yeeeaaaahhh!! Module is a controller
                        Integer underlyingSubsysNumber = Integer.parseInt(m2.group("subsysnumber"));
                        Integer underlyingAddress = Integer.parseInt(m2.group("address"));

                        Subsystem subsystem = actualStationBuilder.getSubnets().get(underlyingSubsysNumber);

                        if (subsystem == null) {
                            System.err.println("Trying to add a device to a non-existent subsystem: " + efRes);
                        } else {
                            efRes.setSubsystemMemberShip(new PNIOSubsystemMemberShip(subsystem, underlyingAddress, PNIOSubsystemMemberShip.Role.CONTROLLER));

                            subsystem.attachNode(underlyingAddress, efRes);
                        }
                    }
                });

            });

            return res;
        }

        throw new STEP7HWCfgFileSectionFormatErrorException("Invalid format: " + section.getTitle());
    }
}
