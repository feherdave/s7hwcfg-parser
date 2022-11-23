package org.feherdave.s7hwcfg.s7.hw;

import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFileSectionFormatErrorException;
import org.feherdave.s7hwcfg.s7.HWConfigElement;
import org.feherdave.s7hwcfg.s7.hw.module.SubsystemMemberShip;
import org.feherdave.s7hwcfg.s7.system.Address;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Base class for hardware components.
 */
public abstract class HWComponent extends HWConfigElement {

    public class AddressArea {
        public Address startAddress;
        public Address areaLength;

        public AddressArea(Address startAddress, Address areaLength) {
            this.startAddress = startAddress;
            this.areaLength = areaLength;
        }
    }

    public class Symbol {
        public Address address;
        public String symbolName;
        public String comment;
    }

    protected Map<String, List<AddressArea>> addressAreas = new LinkedHashMap<>();
    protected SubsystemMemberShip subsystemMemberShip;

    /**
     * Processes the configuration section between START and END
     *
     * @param configSection
     */
    @Override
    public void parseConfigurationData(List<String> configSection) throws STEP7HWCfgFileSectionFormatErrorException {

        super.parseConfigurationData(configSection);

        // Process input/output addresses
        String addressRegex = "^\\s*ADDRESS\\s*(?<startByte>\\d+)\\s*,\\s*(?<startBit>\\d+)\\s*,\\s*(?<lengthByte>\\d+)\\s*,\\s*(?<lengthBit>\\d+)\\s*,\\s*(?<addressType1>\\d+)\\s*,\\s*(?<addressType2>\\d+)\\s*$";

        // Parse input address definitions
        List<String> inputAddressLines = configSection.stream()
                .dropWhile(line -> !line.equals("LOCAL_IN_ADDRESSES"))
                .skip(1)
                .takeWhile(line -> !CONFIG_DATA_KEYWORDS.contains(line))
                .filter(line -> line.matches(addressRegex))
                .collect(Collectors.toList());

        for (String line : inputAddressLines) {
            addressAreas.putIfAbsent("input", new ArrayList<>());
            List<AddressArea> inputAddresses = addressAreas.get("input");

            Pattern p = Pattern.compile(addressRegex);
            Matcher m = p.matcher(line);

            if (m.matches()) {
                Integer startByte = Integer.parseInt(m.group("startByte"));
                Integer startBit = Integer.parseInt(m.group("startBit"));
                Integer areaLength = Integer.parseInt(m.group("lengthByte"));
                Integer areaLengthOffset = Integer.parseInt(m.group("lengthBit"));
                Integer addressType1 = Integer.parseInt(m.group("addressType1"));
                Integer addressType2 = Integer.parseInt(m.group("addressType2"));

                // That's only a guess... :)
                Address startAddress;
                Address addrAreaLength = Address.Plain().b(areaLength);

                switch (addressType1) {
                    case 0:
                        if (addressType2 == 16) {
                            addrAreaLength = Address.Plain().x(areaLength, 0);
                        }
                    case 1:
                    case 2:
                        startAddress = Address.Input().b(startByte);
                        break;
                    case 7:
                    case 8:
                        startAddress = Address.Input().w(startByte);
                        break;
                    default:
                        startAddress = Address.Input().x(startByte, startBit);
                }

                inputAddresses.add(new AddressArea(startAddress, addrAreaLength));
            } else {
                throw new STEP7HWCfgFileSectionFormatErrorException("The following line in section LOCAL_IN_ADDRESSES couldn't be parsed: " + line);
            }
        }

        // Parse output address definitions
        List<String> outputAddressLines = configSection.stream()
                .dropWhile(line -> !line.equals("LOCAL_OUT_ADDRESSES"))
                .skip(1)
                .takeWhile(line -> !CONFIG_DATA_KEYWORDS.contains(line))
                .filter(line -> line.matches(addressRegex))
                .collect(Collectors.toList());

        for (String line : outputAddressLines) {
            addressAreas.putIfAbsent("output", new ArrayList<>());
            List<AddressArea> outputAddresses = addressAreas.get("output");

            Pattern p = Pattern.compile(addressRegex);
            Matcher m = p.matcher(line);

            if (m.matches()) {
                Integer startByte = Integer.parseInt(m.group("startByte"));
                Integer startBit = Integer.parseInt(m.group("startBit"));
                Integer areaLength = Integer.parseInt(m.group("lengthByte"));
                Integer areaLengthOffset = Integer.parseInt(m.group("lengthBit"));
                Integer addressType1 = Integer.parseInt(m.group("addressType1"));
                Integer addressType2 = Integer.parseInt(m.group("addressType2"));

                // That's only a guess... :)
                Address startAddress;
                Address addrAreaLength = Address.Plain().b(areaLength);

                switch (addressType1) {
                    case 0:
                        if (addressType2 == 16) {
                            addrAreaLength = Address.Plain().x(areaLength, 0);
                        }
                    case 1:
                    case 2:
                        startAddress = Address.Output().b(startByte);
                        break;
                    case 7:
                    case 8:
                        startAddress = Address.Output().w(startByte);
                        break;
                    default:
                        startAddress = Address.Output().x(startByte, startBit);
                }

                outputAddresses.add(new AddressArea(startAddress, addrAreaLength));
            } else {
                throw new STEP7HWCfgFileSectionFormatErrorException("The following line in section LOCAL_OUT_ADDRESSES couldn't be parsed: " + line);
            }
        }
    }

    /**
     * Gets subsystem membership.
     *
     * @return
     */
    public SubsystemMemberShip getSubsystemMemberShip() {
        return subsystemMemberShip;
    }

    /**
     * Sets subsystem membeership.
     *
     * @param subsystemMemberShip
     */
    public void setSubsystemMemberShip(SubsystemMemberShip subsystemMemberShip) {
        this.subsystemMemberShip = subsystemMemberShip;
    }
}
