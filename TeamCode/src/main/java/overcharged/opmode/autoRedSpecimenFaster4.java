package overcharged.opmode;

import static overcharged.config.RobotConstants.TAG_SL;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import overcharged.components.RobotMecanum;
import overcharged.components.colorSensor;
import overcharged.drive.SampleMecanumDrive;
import overcharged.pedroPathing.follower.Follower;
import overcharged.pedroPathing.localization.Pose;
import overcharged.pedroPathing.pathGeneration.BezierLine;
import overcharged.pedroPathing.pathGeneration.BezierPoint;
import overcharged.pedroPathing.pathGeneration.Path;
import overcharged.pedroPathing.pathGeneration.PathChain;
import overcharged.pedroPathing.pathGeneration.Point;
import overcharged.pedroPathing.util.Timer;

@Autonomous(name = "red specimen +4 faster faster", group = "1Autonomous")
public class autoRedSpecimenFaster4 extends OpMode {
    boolean vslideGoBottom = false;
    boolean hSlideGoBottom = false;
    boolean in = true;
    // Init
    private RobotMecanum robot;
    private DigitalChannel hlimitswitch;
    private DigitalChannel vlimitswitch;
    FtcDashboard dashboard = FtcDashboard.getInstance();
    SampleMecanumDrive drive;
    MultipleTelemetry telems;
    private ElapsedTime pathTimer;

    // Other init
    private int pathState;

    // LOCATIONS
    // GUIDE:
    // (0,0) is the corner. (144, 144) is the opposite.

    // TODO: Here are all the Sample Poses
    // Blue side Blue Element Poses
    //
    private Pose blueBlueLeftSample = new Pose(2.5+9.75+10.5, 4*24+1.5);
    private Pose blueBlueMidSample = new Pose(2.5+9.75, 4*24+1.5);
    private Pose blueBlueRightSample = new Pose(2.5, 4*24+1.5);
    // Blue side Neutral Element Poses
    //
    private Pose blueNeutLeftSample = new Pose(144-2.5, 4*24+1.5);
    private Pose blueNeutMidSample = new Pose(144-2.5-9.75, 4*24+1.5);
    private Pose blueNeutRightSample = new Pose(144-2.5-9.75-10.5, 4*24+1.5);
    // Red Side Red Element Poses
    //
    private Pose redRedLeftSample = new Pose(144-2.5-9.75-10.5, 2*24-2.5);
    private Pose redRedMidSample = new Pose(144-2.5-9.75, 2*24-2.5);
    private Pose redRedRightSample = new Pose(144-2.5, 2*24-2.5);
    // Red Side Neutral Element Poses
    //
    private Pose redNeutLeftSample = new Pose(2.5, 2*24-2.5);
    private Pose redNeutMidSample = new Pose(2.5+9.75, 2*24-2.5);
    private Pose redNeutRightSample = new Pose(2.5+9.75+10.5, 2*24-2.5);

    // TODO: Here are the Basket and Observation Zone positions
    // Blue side Left Basket Pose
    private Pose blueLeftBasket = new Pose();
    // Blue side Right Basket Pose
    private Pose blueRightBasket = new Pose();
    // Red side Left Basket Pose
    private Pose redLeftBasket = new Pose();
    // Red side Right Basket Pose
    private Pose redRightBasket = new Pose();

    // OTHER POSES
    private Pose beforeSpecimen, atSpecimen, backUp, goPark, goForward, goRotate, bitForward, bitBack, toSample, secondScore, bitCloser, bitBitBack, thirdSample, getThirdSample, thirdScore, thirdScoreCloser, fourthScore, fourthScoreCloser;
    private Pose startPose = new Pose(135, 66, Math.PI);

    private Path redPark, redPark2, slightMove, nextRotate, bitRotate, toSample2, grabSample, nextSample, getCloser, getGetBack, toSample3, grabSample3, scoreSample3, scoredSample3, scoreSample4, scoredSample4;

    private PathChain preload;

    private Follower follower;

