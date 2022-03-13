package ru.crystals.pos.customerdisplay.lcd;


public class DisplayThread extends Thread {

    private String line1 = "";
    private String line2 = "";
    private boolean changed = false;
    private FiscalPrinterPluginWithCustomerDisplay provider;
    private long interval;

    public DisplayThread(FiscalPrinterPluginWithCustomerDisplay fiscalPrinter, long interval) {
        this.provider = fiscalPrinter;
        this.interval = interval;
    }

    @Override
    public void run() {
        while (!interrupted()) {
            try {
                displayLines();
                Thread.sleep(interval);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized String getLine1() {
        return line1;
    }

    public synchronized void setLine1(String line1) {
        if (!this.line1.equals(line1)) {
            this.line1 = line1;
            changed = true;
        }
    }

    public synchronized String getLine2() {
        return line2;
    }

    public synchronized void setLine2(String line2) {
        if (!this.line2.equals(line2)) {
            this.line2 = line2;
            changed = true;
        }
    }

    private synchronized void displayLines() {
        try {
            if (changed) {
                changed = false;
                provider.displayLines(new String[]{line1, line2});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized long getInterval() {
        return interval;
    }

    public synchronized void setInterval(long interval) {
        this.interval = interval;
    }
}
