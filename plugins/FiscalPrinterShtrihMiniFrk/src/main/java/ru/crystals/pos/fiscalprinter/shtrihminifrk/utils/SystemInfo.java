package ru.crystals.pos.fiscalprinter.shtrihminifrk.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SystemInfo {

    private String zap = ";";
    private String osName = "";
    private String osVersion = "";
    private boolean linux = true;
    private boolean is64BitOS = false;

    public SystemInfo() {
        String arch = System.getProperty("os.arch");
        boolean isArm = "arm".equals(arch);
        if (isArm) {
            osName = "Linux ARM";
        } else {
            if (!System.getProperty("os.name").toLowerCase().contains("linux")) {
                linux = false;
            }
            if (!linux) {
                String archWin = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

                String realArch = archWin.endsWith("64")
                        || wow64Arch != null && wow64Arch.endsWith("64")
                        ? "64" : "32";
                is64BitOS = realArch.contains("64");

            } else {
                is64BitOS = arch.contains("64");
            }
            osName = String.format("%s %s", (linux ? "Linux" : "Windows"), (is64BitOS ? "64bit" : "32bit"));
        }
        zap = linux ? ":" : ";";
        osVersion = getOsVersionFromOs();
    }

    public boolean isIs64BitOS() {
        return is64BitOS;
    }

    public boolean isLinux() {
        return linux;
    }

    public String getOsName() {
        return osName;
    }

    public String getZap() {
        return zap;
    }

    public String getOsVersion() {
        return osVersion;
    }

    private String getOsVersionFromOs() {
        String result = "";
        if (linux) {
            try {
                Process p = Runtime.getRuntime().exec("uname -r");
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(p.getInputStream()));
                return in.readLine();
            } catch (IOException e) {
                return result;
            }
        } else {
            return System.getProperty("os.version");
        }
    }


    public boolean isTinyCore() {
        if (getOsVersionFromOs().contains("tinycore")) {
            return true;
        } else {
            return false;
        }
    }
}
