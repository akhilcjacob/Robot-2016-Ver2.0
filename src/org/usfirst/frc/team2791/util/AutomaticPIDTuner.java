package org.usfirst.frc.team2791.util;

import org.usfirst.frc.team2791.overridenClasses.BasicPID;

import java.util.Arrays;

/**
 * Created by Akhil on 4/13/2016.
 * This class uses the twiddle algorithm to calculate PID gains
 * This is based on theory and has yet to be tested
 */
public class AutomaticPIDTuner implements Runnable {
    Thread tunerThread;
    AutomaticPIDTuner tuner;
    BasicPID controller;
    boolean running = true;
    //error tolerance
    private double tolerence;
    private double bestError;
    //calculated pid values
    private double[] pid = {0, 0, 0};

    private double[] pidIncrementer = {.1, .1, .1};


    private AutomaticPIDTuner() {
        tolerence = 0.0001;
    }

    public AutomaticPIDTuner getInstance() {
        if (tuner == null)
            tuner = new AutomaticPIDTuner();
        if (tunerThread == null) {
            tunerThread = new Thread();
        }
        return tuner;
    }

    public void setPIDController(BasicPID PIDController) {
        controller = PIDController;
    }

    public void setTolerence(double newTolerence) {
        tolerence = newTolerence;
    }

    public void destroy() {
        running = false;
        tuner = null;
        tunerThread = null;
    }

    public void calculateAndUpdateGains(double setpoint) {
        //keep running until the imcrementers are near 0
        double currentError = 100;
        while (pidIncrementer[0] + pidIncrementer[1] + pidIncrementer[2] > tolerence) {
            for (int x = 0; x < 3; x++) {
                pid[x] += pidIncrementer[x];
                controller.changeGains(pid[0], pid[1], pid[2]);
                controller.setSetPoint(setpoint);
                sleep(5000);
                currentError = controller.getError();
                if (currentError < bestError) {
                    bestError = currentError;
                    pidIncrementer[x] *= 1.1;
                } else {
                    pid[x] -= 2.0 * pidIncrementer[x];
                    controller.changeGains(pid[0], pid[1], pid[2]);
                    controller.setSetPoint(0);
                    sleep(5000);
                    currentError = controller.getError();
                    if (currentError < bestError) {
                        bestError = currentError;
                        pidIncrementer[x] *= 1.1;
                    } else {
                        pid[x] += pidIncrementer[x];
                        pidIncrementer[x] *= 0.9;
                    }
                }

            }
            System.out.println("Automatic Tuning Results: PID values: " + Arrays.toString(pid) + " Current error: "
                    + currentError + " Best error " + bestError);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        tunerThread.start();
    }

    public void run() {
        double multiplier = -1.1;
        double setpoint = 10;
        while (running) {
            calculateAndUpdateGains(setpoint*=multiplier);
        }
    }
}

