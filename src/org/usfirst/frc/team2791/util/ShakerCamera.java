package org.usfirst.frc.team2791.util;

import com.ni.vision.NIVision;
import com.ni.vision.VisionException;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.vision.USBCamera;

import java.util.ArrayList;


/**
 * Created by Akhil on 4/11/2016.
 * This class takes images from the camera and runs processing
 * it is setup to use one frame at a time and to also run on a separate thread
 */
public class ShakerCamera implements Runnable {
    //Camera used was the logitech C210, we caluclated this value to be 54.66
    //the internet says it is 53
    private static final double CAMERA_WIDTH_DEGREES = 53;
    //The virtual port of the camera-check on roborio webdash
    private static final String CAMERA_PORT = "cam2";
    //frame is the one that comes directly off the camera
    private NIVision.Image frame;
    //binaryFrame has some filters run on it (Ex. HSV, particleFilter)
    private NIVision.Image binaryFrame;
    //particleBinaryFrame is the one we use to measure values
    private NIVision.Image particleBinaryFrame;
    //current frame's resolution
    private double frameWidth;
    private double frameHeight;
    //the camera object itself
    private USBCamera camera;
    //exposure and brightness when doing vision processing
    //this is necessary to remove noise
    private int exposure = 1;
    private int brightness = 1;

    private boolean manualMode = true;
    //image processing criteria array
    private NIVision.ParticleFilterCriteria2 criteria[];
    private NIVision.ParticleFilterOptions2 filterOptions;
    //RUN METHOD FLAGS
    //automatically put the image to the dashboard
    private boolean automaticCaptureAndUpdate = true;
    //get a new frame and process it
    private boolean updateAndGetNewFrame = false;
    //this is a flag to switch into the opposite camera mode
    private boolean switchMode = false;

    private ParticleReport target = null;

