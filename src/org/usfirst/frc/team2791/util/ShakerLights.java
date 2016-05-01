package org.usfirst.frc.team2791.util;

import edu.wpi.first.wpilibj.DigitalOutput;

/**
 * Created by Akhil on 4/27/2016.
 * This class sends special commands to the lights
 */
public class ShakerLights implements Runnable {
    private DigitalOutput PortA;
    private DigitalOutput PortB;
    private boolean solidGreen = false;
    private boolean yellowPulse = false;
    private boolean greenPulse = false;


    public ShakerLights() {
        //TODO PUT PORT NUMBERS IN
//        PortA = new DigitalOutput();
//        PortB = new DigitalOutput();

    }

    public void run() {
        while (true) {
            try {
                if (solidGreen) {
                    PortA.set(true);
                    PortB.set(true);
                }
                if (yellowPulse) {
                    PortA.set(true);
                    PortB.set(false);
                }
                if (greenPulse) {
                    PortA.set(false);
                    PortB.set(true);
                }
                if (!(solidGreen || yellowPulse || greenPulse)) {
                    PortA.set(false);
                    PortB.set(false);
                }
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                run();
            }
        }
    }

    /**
     * this turns the entire shooter arm solid green
     *
     * @param state this is the flag that decides true or false
     */
    public void setSolidGreen(boolean state) {
        this.solidGreen = state;
    }

    /**
     * This will pulse leds in yellow color from bottom to top(shooter)
     *
     * @param pulse this flag deicdes whehter to pulse or not
     */
    public void setYellowPulse(boolean pulse) {
        this.yellowPulse = pulse;
    }

    /**
     * This will pulse leds in green color from bottom to top
     *
     * @param pulse this flag controls whether to pulse or not
     */
    public void setGreenPulse(boolean pulse) {
        this.greenPulse = pulse;
    }
}
