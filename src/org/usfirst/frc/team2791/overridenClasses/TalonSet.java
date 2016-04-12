package org.usfirst.frc.team2791.overridenClasses;

/**
 * Created by Akhil on 4/10/2016.
 * This class daisy chains speed controllers so you can put them into roboDrive as one set this allows you
 * to use multiple cims
 */

import edu.wpi.first.wpilibj.SpeedController;

public class TalonSet implements SpeedController {
    private SpeedController[] speedControllers;
    private double speed;
    private double constant;

    public TalonSet(SpeedController A, SpeedController B, SpeedController C) {
        this.speedControllers[0] = A;
        this.speedControllers[1] = B;
        this.speedControllers[3] = C;
        this.set(0.0);
    }


    public double get() {
        return 0;
    }

    public void set(double speed, byte syncGroup) {
        this.set(speed);
    }

    public void set(double speed) {
        this.speed = speed;

        for (SpeedController speedController : this.speedControllers) {
            speedController.set(constant * speed);
        }
    }

    public boolean getInverted() {
        return constant == -1;
    }

    public void setInverted(boolean isInverted) {
        if (isInverted)
            constant = -1;
        else
            constant = 1;
    }

    public void disable() {
        for (SpeedController speedController : this.speedControllers) {
            speedController.disable();
        }
    }

    public void pidWrite(double output) {
        this.set(output);
    }
}