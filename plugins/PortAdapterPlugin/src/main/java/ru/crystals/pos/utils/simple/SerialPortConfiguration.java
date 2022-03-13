package ru.crystals.pos.utils.simple;

import jssc.SerialPort;

import java.util.Objects;

public class SerialPortConfiguration {

    private final String port;
    private final int baudRate;
    private final int dataBits;
    private final int stopBits;
    private final int parity;

    public SerialPortConfiguration(String port, int baudRate, int dataBits, int stopBits, int parity) {
        Objects.requireNonNull(port, "Port should not be null");
        this.port = port;
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
    }

    public static SerialPortConfigurationBuilder builder() {
        return new SerialPortConfigurationBuilder();
    }

    public String getPort() {
        return port;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SerialPortConfiguration that = (SerialPortConfiguration) o;
        return baudRate == that.baudRate &&
                dataBits == that.dataBits &&
                stopBits == that.stopBits &&
                parity == that.parity &&
                Objects.equals(port, that.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(port, baudRate, dataBits, stopBits, parity);
    }

    @Override
    public String toString() {
        return "SerialPortConfiguration{" +
                "port='" + port + '\'' +
                ", baudRate=" + baudRate +
                ", dataBits=" + dataBits +
                ", stopBits=" + stopBits +
                ", parity=" + parity +
                '}';
    }

    public static class SerialPortConfigurationBuilder {
        private String port;
        private int baudRate = SerialPort.BAUDRATE_9600;
        private int dataBits = SerialPort.DATABITS_8;
        private int stopBits = SerialPort.STOPBITS_1;
        private int parity = SerialPort.PARITY_NONE;

        SerialPortConfigurationBuilder() {
        }

        public SerialPortConfigurationBuilder port(String port) {
            this.port = port;
            return this;
        }

        public SerialPortConfigurationBuilder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public SerialPortConfigurationBuilder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public SerialPortConfigurationBuilder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public SerialPortConfigurationBuilder parity(int parity) {
            this.parity = parity;
            return this;
        }

        public SerialPortConfiguration build() {
            return new SerialPortConfiguration(port, baudRate, dataBits, stopBits, parity);
        }
    }
}
