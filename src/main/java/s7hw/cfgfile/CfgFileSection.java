package s7hw.cfgfile;

import java.util.List;
import java.util.stream.Collectors;

public class CfgFileSection {

    private String sectionHeader;
    private List<String> sectionData;

    public CfgFileSection(List<String> sectionData) {
        List<String> data = sectionData.stream()
                .dropWhile(line -> line.isEmpty() || line.isBlank())
                .takeWhile(line -> !line.isEmpty() && !line.isBlank())
                .collect(Collectors.toList());

        this.sectionHeader = data.get(0);
        this.sectionData = data.subList(1, data.size());
    }

    public List<String> getSectionData() {
        return sectionData;
    }

    public String getSectionHeader() {
        return sectionHeader;
    }
}
