package org.feherdave.s7hwcfg.cfgfile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class representing a section in the config file.
 */
public class CfgFileSection {

    private List<String> sectionHeader;
    private List<String> configurationData;

    public CfgFileSection(List<String> configurationData) {
        List<String> data = configurationData.stream()
                .dropWhile(line -> line.isEmpty() || line.isBlank())
                .takeWhile(line -> !line.isEmpty() && !line.isBlank())
                .collect(Collectors.toList());

        this.sectionHeader = data.stream()
                .takeWhile(line -> !line.equals("BEGIN"))
                .collect(Collectors.toList());

        this.configurationData = data.stream()
                .dropWhile(line -> !line.equals("BEGIN"))
                .takeWhile(line -> !line.equals("END"))
                .collect(Collectors.toList());
    }

    /**
     * Gets all lines of configuration data.
     *
     * @return
     */
    public List<String> getConfigurationData() {
        return configurationData;
    }

    /**
     * Gets first line of section header.
     *
     * @return
     */
    public String getSectionHeader() {
        return sectionHeader.get(0);
    }

    /**
     * Gets all lines of section header.
     *
     * @return
     */
    public List<String> getFullSectionHeader() {
        return sectionHeader;
    }

    /**
     * Gets only options data of section header (section header lines excluding the first line).
     *
     * @return
     */
    public List<String> getSectionHeaderOptions() {
        return sectionHeader.subList(1, sectionHeader.size());
    }
}
