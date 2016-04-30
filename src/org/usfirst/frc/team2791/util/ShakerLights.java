package org.usfirst.frc.team2791.util;

import edu.wpi.first.wpilibj.DigitalOutput;
import edu.wpi.first.wpilibj.Timer;

/**
 * Created by Akhil on 4/27/2016.
 * This class sends special commands to the lights
 */
public class ShakerLights implements Runnable {
    private DigitalOutput PortA;
    private DigitalOutput PortB;
    private boolean hasBall = false;
    private boolean preppingShot = false;
    private boolean shooting = false;


    public ShakerLights() {
        //TODO PUT PORT NUMBERS IN
//        PortA = new DigitalOutput();
//        PortB = new DigitalOutput();

    }

    public void run() {
        while (true) {
            try {
                if (hasBall) {
                    Timer temp = new Timer();
                    temp.start();
                    while (temp.get() < 2) {
                        PortA.set(true);
                        PortB.set(true);
                    }
                    hasBall = false;
                }
                if (preppingShot) {
                    PortA.set(true);
                    PortB.set(false);
                }
                if (shooting) {
                    PortA.set(false);
                    PortB.set(true);
                }
                if (!(hasBall || preppingShot || shooting)) {
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

    public void setHasBall(boolean hasBall) {
        this.hasBall = hasBall;
    }

    public void setPreppingShot(boolean preppingShot) {
        this.preppingShot = preppingShot;
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
    }
}
