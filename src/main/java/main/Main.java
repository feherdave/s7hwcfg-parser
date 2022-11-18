package main;

import s7hw.HWConfig;
import s7hw.cfgfile.S7HWCfgFileFormatException;
import s7hw.cfgfile.S7HWCfgFileSectionFormatErrorException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try {
            HWConfig hwConfig = HWConfig.readFromFile(new File("D:\\tmp\\cpu04.cfg"));

            System.out.println();
        } catch (S7HWCfgFileFormatException | IOException | S7HWCfgFileSectionFormatErrorException e) {
            System.out.println(e.getMessage());
        }
    }
}