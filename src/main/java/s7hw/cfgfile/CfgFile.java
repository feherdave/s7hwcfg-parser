package s7hw.cfgfile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CfgFile {

    enum FileFormat { READABLE, COMPACT }

    private String fileVersion;
    private FileFormat format = FileFormat.READABLE;
    private Map<String, String> metaData = new HashMap<>();
    private List<CfgFileSection> sectionData = new ArrayList<>();

    public CfgFile(File file) throws S7HWCfgFileFormatException, IOException {

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader br = new BufferedReader(isr);

        List<String> lines = br.lines().collect(Collectors.toList());

        if (lines.size() == 0) {
            throw new S7HWCfgFileFormatException("File too short (line count 0).");
        }

        List<String> headerLines = new ArrayList<>();
        List<String> sectionLines = new ArrayList<>();

        ListIterator<String> iter = lines.listIterator();

        // Read lines
        if (iter.hasNext()) {

            // Read header
            boolean contentStartFound = false;
            String line = "";

            do {
                line = iter.next();

                contentStartFound = line.startsWith("STATION");

                if (!contentStartFound) {
                    headerLines.add(line);
                }
            } while (iter.hasNext() && !contentStartFound);

            if (!contentStartFound) {
                throw new S7HWCfgFileFormatException("STATION section missing.");
            }

            // Read content

            do {
                sectionLines.add(line);

                if (iter.hasNext()) {
                    line = iter.next();
                }
            } while (iter.hasNext());

            parseHeader(headerLines);
            readSections(sectionLines);
        }

    }

    /**
     * Parse header lines.
     *
     * @param headerLines
     * @throws S7HWCfgFileFormatException
     */
    private void parseHeader(List<String> headerLines) throws S7HWCfgFileFormatException {
        String fileVersionRegex = "^FILEVERSION \\\"([a-zA-Z0-9]+\\.[a-zA-Z0-9]+)\\\"$";
        String metaDataRegex = "^\\#(?<metatag>[A-Z0-9_]+)\\s(?<metadata>.+)$";
        String compactFormatRegex = "^FORMAT\\sCOMPACT$";

        // Read file version
        Pattern fileVersionPattern = Pattern.compile(fileVersionRegex);
        String fileVersionLine = headerLines.stream().filter(line -> line.matches(fileVersionRegex)).findFirst().orElseThrow(() -> new S7HWCfgFileFormatException("FILEVERSION entry missing from file"));

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
                            };
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
     * @throws S7HWCfgFileFormatException
     */
    private void readSections(List<String> sectionLines) throws S7HWCfgFileFormatException {
        Iterator<String> iter = sectionLines.iterator();

        if (iter.hasNext()) {
            List<String> sectionStringData = new ArrayList<>();

            do {
                String line = iter.next();

                if (!line.isEmpty()) {
                    sectionStringData.add(line.trim());
                } else {
                    if (!sectionStringData.get(sectionStringData.size() - 1).equals("END")) {
                        throw new S7HWCfgFileFormatException("END missing in the following section: " + sectionStringData.stream().collect(Collectors.joining("\n")));
                    }

                    sectionData.add(new CfgFileSection(sectionStringData));
                    sectionStringData = new ArrayList<>();
                }
            } while (iter.hasNext());
        } else {
            throw new S7HWCfgFileFormatException("STATION section doesn't follow rules");
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
    public List<CfgFileSection> getSectionData() {
        return sectionData;
    }
}
