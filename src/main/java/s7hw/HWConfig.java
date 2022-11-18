package s7hw;

import s7hw.cfgfile.CfgFile;
import s7hw.cfgfile.S7HWCfgFileFormatException;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HWConfig {

    private Station station;
    private List<Rack> racks = new ArrayList<>();

    /**
     * Parses a .cfg file exported from a STEP7 HW config.
     *
     * @param hwCfgFile Exported HW config file.
     * @return HWConfig object.
     */
    public static HWConfig readFromFile(File hwCfgFile) throws S7HWCfgFileFormatException, IOException, S7HWCfgFileSectionFormatErrorException {
        CfgFile cfgFile = new CfgFile(hwCfgFile);
        HWConfig res = new HWConfig();

        res.setStation(Station.fromSectionData(cfgFile.getSectionData()));

        return res;
    }

    private void setStation(Station station) {
        this.station = station;
    }
}