    //TODO: Starting from here are the poses for the paths
    public void firstSpecimen(){
        //beforeBucket = new Pose(-10,-10,Math.PI/4);
        beforeSpecimen = new Pose(111,68,Math.PI);
        // atSpecimen = new Pose(117,70,0);
        goForward = new Pose(130,68, Math.PI);
        backUp = new Pose(119,68, Math.PI);
        goPark = new Pose(118,84, 3*Math.PI/4);
        goRotate = new Pose(115,93, Math.PI/4);
        bitForward = new Pose(117,95, 2*Math.PI/3);
        bitBack = new Pose(114,93, Math.PI/4);
        toSample = new Pose(115,98, 2*Math.PI/3);
        secondScore = new Pose(114,93, Math.PI/4);
        bitCloser = new Pose(131,96, Math.PI);
        bitBitBack = new Pose(109,63, Math.PI);
        thirdSample = new Pose(131,96, Math.PI);
        getThirdSample = new Pose(109,65, Math.PI);
        thirdScore = new Pose(131,96, Math.PI);
        thirdScoreCloser = new Pose(109,67, Math.PI);
        fourthScore = new Pose(129,98, Math.PI);
        fourthScoreCloser = new Pose(114,64, Math.PI);


    }


    //TODO: here are where the paths are defined
    public void buildPaths() {
        /*
        firstScore = new Path(new BezierLine(new Point(startPose),new Point(beforeBucket)));
        firstScore.setConstantHeadingInterpolation(Math.PI/2);

        inchBucket = new Path(new BezierLine(new Point(beforeBucket), new Point(ready2Score)));
        inchBucket.setConstantHeadingInterpolation(3*Math.PI/4);

         */

        preload = follower.pathBuilder()
                .addPath(new BezierLine(new Point(goForward),new Point(beforeSpecimen)))
                //.setConstantHeadingInterpolation(startPose.getHeading())
                .setLinearHeadingInterpolation(goForward.getHeading(), beforeSpecimen.getHeading())
                .setPathEndTimeoutConstraint(0)
                .build();

        // slightMove = new Path(new BezierLine(new Point(beforeSpecimen), new Point(atSpecimen)));
        //slightMove.setConstantHeadingInterpolation(0);

        slightMove = new Path(new BezierLine(new Point(startPose), new Point(goForward)));
        slightMove.setConstantHeadingInterpolation(Math.PI);
        redPark = new Path(new BezierLine(new Point(beforeSpecimen), new Point(backUp)));
        redPark.setConstantHeadingInterpolation(Math.PI);
        redPark2 = new Path(new BezierLine(new Point(backUp), new Point(goPark)));
        redPark2.setConstantHeadingInterpolation(3*Math.PI/4);
        nextRotate = new Path(new BezierLine(new Point(goPark), new Point(goRotate)));
        nextRotate.setConstantHeadingInterpolation(Math.PI/4);
        bitRotate = new Path(new BezierLine(new Point(goRotate), new Point(bitForward)));
        bitRotate.setConstantHeadingInterpolation(2*Math.PI/3);
        toSample2 = new Path(new BezierLine(new Point(bitForward), new Point(bitBack)));
        toSample2.setConstantHeadingInterpolation(Math.PI/4);
        grabSample = new Path(new BezierLine(new Point(bitBack), new Point(toSample)));
        grabSample.setConstantHeadingInterpolation(2*Math.PI/3);
        nextSample = new Path(new BezierLine(new Point(toSample), new Point(secondScore)));
        nextSample.setConstantHeadingInterpolation(Math.PI/4);
        getCloser = new Path(new BezierLine(new Point(secondScore), new Point(bitCloser)));
        getCloser.setConstantHeadingInterpolation(Math.PI);
        getGetBack = new Path(new BezierLine(new Point(bitCloser), new Point(bitBitBack)));
        getGetBack.setConstantHeadingInterpolation(Math.PI);
        toSample3 = new Path(new BezierLine(new Point(bitBitBack), new Point(thirdSample)));
        toSample3.setConstantHeadingInterpolation(Math.PI);
        grabSample3 = new Path(new BezierLine(new Point(thirdSample), new Point(getThirdSample)));
        grabSample3.setConstantHeadingInterpolation(Math.PI);
        scoreSample3 = new Path(new BezierLine(new Point(getThirdSample), new Point(thirdScore)));
        scoreSample3.setConstantHeadingInterpolation(Math.PI);
        scoredSample3 = new Path(new BezierLine(new Point(thirdScore), new Point(thirdScoreCloser)));
        scoredSample3.setConstantHeadingInterpolation(Math.PI);
        scoreSample4 = new Path(new BezierLine(new Point(thirdScoreCloser), new Point(fourthScore)));
        scoreSample4.setConstantHeadingInterpolation(Math.PI);
        scoredSample4 = new Path(new BezierLine(new Point(fourthScore), new Point(fourthScoreCloser)));
        scoredSample4.setConstantHeadingInterpolation(Math.PI);

    }




