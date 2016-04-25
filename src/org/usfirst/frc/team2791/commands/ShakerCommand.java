package org.usfirst.frc.team2791.commands;

/**
 * Created by Akhil on 3/7/2016.
 * This class acts as template for any command class
 */
public abstract class ShakerCommand {// template class for commands

    //Variable tells whether or not the command is running
    protected static boolean running;
    protected static int counter;

    //returns whether the class is running
    static boolean isRunning() {
        return running;
    }

    //Reset the class
    abstract void reset();

    //run method for when it is on its own thread
    abstract void run();

    //extending class should reset then set running method to true
    //then run the run method
    abstract void start();

    //Put necessary values on the smartdashboard
    abstract public void updateSmartDash();

    //put values necessary to debug the command
    abstract public void debug();
}
