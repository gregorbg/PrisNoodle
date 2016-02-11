package com.puzzletimer.timer;

import com.puzzletimer.managers.TimerManager;

import javax.sound.sampled.TargetDataLine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StackmatTimerReader implements Runnable {
    public interface StackmatTimerReaderListener {
        void dataReceived(byte[] data, boolean hasSixDigits);
    }

    private double sampleRate;
    private int baudRateOffset;
    private int previousBaudRate;
    private int baudRate;
    private double period;
    private TargetDataLine targetDataLine;
    private ArrayList<StackmatTimerReaderListener> listeners;
    private boolean running;
    private boolean hasSixDigits;
    private TimerManager timerManager;

    StackmatTimerReader(TargetDataLine targetDataLine, TimerManager timerManager) {
        this.sampleRate = targetDataLine.getFormat().getFrameRate();
        this.baudRateOffset = 0;
        this.previousBaudRate = 1200;
        this.baudRate = this.previousBaudRate + this.baudRateOffset;
        this.period = this.sampleRate / (double) this.baudRate;
        this.targetDataLine = targetDataLine;
        this.listeners = new ArrayList<>();
        this.running = false;
        this.hasSixDigits = false;
        this.timerManager = timerManager;
    }

    private byte[] readPacket(byte[] samples, int offset, byte bitThreshold, boolean isInverted) {
        byte[] data = new byte[10];
        for (int i = 0; i < 9; i++) {
            // start bit
            boolean startBit = samples[offset + (int) (10 * i * this.period)] <= bitThreshold;
            if ((isInverted && startBit) || (!isInverted && !startBit)) return new byte[10]; // invalid data

            // data bits
            data[i] = 0x00;
            for (int j = 0; j < 8; j++)
                if (samples[offset + (int) ((10 * i + j + 1) * this.period)] > bitThreshold) data[i] |= 0x01 << j;

            if (isInverted) data[i] = (byte) ~data[i];

            // stop bit
            boolean stopBit = samples[offset + (int) ((10 * i + 9) * this.period)] <= bitThreshold;
            if ((isInverted && !stopBit) || (!isInverted && stopBit)) return new byte[10]; // invalid data
        }
        if (data[8] == '\n') this.hasSixDigits = true;
        if (data[8] == '\r') this.hasSixDigits = false;
        if (this.hasSixDigits) {
            int i = 9;
            // start bit
            boolean startBit = samples[offset + (int) (10 * i * this.period)] <= bitThreshold;
            if ((isInverted && startBit) || (!isInverted && !startBit)) return new byte[10]; // invalid data

            // data bits
            data[i] = 0x00;
            for (int j = 0; j < 8; j++)
                if (samples[offset + (int) ((10 * i + j + 1) * this.period)] > bitThreshold) data[i] |= 0x01 << j;

            if (isInverted) data[i] = (byte) ~data[i];

            // stop bit
            boolean stopBit = samples[offset + (int) ((10 * i + 9) * this.period)] <= bitThreshold;
            if ((isInverted && !stopBit) || (!isInverted && stopBit)) return new byte[10]; // invalid data
        }
        return data;
    }

    private boolean isValidPacket(byte[] data) {
        int sum = 0;
        for (int i = 1; i < (hasSixDigits ? 7 : 6); i++) sum += data[i] - '0';

        return hasSixDigits ? (" ACILRS".contains(String.valueOf((char) data[0])) &&
                Character.isDigit(data[1]) &&
                Character.isDigit(data[2]) &&
                Character.isDigit(data[3]) &&
                Character.isDigit(data[4]) &&
                Character.isDigit(data[5]) &&
                Character.isDigit(data[6]) &&
                data[7] == sum + 64 &&
                data[8] == '\n' &&
                data[9] == '\r') :
                ((" ACILRS".contains(String.valueOf((char) data[0])) &&
                        Character.isDigit(data[1]) &&
                        Character.isDigit(data[2]) &&
                        Character.isDigit(data[3]) &&
                        Character.isDigit(data[4]) &&
                        Character.isDigit(data[5]) &&
                        data[6] == sum + 64 &&
                        data[7] == '\n' &&
                        data[8] == '\r'));
    }

    @Override
    public void run() {
        this.running = true;

        this.targetDataLine.start();

        byte[] buffer = new byte[(int) (this.sampleRate / 4)];
        int offset = buffer.length;

        while (this.running) {
            // update buffer in a queue fashion
            System.arraycopy(buffer, offset, buffer, 0, buffer.length - offset);
            this.targetDataLine.read(buffer, buffer.length - offset, offset);

            boolean isPacketStart = false;
            boolean isSignalInverted = false;

            // find packet start
            loop:
            for (this.baudRateOffset = 0; this.baudRateOffset < 25; this.baudRateOffset++) {
                this.baudRate = this.previousBaudRate + this.baudRateOffset;
                this.period = this.sampleRate / (double) this.baudRate;
                for (offset = 0; offset + (this.hasSixDigits ? 0.132015 : 0.119181) * this.sampleRate < buffer.length; offset++)
                    for (int threshold = 0; threshold < 256; threshold++) {
                        byte[] data = readPacket(buffer, offset, (byte) (threshold - 127), false);
                        if (isValidPacket(data)) {
                            isPacketStart = true;
                            break loop;
                        }

                        // try inverting the signal
                        data = readPacket(buffer, offset, (byte) (threshold - 127), true);
                        if (isValidPacket(data)) {
                            isPacketStart = true;
                            isSignalInverted = true;
                            break loop;
                        }
                    }
                this.baudRateOffset = -this.baudRateOffset;
                if (this.baudRateOffset < 0) this.baudRateOffset--;
            }

            if (!isPacketStart) {
                this.timerManager.dataNotReceived(buffer);
                continue;
            }

            HashMap<Integer, Integer> baudRateHistogram = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < this.period; j++) {
                    this.baudRate = this.previousBaudRate + this.baudRateOffset + i;
                    this.period = this.sampleRate / (double) this.baudRate;
                    byte data[] = readPacket(buffer, offset + j, (byte) 0, isSignalInverted);

                    if (isValidPacket(data))
                        baudRateHistogram.put(this.previousBaudRate + this.baudRateOffset + i, baudRateHistogram.containsKey(this.previousBaudRate + this.baudRateOffset + i) ? baudRateHistogram.get(this.previousBaudRate + this.baudRateOffset + i) + 1 : 1);
                }
                i = -i;
                if (i < 0) i--;
            }

            int highestFrequencyBaudRate = 0;
            for (Map.Entry<Integer, Integer> entry : baudRateHistogram.entrySet())
                if (entry.getValue() > highestFrequencyBaudRate) {
                    this.baudRate = entry.getKey();
                    highestFrequencyBaudRate = entry.getValue();
                }

            this.period = this.sampleRate / this.baudRate;
            this.previousBaudRate = this.baudRate;

            // create packet histogram
            HashMap<Long, Integer> packetHistogram = new HashMap<>();
            for (int i = 0; i < this.period; i++)
                for (int threshold = 0; threshold < 256; threshold++) {
                    byte data[] = readPacket(buffer, offset + i, (byte) (threshold - 127), isSignalInverted);

                    if (isValidPacket(data)) {
                        // encode packet
                        long packet = 0L;
                        for (int j = 0; j < (hasSixDigits ? 7 : 6); j++) packet |= (long) data[j] << 8 * j;

                        packetHistogram.put(packet, packetHistogram.containsKey(packet) ? packetHistogram.get(packet) + 1 : 1);
                    }
                }

            // select packet with highest frequency
            long packet = 0L;
            int highestFrequency = 0;
            for (Map.Entry<Long, Integer> entry : packetHistogram.entrySet())
                if (entry.getValue() > highestFrequency) {
                    packet = entry.getKey();
                    highestFrequency = entry.getValue();
                }

            // decode packet
            byte[] data = new byte[9];
            for (int i = 0; i < (hasSixDigits ? 7 : 6); i++) data[i] = (byte) (packet >> 8 * i);

            this.timerManager.dataNotReceived(buffer);

            // notify listeners
            for (StackmatTimerReaderListener listener : this.listeners) listener.dataReceived(data, this.hasSixDigits);

            // skip read packet
            offset += (this.hasSixDigits ? 0.132015 : 0.119181) * this.sampleRate;
        }

        this.targetDataLine.close();
    }

    public void stop() {
        this.running = false;
    }

    public void addEventListener(StackmatTimerReaderListener listener) {
        this.listeners.add(listener);
    }

    public void removeEventListener(StackmatTimerReaderListener listener) {
        this.listeners.remove(listener);
    }
}
