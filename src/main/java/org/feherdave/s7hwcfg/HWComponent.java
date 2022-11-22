package org.feherdave.s7hwcfg;

import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileSectionFormatErrorException;
import org.feherdave.s7hwcfg.system.Address;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Base class for hardware components.
 */
public abstract class HWComponent {

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

    public static List<String> CONFIG_DATA_KEYWORDS = List.of("LOCAL_IN_ADDRESSES", "LOCAL_OUT_ADDRESSES", "PARAMETER", "SYMBOL");

    protected Map<String, String> stationData = new LinkedHashMap<>();
    protected Map<String, List<AddressArea>> addressAreas = new LinkedHashMap<>();

    public Map<String, String> getStationData() {
        return stationData;
    }

    /**
     * Processes the configuration section between START and END
     *
     * @param configSection
     */
    protected void parseConfigurationData(List<String> configSection) throws S7HWCfgFileSectionFormatErrorException {

        // Process configuration section
        configSection.stream()
                .takeWhile(line -> !CONFIG_DATA_KEYWORDS.contains(line))
                .forEach(line -> {
                    Pattern dataKeyValuePair = Pattern.compile("^(?<key>\\w+)\\s+\"(?<value>.*?)\"$");
                    Matcher dataKeyValuePairMatcher = dataKeyValuePair.matcher(line);

                    if (dataKeyValuePairMatcher.matches()) {
                        stationData.put(dataKeyValuePairMatcher.group("key"), dataKeyValuePairMatcher.group("value"));
                    }
                });

        // Process input/output addresses
        String addressRegex = "^\\s*ADDRESS\\s*(?<startByte>\\d+)\\s*,\\s*(?<startBit>\\d+)\\s*,\\s*(?<lengthByte>\\d+)\\s*,\\s*(?<lengthBit>\\d+)\\s*,\\s*(?<addressType>\\d+)\\s*,\\s*(?<var2>\\d+)\\s*$";

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
                Integer addressType = Integer.parseInt(m.group("addressType"));

                // That's only a guess... :)
                Address startAddress;

                switch (addressType) {
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

                inputAddresses.add(new AddressArea(startAddress, Address.Plain().b(areaLength)));
            } else {
                throw new S7HWCfgFileSectionFormatErrorException("The following line in section LOCAL_IN_ADDRESSES couldn't be parsed: " + line);
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
                Integer addressType = Integer.parseInt(m.group("addressType"));

                // That's only a guess... :)
                Address startAddress;

                switch (addressType) {
                    case 1:
                    case 2:
                        startAddress = Address.Output().b(startByte);
                        break;
                    case 7:
                    case 8:
                        startAddress = Address.Output().w(startByte);
                    default:
                        startAddress = Address.Output().x(startByte, startBit);
                }

                outputAddresses.add(new AddressArea(startAddress, Address.Plain().b(areaLength)));
            } else {
                throw new S7HWCfgFileSectionFormatErrorException("The following line in section LOCAL_OUT_ADDRESSES couldn't be parsed: " + line);
            }
        }
    }
}
