package org.feherdave.s7hwcfg;

import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFile;
import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFileFormatException;
import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFileSectionFormatErrorException;
import org.feherdave.s7hwcfg.s7.Station;

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
    public static HWConfig readFromFile(File hwCfgFile) throws STEP7HWCfgFileFormatException, IOException, STEP7HWCfgFileSectionFormatErrorException {
        STEP7HWCfgFile step7HWCfgFile = new STEP7HWCfgFile(hwCfgFile);
        HWConfig res = new HWConfig();

        res.setStation(step7HWCfgFile.parseSections());

        return res;
    }

    private void setStation(Station station) {
        this.station = station;
    }
}
