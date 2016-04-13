package org.usfirst.frc.team2791.util;


/**
 * Created by Akhil on 4/12/2016.
 * This is a toggle class designed to change output only once and hold it
 */
public class Latch {
    private boolean previousOutput;
    private boolean output;
    private boolean firstSwitch = true;

    public Latch(boolean defaultState) {
        output = defaultState;
    }

    public void giveToggleInput(boolean input) {
        output = input && !previousOutput;
        previousOutput = input;
    }

    public void setManual(boolean newOutput) {
        output = newOutput;
    }

    public boolean getToggleOutput() {
        return output;
    }

    public boolean get() {
        return getToggleOutput();
    }

}