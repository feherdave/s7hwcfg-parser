import org.feherdave.s7hwcfg.HWConfig;
import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFileFormatException;
import org.feherdave.s7hwcfg.cfgfile.STEP7HWCfgFileSectionFormatErrorException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class S7HWCfgParserTest {

    @Test
    public void testSTEP7HWCfgParser() {

        try {
            HWConfig hwConfig = HWConfig.readFromFile(new File("D:\\tmp\\pm3_winder.cfg"));
        } catch (STEP7HWCfgFileFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (STEP7HWCfgFileSectionFormatErrorException e) {
            throw new RuntimeException(e);
        }

    }

}
