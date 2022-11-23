package org.feherdave.s7hwcfg.s7;

import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFileSectionFormatErrorException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HWConfigElement {

    public static List<String> CONFIG_DATA_KEYWORDS = List.of("LOCAL_IN_ADDRESSES", "LOCAL_OUT_ADDRESSES", "PARAMETER", "SYMBOL");
    protected Map<String, String> data = new LinkedHashMap<>();

    /**
     * Processes the configuration section between START and END
     *
     * @param configSection
     */
    public void parseConfigurationData(List<String> configSection) throws STEP7HWCfgFileSectionFormatErrorException {
        // Process configuration section
        configSection.stream()
                .takeWhile(line -> !CONFIG_DATA_KEYWORDS.contains(line))
                .forEach(line -> {
                    Pattern dataKeyValuePair = Pattern.compile("^(?<key>\\w+)\\s+\"(?<value>.*?)\"$");
                    Matcher dataKeyValuePairMatcher = dataKeyValuePair.matcher(line);

                    if (dataKeyValuePairMatcher.matches()) {
                        data.put(dataKeyValuePairMatcher.group("key"), dataKeyValuePairMatcher.group("value"));
                    }
                });
    }

    public Map<String, String> getData() {
        return data;
    }

    public void putData(String key, String value) {
        data.put(key, value);
    }
}
