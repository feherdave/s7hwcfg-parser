package s7hw;

import com.google.gson.internal.LinkedTreeMap;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for hardware components.
 */
public abstract class HWComponent {

    public class Address {
        public enum AddressType { INPUT, OUTPUT };

        public AddressType addressType;
        public Integer address;
        public Integer offsetBit;

        public Address(AddressType addressType, Integer address, Integer offsetBit) {
            this.addressType = addressType;
            this.address = address;
            this.offsetBit = offsetBit;
        }
    }

    public class AddressArea {
        public Address startAddress;
        public Integer areaLength;
        public Integer areaLengthBit;
        public Integer usedSpace;      // Not sure...
        public Integer usedSpaceBit;   // Not sure...

        public AddressArea(Address.AddressType addressType, Integer startByte, Integer startBit, Integer areaLength, Integer areaLengthBit, Integer usedSpace, Integer usedSpaceBit) {
            this.startAddress = new Address(addressType, startByte, startBit);
            this.areaLength = areaLength;
            this.areaLengthBit = areaLengthBit;
            this.usedSpace = usedSpace;
            this.usedSpaceBit = usedSpaceBit;
        }
    }

    public class Symbol {
        public Address address;
        public String symbolName;
        public String comment;
    }

    public static List<String> CONFIG_DATA_KEYWORDS = List.of("LOCAL_IN_ADDRESSES", "LOCAL_OUT_ADDRESSES", "PARAMETER", "SYMBOL");

    protected Map<String, String> stationData = new LinkedTreeMap<>();
    protected Map<String, List<AddressArea>> addressAreas = new LinkedTreeMap<>();

    public Map<String, String> getStationData() {
        return stationData;
    }

    /**
     * Processes the configuration section between START and END
     *
     * @param configSection
     */
    protected void parseConfigurationData(List<String> configSection) {

        // Process configuration section
        configSection.stream()
                .takeWhile(line -> !CONFIG_DATA_KEYWORDS.contains(line))
                .forEach(line -> {
                    Pattern dataKeyValuePair = Pattern.compile("^(?<key>\\w+)\\s+\\\"(?<value>\\w*)\\\"$");
                    Matcher dataKeyValuePairMatcher = dataKeyValuePair.matcher(line);

                    if (dataKeyValuePairMatcher.matches()) {
                        stationData.put(dataKeyValuePairMatcher.group("key"), dataKeyValuePairMatcher.group("value"));
                    }
                });

        // Process input/output addresses
        String addressRegex = "^\\s*ADDRESS\\s*(?<startByte>\\d+)\\s*,\\s*(?<startBit>\\d+)\\s*,\\s*(?<areaLength>\\d+)\\s*,\\s*(?<areaLengthOffset>\\d+)\\s*,\\s*(?<usedLength>\\d+)\\s*,\\s*(?<usedLengthOffset>\\d+)\\s*$";

        // Input
        configSection.stream()
                .dropWhile(line -> !line.equals("LOCAL_IN_ADDRESSES"))
                .skip(1)
                .takeWhile(line -> !CONFIG_DATA_KEYWORDS.contains(line))
                .filter(line -> line.matches(addressRegex))
                .forEach(line -> {
                    addressAreas.putIfAbsent("input", new ArrayList<>());
                    List<AddressArea> inputAddresses = addressAreas.get("input");

                    Pattern p = Pattern.compile(addressRegex);
                    Matcher m = p.matcher(line);

                    Integer startByte = Integer.parseInt(m.group("startByte"));
                    Integer startBit = Integer.parseInt(m.group("startBit"));
                    Integer areaLength = Integer.parseInt(m.group("areaLength"));
                    Integer areaLengthOffset = Integer.parseInt(m.group("areaLengthOffset"));
                    Integer usedLength = Integer.parseInt(m.group("usedLength"));
                    Integer usedLengthOffset = Integer.parseInt(m.group("usedLengthOffset"));

                    inputAddresses.add(
                            new AddressArea(
                                    Address.AddressType.INPUT,
                                    startByte,
                                    startBit,
                                    areaLength,
                                    areaLengthOffset,
                                    usedLength,
                                    usedLengthOffset
                            ));
                });

        // Output
        configSection.stream()
                .dropWhile(line -> !line.equals("LOCAL_OUT_ADDRESSES"))
                .skip(1)
                .takeWhile(line -> !CONFIG_DATA_KEYWORDS.contains(line))
                .filter(line -> line.matches(addressRegex))
                .forEach(line -> {
                    addressAreas.putIfAbsent("output", new ArrayList<>());
                    List<AddressArea> inputAddresses = addressAreas.get("output");

                    Pattern p = Pattern.compile(addressRegex);
                    Matcher m = p.matcher(line);

                    Integer startByte = Integer.parseInt(m.group("startByte"));
                    Integer startBit = Integer.parseInt(m.group("startBit"));
                    Integer areaLength = Integer.parseInt(m.group("areaLength"));
                    Integer areaLengthOffset = Integer.parseInt(m.group("areaLengthOffset"));
                    Integer usedLength = Integer.parseInt(m.group("usedLength"));
                    Integer usedLengthOffset = Integer.parseInt(m.group("usedLengthOffset"));

                    inputAddresses.add(
                            new AddressArea(
                                    Address.AddressType.OUTPUT,
                                    startByte,
                                    startBit,
                                    areaLength,
                                    areaLengthOffset,
                                    usedLength,
                                    usedLengthOffset
                            ));
                });
    }
}
