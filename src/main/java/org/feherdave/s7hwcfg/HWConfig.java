package org.feherdave.s7hwcfg;

import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFile;
import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileFormatException;
import org.feherdave.s7hwcfg.cfgfile.S7HWCfgFileSectionFormatErrorException;
import java.io.File;
import java.io.IOException;

public class HWConfig {

    private Station station;

    /**
     * Parses a .cfg file exported from a STEP7 HW config.
     *
     * @param hwCfgFile Exported HW config file.
     * @return HWConfig object.
     */
    public static HWConfig readFromFile(File hwCfgFile) throws S7HWCfgFileFormatException, IOException, S7HWCfgFileSectionFormatErrorException {
        STEP7HWCfgFile STEP7HWCfgFile = new STEP7HWCfgFile(hwCfgFile);
        HWConfig res = new HWConfig();

        res.setStation(Station.fromSectionData(STEP7HWCfgFile.getSectionData()));

        return res;
    }

    private void setStation(Station station) {
        this.station = station;
    }
}