    public ShakerCamera() {
        //create camera object
        camera = new USBCamera(CAMERA_PORT);
        //start capture basically like an init method
        camera.startCapture();
        binaryFrame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_U8, 0);
        particleBinaryFrame = NIVision.imaqCreateImage(NIVision.ImageType.IMAGE_U8, 0);
        //put HSV filter values on the smartDashboard
        SmartDashboard.putNumber("H min", 20);
        SmartDashboard.putNumber("H max", 149);
        SmartDashboard.putNumber("S min", 78);
        SmartDashboard.putNumber("S max", 255);
        SmartDashboard.putNumber("V min", 90);
        SmartDashboard.putNumber("V max", 255);
        //initalize the processing criteria
        criteria = new NIVision.ParticleFilterCriteria2[1];
        criteria[0] = new NIVision.ParticleFilterCriteria2(NIVision.MeasurementType.MT_AREA_BY_IMAGE_AREA, 7,
                100.0, 0, 0);
        // set the lower threshold on percent target to image area in the criteria filter
        criteria[0].lower = 0.3f;
        filterOptions = new NIVision.ParticleFilterOptions2(0, 0, 1, 1);
        //Smart dahsboard value to debug the image
        SmartDashboard.putBoolean("Debug Image", false);
    }

    public void run() {
        firstTimeRunInit();
        while (true) {
            try {
                if (automaticCaptureAndUpdate) {
                    camera.getImage(frame);
                    if (frame != null) {
                        //get and update the current size of the image
                        NIVision.GetImageSizeResult imageSize = NIVision.imaqGetImageSize(frame);
                        frameWidth = imageSize.width;
                        frameHeight = imageSize.height;
                        if (SmartDashboard.getBoolean("Debug Image")) {
                            //If the robot is in debugging image mode show the camera image
                            measureAndGetParticles();
                            drawCrossHairs();
                            CameraServer.getInstance().setImage(binaryFrame);
                        }
                    }
                } else {
                    if (updateAndGetNewFrame) {
                        synchronized (this) {
                            System.out.println("Grabbing new frame and processing");
                            updateAndGetNewFrame = false;
                            camera.getImage(frame);
                            target = internalGetTarget();
                            notify();
                        }
                    }
                }
                if (automaticCaptureAndUpdate) {
                    if (switchMode) {
                        if (!manualMode) {
                            manualMode = true;
                            setCameraSettings(brightness, exposure);
                        } else {
                            manualMode = false;
                            setCameraSettingsAutomatic();
                        }
                        switchMode = false;
                    }
                }
                Thread.sleep(100);//run at a 100 hz
            } catch (VisionException | InterruptedException npe) {
                //if something goes wrong and causes the camera to crash then
                //print out the error and restart the code for the camera
                System.out.println("Vision ERROR: " + npe.getMessage());
                run();
            }
        }
    }

    /******************
     * INTERNAL RUN METHODS
     ****************/

    private void firstTimeRunInit() {
        //This is everything that needs to be run the first time on the run method
        System.out.println("Starting the camera thread....");
        try {
            camera.setFPS(20);
            //turn down exposure and brightness to reduce vision noise
            camera.setBrightness(brightness);
            camera.setExposureManual(exposure);
            //Set the resolution of the camera
            camera.setSize(640, 480);
        } catch (VisionException e) {
            e.printStackTrace();
            //re-run if something goes wrong in the init
            run();
        }
    }

    private void setCameraSettings(int cameraBrightness, int cameraExposure) {
        //sets the brightness and exposure really low for vision processing
        camera.setExposureManual(cameraExposure);
        camera.setBrightness(cameraBrightness);
        camera.updateSettings();
    }

    private void setCameraSettingsAutomatic() {
        //sets the camera input to automatic exposure and higher brightness then pushes them to the camera
        camera.setExposureAuto();
        camera.setBrightness(25);
        camera.updateSettings();
    }


    private void drawCrossHairs() {
        //draws cross hairs for debugging purposes
        NIVision.imaqDrawLineOnImage(binaryFrame, binaryFrame, NIVision.DrawMode.DRAW_VALUE,
                new NIVision.Point((int) frameWidth / 2, 0), new NIVision.Point((int) frameWidth / 2, (int) frameHeight), 100f);
        NIVision.imaqDrawLineOnImage(binaryFrame, binaryFrame, NIVision.DrawMode.DRAW_VALUE,
                new NIVision.Point(0, (int) frameHeight / 2), new NIVision.Point((int) frameWidth, (int) frameHeight / 2), 100f);
    }

    /**
     * finds the best target based on area and width
     *
     * @return class with values on the target
     */
    private ParticleReport internalGetTarget() {
        ArrayList<ParticleReport> reports = measureAndGetParticles();
        if (reports == null || reports.size() == 0) {
            System.out.println("The camera reports are empty");
            return null;
        }
        int targetLoc = 0;
        if (reports.size() != 1) {
            double maxPercentArea = 0;
            int counter = 0;
            for (ParticleReport par : reports) {
                // this shouldn't be happening but it is
                if (par == null)
                    continue;
                double width = Math.abs(par.BoundingRectLeft - par.BoundingRectRight);
                double height = Math.abs(par.BoundingRectTop - par.BoundingRectBottom);
                double widthToHeight = width / height;
                if (maxPercentArea * 1.20 < par.PercentAreaToImageArea
                        && (widthToHeight > 0.85 && widthToHeight < 1.3)) {
                    maxPercentArea = par.PercentAreaToImageArea;
                    targetLoc = counter;
                }
                counter++;
            }
        }
        ParticleReport par = reports.get(targetLoc);
        // creates a rectangle to cover the target
        NIVision.Rect r = new NIVision.Rect((int) par.BoundingRectTop, (int) par.BoundingRectLeft,
                Math.abs((int) (par.BoundingRectTop - par.BoundingRectBottom)),
                Math.abs((int) (par.BoundingRectLeft - par.BoundingRectRight)));
        // draws the rectangle on the binary image
        NIVision.imaqDrawShapeOnImage(binaryFrame, binaryFrame, r, NIVision.DrawMode.DRAW_VALUE,
                NIVision.ShapeMode.SHAPE_RECT, 20f);// highlight the choosen
        // target in a different
        // color
        return par;
    }

    /**
     * Takes an image and removes noise and find possible targets
     *
     * @return an arraylist with infomration on every possible blob in the fov
     */
    private ArrayList<ParticleReport> measureAndGetParticles() {
        //Store measured values from the particles
        ArrayList<ParticleReport> particles = new ArrayList<ParticleReport>();
        //Run a threshold on the image to get the specific color we want
        NIVision.imaqColorThreshold(binaryFrame, frame, 255, NIVision.ColorMode.HSV,
                new NIVision.Range((int) SmartDashboard.getNumber("H min"), (int) SmartDashboard.getNumber("H max")),
                new NIVision.Range((int) SmartDashboard.getNumber("S min"), (int) SmartDashboard.getNumber("S max")),
                new NIVision.Range((int) SmartDashboard.getNumber("V min"), (int) SmartDashboard.getNumber("V max")));
        //remove the noise that still exists
        NIVision.imaqParticleFilter4(particleBinaryFrame, binaryFrame, criteria, filterOptions,
                null);
        // count the number of viable particles
        int numParticles = NIVision.imaqCountParticles(particleBinaryFrame, 1);
        // checks to make sure there is at least one particle
        if (numParticles > 0) {
            // Measure each of the particles
            for (int particleIndex = 0; particleIndex < numParticles; particleIndex++) {
                // iterates through the particles and measures them
                ParticleReport par = new ParticleReport();
                // adds the particle report to the arraylist
                particles.add(par);

                // area of the particle
                par.Area = NIVision.imaqMeasureParticle(particleBinaryFrame, particleIndex, 0,
                        NIVision.MeasurementType.MT_AREA);
                // Y value of the upper part of box
                par.BoundingRectTop = NIVision.imaqMeasureParticle(particleBinaryFrame, particleIndex, 0,
                        NIVision.MeasurementType.MT_BOUNDING_RECT_TOP);
                // X value of the left part of box
                par.BoundingRectLeft = NIVision.imaqMeasureParticle(particleBinaryFrame, particleIndex, 0,
                        NIVision.MeasurementType.MT_BOUNDING_RECT_LEFT);
                // Y value of the bottom part of box
                par.BoundingRectBottom = NIVision.imaqMeasureParticle(particleBinaryFrame, particleIndex, 0,
                        NIVision.MeasurementType.MT_BOUNDING_RECT_BOTTOM);
                // X value of the right part of box
                par.BoundingRectRight = NIVision.imaqMeasureParticle(particleBinaryFrame, particleIndex, 0,
                        NIVision.MeasurementType.MT_BOUNDING_RECT_RIGHT);
                //calculate the middle in the X dir of the particle
                par.CenterOfMassX = par.BoundingRectRight + par.BoundingRectLeft;
                par.CenterOfMassX /= 2;
                //calculate the middle in the y dir of the particle
                par.CenterOfMassY = par.BoundingRectTop + par.BoundingRectBottom;
                par.CenterOfMassY /= 2;
                // calculate the angle from the middle
                double angleFromMiddle = CAMERA_WIDTH_DEGREES * getNormalizedCenterOfMassX(par.CenterOfMassX);
                par.optimalTurnAngle = angleFromMiddle / 2;
                // creates a rectangle to cover the target
                NIVision.Rect r = new NIVision.Rect((int) par.BoundingRectTop, (int) par.BoundingRectLeft,
                        Math.abs((int) (par.BoundingRectTop - par.BoundingRectBottom)),
                        Math.abs((int) (par.BoundingRectLeft - par.BoundingRectRight)));
                // draws the rectangle on the binary image
                NIVision.imaqDrawShapeOnImage(binaryFrame, binaryFrame, r, NIVision.DrawMode.DRAW_VALUE,
                        NIVision.ShapeMode.SHAPE_RECT, 125f);

                // put the important values to the dashboard
                SmartDashboard.putNumber("Theta diff", par.optimalTurnAngle);
                SmartDashboard.putNumber("center of mass x", par.CenterOfMassX);
                SmartDashboard.putNumber("Boudnding rect top", par.BoundingRectTop);
                SmartDashboard.putNumber("Normalized center of mass x",
                        getNormalizedCenterOfMassX(par.CenterOfMassX));
            }
        }

        return particles;
    }

    private double getNormalizedCenterOfMassX(double currentCenterInPixels) {
        return ((2 * currentCenterInPixels) / frameWidth) - 1;
    }

    /******************
     * END INTERNAL RUN METHODS
     ****************/

    public void getNextFrame() {
        updateAndGetNewFrame = true;
    }

    public ParticleReport getTarget() {
        return target;
    }

    /**
     * this is for normal camera use and it automatically puts images onto the dashboard
     */
    public void setAutomaticCaptureAndUpdate() {
        automaticCaptureAndUpdate = true;
    }

    /**
     * this is for vision processing, it sets it to a mode where each images has to be requested for
     */
    public void setManualCapture() {
        automaticCaptureAndUpdate = false;
    }

    public void setCameraMode(boolean manual) {
        //if it is already in the desired mode do nothing and break out of method
        if (manualMode == manual)
            return;
        //if the modes don't match then turn the flag swtich to true
        if (manual != manualMode)
            switchMode = true;
    }

    public void switchMode() {
        switchMode = true;
    }

    public class ParticleReport {
        // a class just to busy values of the particles
        public double optimalTurnAngle;
        double PercentAreaToImageArea;
        double Area;
        double BoundingRectLeft;
        double BoundingRectTop;
        double BoundingRectRight;
        double BoundingRectBottom;
        double CenterOfMassX;
        double CenterOfMassY;
    }
}