    // TODO: HERE IS WHERE THE MAIN PATH IS
    // Main pathing
    public void autoPath() {
        switch (pathState) {
            // Auto Body
            //
            case 10: // scores initial specimen
                pathTimer.reset();
                robot.claw.setClose();
                waitFor(300);
                robot.vSlides.moveEncoderTo(robot.vSlides.mid+50, 1.2f);
                follower.followPath(preload);
                robot.clawBigTilt.setOut();
                robot.depoHslide.setOut();
                robot.clawSmallTilt.setFlat();
                setPathState(13);
                break;
            case 13: // scores initial specimen
                if(!follower.isBusy()) {
                    waitFor(200);
                    robot.claw.setOpen();
                    setPathState(14);
                }
                break;
            case 14:
                if(!follower.isBusy()) {
                    waitFor(100);
                    follower.followPath(redPark);
                    redPark.setLinearHeadingInterpolation(backUp.getHeading(), Math.toRadians(180));
                    robot.depoHslide.setInit();
                    waitFor(200);
                    robot.intakeTilt.setOut();
                    robot.depoWrist.setIn();
                    robot.claw.setOpen();
                    robot.clawBigTilt.setTransfer();
                    robot.clawSmallTilt.setTransfer();
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid-50, 1f);
                    vslideGoBottom = true;
                    setPathState(16);
                }
                break;
            /*case 15:
                if(!follower.isBusy()) {
                    robot.depoHslide.setInit();
                    robot.intakeTilt.setOut();
                    robot.depoWrist.setIn();
                    robot.claw.setOpen();
                    robot.clawBigTilt.setTransfer();
                    robot.clawSmallTilt.setTransfer();
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid-50, 1f);
                    vslideGoBottom = true;
                    setPathState(16);
                }
                break;*/
            case 16:
                if(!follower.isBusy()) {
                    waitFor(100);
                    follower.followPath(redPark2);
                    setPathState(17);
                }
                break;
            case 17:
                if(!follower.isBusy()) {
                    robot.intakeTilt.setOut();
                    robot.intake.in();
                    robot.latch.setOut();
                    in = false;
                    robot.hslides.moveEncoderTo(500, 0.8f);
                    setPathState(18);
                }
                break;
            case 18:
                if(robot.sensorF.getColor() == colorSensor.Color.RED) {
                    follower.followPath(nextRotate);
                    setPathState(19);
                }
                else if (pathTimer.milliseconds()>5000) {
                    pathTimer.reset();
                    robot.intake.off();
                    robot.intakeTilt.setTransfer();
                    hSlideGoBottom = true;
                    setPathState(20);
                }
                break;
            case 19:
                if(!follower.isBusy()){
                    //follower.holdPoint(new BezierPoint(new Point(goRotate)), Math.toRadians(45));
                    robot.intake.in();
                    waitFor(10);
                    robot.intake.out();
                    waitFor(40);
                    setPathState(20);
                }
                break;
            case 20:
                if(robot.sensorF.getColor() == colorSensor.Color.NONE){
                    robot.intake.in();
                    follower.followPath(bitRotate);
                    setPathState(21);
                }
                break;
            case 21:
                if(!follower.isBusy()) {
                    follower.holdPoint(new BezierPoint(new Point(bitForward)), Math.toRadians(132));
                    if (robot.sensorF.getColor() == colorSensor.Color.RED) {
                        follower.followPath(toSample2);
                        setPathState(22);
                    }
                    else if (pathTimer.milliseconds()>5000) {
                        pathTimer.reset();
                        robot.intake.off();
                        robot.intakeTilt.setTransfer();
                        hSlideGoBottom = true;
                        setPathState(23);
                    }
                    break;
                }
            case 22:
                if(!follower.isBusy()){
                    robot.intake.in();
                    waitFor(10);
                    robot.intake.out();
                    waitFor(40);
                    setPathState(23);
                }
                break;
            case 23:
                if(robot.sensorF.getColor() == colorSensor.Color.NONE){
                    robot.intakeTilt.setTransfer();
                    hSlideGoBottom = true;
                    robot.claw.setOpen();
                    robot.clawSmallTilt.setWall();
                    robot.clawBigTilt.setWall();
                    robot.intake.off();
                    setPathState(24);
                }
                break;
            case 24:
                if(!follower.isBusy()) {
                    follower.followPath(getCloser);
                    setPathState(25);
                }
                break;
            case 25:
                if(!follower.isBusy()) {
                    follower.holdPoint(new BezierPoint(new Point(bitCloser)), Math.toRadians(180));
                    robot.claw.setClose();
                    setPathState(26);
                }
                break;
            case 26: // scores initial specimen
                if(!follower.isBusy()) {
                    follower.followPath(getGetBack);
                    robot.vSlides.moveEncoderTo(80, 1.2f);
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid+90, 1.2f);
                    robot.claw.setClose();
                    robot.clawBigTilt.setOut();
                    robot.depoHslide.setOut();
                    robot.clawSmallTilt.setFlat();
                    setPathState(27);
                }
                break;
            case 27: // scores initial specimen
                if(!follower.isBusy()) {
                    follower.holdPoint(new BezierPoint(new Point(bitBitBack)), Math.toRadians(180));
                    robot.claw.setOpen();
                    waitFor(150);
                    follower.followPath(toSample3);
                    robot.depoHslide.setInit();
                    waitFor(200);
                    robot.intakeTilt.setOut();
                    robot.depoWrist.setIn();
                    robot.claw.setOpen();
                    robot.clawBigTilt.setWall();
                    robot.clawSmallTilt.setWall();
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid-50, 1f);
                    vslideGoBottom = true;
                    setPathState(28);
                }
                break;
            case 28:
                if(!follower.isBusy()) {
                    follower.holdPoint(new BezierPoint(new Point(thirdSample)), Math.toRadians(180));
                    robot.claw.setClose();
                    setPathState(29);
                }
                break;
            case 29: // scores initial specimen
                if(!follower.isBusy()) {
                    follower.followPath(grabSample3);
                    robot.vSlides.moveEncoderTo(80, 1.2f);
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid+90, 1.2f);
                    robot.claw.setClose();
                    robot.clawBigTilt.setOut();
                    robot.depoHslide.setOut();
                    robot.clawSmallTilt.setFlat();
                    setPathState(30);
                }
                break;
            case 30: // scores initial specimen
                if(!follower.isBusy()) {
                    follower.holdPoint(new BezierPoint(new Point(getThirdSample)), Math.toRadians(180));
                    robot.claw.setOpen();
                    waitFor(150);
                    follower.followPath(scoreSample3);
                    robot.depoHslide.setInit();
                    waitFor(200);
                    robot.intakeTilt.setOut();
                    robot.depoWrist.setIn();
                    robot.claw.setOpen();
                    robot.clawBigTilt.setWall();
                    robot.clawSmallTilt.setWall();
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid-50, 1f);
                    vslideGoBottom = true;
                    setPathState(31);
                }
                break;
            case 31:
                if(!follower.isBusy()) {
                    follower.holdPoint(new BezierPoint(new Point(thirdScore)), Math.toRadians(180));
                    robot.claw.setClose();
                    setPathState(32);
                }
                break;
            case 32: // scores initial specimen
                if(!follower.isBusy()) {
                    follower.followPath(scoredSample3);
                    robot.vSlides.moveEncoderTo(80, 1.2f);
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid+90, 1.2f);
                    robot.claw.setClose();
                    robot.clawBigTilt.setOut();
                    waitFor(200);
                    robot.depoHslide.setOut();
                    robot.clawSmallTilt.setFlat();
                    setPathState(33);
                }
                break;
            case 33: // scores initial specimen
                if(!follower.isBusy()) {
                    follower.holdPoint(new BezierPoint(new Point(thirdScoreCloser)), Math.toRadians(180));
                    robot.claw.setOpen();
                    waitFor(150);
                    follower.followPath(scoreSample4);
                    robot.depoHslide.setInit();
                    waitFor(200);
                    robot.intakeTilt.setOut();
                    robot.depoWrist.setIn();
                    robot.claw.setOpen();
                    robot.clawBigTilt.setTransfer();
                    robot.clawSmallTilt.setTransfer();
                    robot.vSlides.moveEncoderTo(robot.vSlides.mid-50, 1f);
                    vslideGoBottom = true;
                    setPathState(100);
                }
                break;


            case 100: // EMPTY TEST CASE
                //follower.holdPoint(new BezierPoint(firstScore.getLastControlPoint()), Math.toRadians(-90));
                telems.addLine("CASE 100 - IN TEST CASE!!");
                break;

        }
    }


    // path setter
    public void setPathState(int state){
        pathState = state;
        pathTimer.reset();
        autoPath();
    }

    // Distance Sensor Checker
    public void startDistanceSensorDisconnectDetection(int state) {
    }

    //loop de loop
    @Override
    public void loop() {
        follower.update();
        autoPath();
        telemetry.addLine("TValue: "+follower.getCurrentTValue());
        telemetry.addLine("Path: " + pathState);
        telemetry.addLine("vLimit" + vlimitswitch.getState());
        telemetry.addLine("hLimit" + hlimitswitch.getState());

        //functions
        if (!hlimitswitch.getState() && hSlideGoBottom) {
            // in = true;
            robot.latch.setInit();
            robot.hslides.hslides.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.hslides.hslides.setPower(-1.3f);
            RobotLog.ii(TAG_SL, "Going down");
        } else if (hlimitswitch.getState() && hSlideGoBottom) {
            robot.latch.setInit();
            // robot.intakeTilt.setTransfer();
            robot.hslides.hslides.setPower(0);
            robot.hslides.hslides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            hSlideGoBottom = false;
            in = true;
            RobotLog.ii(TAG_SL, "Force stopped");
        }

        if (vlimitswitch.getState() && vslideGoBottom) {
            robot.vSlides.vSlidesL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setPower(-1);
            robot.vSlides.vSlidesL.setPower(-1);
            RobotLog.ii(TAG_SL, "Going down");
        } else if (!vlimitswitch.getState() && vslideGoBottom) {
            //robot.hslides.forceStop();
            robot.vSlides.vSlidesR.setPower(0);
            robot.vSlides.vSlidesL.setPower(0);
            robot.vSlides.vSlidesL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            vslideGoBottom = false;
            RobotLog.ii(TAG_SL, "Force stopped");
        }
    }

    // initialize robot
    @Override
    public void init() {

        // Robot things init
        telems = new MultipleTelemetry(dashboard.getTelemetry(), telemetry);
        robot = new RobotMecanum(this, true, false);
        drive = new SampleMecanumDrive(hardwareMap);
        pathTimer = new ElapsedTime();



        //follower init
        follower = new Follower(hardwareMap);
        follower.setStartingPose(startPose);


        //Pose init
        firstSpecimen();
        buildPaths();

        //robot init
        robot.intakeTilt.setOut();
        robot.clawBigTilt.setOut();
        robot.clawSmallTilt.setFlat();
        robot.clawSmallTilt.setWall();
        robot.depoWrist.setIn();
        robot.claw.setClose();

        hlimitswitch = hardwareMap.get(DigitalChannel.class, "hlimitswitch");
        vlimitswitch = hardwareMap.get(DigitalChannel.class, "vlimitswitch");;

    }

    //loop de loop but initialized
    /*@Override
    public void init_loop() {

    }*/

    @Override
    public void start() {
        // starts auto paths
        setPathState(10);

        // safety net if auto doesn't start for some reason
        autoPath();
    }

    public static void waitFor(int milliseconds) { //Waitor Function
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < milliseconds) {
            // loop
        }
    }
}

