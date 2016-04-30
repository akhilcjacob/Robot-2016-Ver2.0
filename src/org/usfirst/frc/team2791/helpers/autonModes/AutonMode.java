package org.usfirst.frc.team2791.helpers.autonModes;

/**
 * Created by team2791 on 3/17/2016.
 * template class for any autonmode
 */
public class AutonMode {
    protected int state = 0;

    public void run() {
        System.out.println("Default auton mode run method running! Override me!");
    }

    public void start() {
        state = 1;
    }

    public boolean getCompleted() {
        return state == 0;
    }
}