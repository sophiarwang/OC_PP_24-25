package overcharged.opmode;

import static overcharged.config.RobotConstants.TAG_SL;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.RobotLog;

import overcharged.components.RobotMecanum;
import overcharged.components.colorSensor;
import overcharged.drive.SampleMecanumDrive;
import overcharged.linear.util.SelectLinear;

import overcharged.pedroPathing.follower.Follower;
import overcharged.pedroPathing.localization.Pose;
import overcharged.pedroPathing.pathGeneration.BezierCurve;
import overcharged.pedroPathing.pathGeneration.BezierLine;
import overcharged.pedroPathing.pathGeneration.BezierPoint;
import overcharged.pedroPathing.pathGeneration.MathFunctions;
import overcharged.pedroPathing.pathGeneration.Path;
import overcharged.pedroPathing.pathGeneration.PathChain;
import overcharged.pedroPathing.pathGeneration.Point;
import overcharged.pedroPathing.util.Timer;

// Main Class
@Autonomous(name = "less turn bucket", group = "Autonomous")
public class autoBucketNT extends OpMode{

    //stuff
    boolean vslideGoBottom = false;
    boolean hSlideGoBottom = false;
    boolean scored = false;
    boolean in = true;

    long totalTime;

    int floorRep = 3;

    // Init
    private RobotMecanum robot;
    private DigitalChannel hlimitswitch;
    private DigitalChannel vlimitswitch;
    FtcDashboard dashboard = FtcDashboard.getInstance();
    SampleMecanumDrive drive;
    MultipleTelemetry telems;
    private Timer pathTimer, opmodeTimer, scanTimer, distanceSensorUpdateTimer, distanceSensorDecimationTimer;

    // Other init
    private int pathState;

    // LOCATIONS
    // GUIDE:
    // (0,0) is the corner. (144, 144) is the opposite.

    // OTHER POSES
    private Pose initBucket, beforeBucket, ready2Score, wallScore, beforeBucket2, beforeBucket3;
    private Pose startPose = new Pose(136, 32, Math.toRadians(90));

    private Path firstScore, inchBucket, goSafe, goBack, floorCycle, secondBack;

    private PathChain preload;

    private Follower follower;

    //TODO: Starting from here are the poses for the paths
    public void firstBucket(){
        beforeBucket = new Pose(120,24, Math.PI);
        beforeBucket2 = new Pose(120,14, Math.PI);
        ready2Score = new Pose(136,18.5,Math.PI/2);
        wallScore = new Pose(125,8.2, Math.PI);
    }


    //TODO: here are where the paths are defined
    public void buildPaths() {

        preload = follower.pathBuilder()
                .addPath(new BezierLine(new Point(startPose),new Point(ready2Score)))
                .setConstantHeadingInterpolation(ready2Score.getHeading())
                .setPathEndTimeoutConstraint(150)
                .build();



        goSafe = new Path(new BezierLine(new Point(ready2Score), new Point(beforeBucket)));
        goSafe.setConstantHeadingInterpolation(Math.PI);

        secondBack = new Path(new BezierLine(new Point(wallScore), new Point(beforeBucket2)));
        secondBack.setConstantHeadingInterpolation(Math.PI);

        floorCycle = new Path(new BezierLine(new Point(beforeBucket), new Point(wallScore)));
        floorCycle.setConstantHeadingInterpolation(Math.PI);
    }


    // TODO: HERE IS WHERE THE MAIN PATH IS
    // Main pathing
    public void autoPath() {
        switch (pathState) {
            // Auto Body
            //
            case 10: // scores initial specimen
                totalTime = pathTimer.getElapsedTime();
                robot.intakeTilt.setFlat();
                robot.vSlides.moveEncoderTo(robot.vSlides.high1, 1f);
                pathTimer.resetTimer();
                follower.followPath(preload, true);
                follower.setMaxPower(0.7);
                setPathState(12);
                break;
            case 12:
                if(pathTimer.getElapsedTime()>600){
                    pathTimer.resetTimer();
                    robot.clawSmallTilt.setOut();
                    waitFor(300);
                    robot.depoWrist.setOut();
                    waitFor(200);
                    setPathState(13);
                }
                break;
            case 13:
                waitFor(400);
                robot.clawBigTilt.setBucket();
                robot.clawSmallTilt.setLeft();
                robot.depoHslide.setInit();
                if(Math.abs(robot.vSlides.vSlidesL.getCurrentPosition()-robot.vSlides.high1) < 20 && follower.getPose().getX() > (ready2Score.getX() - 1) && follower.getPose().getY() > (ready2Score.getY() - 1)){
                    waitFor(90);
                    robot.claw.setBig();
                    scored = true;
                    setPathState(14);
                }
                break;
            case 14:
                waitFor(350);
                robot.depoWrist.setIn();
                waitFor(450);
                robot.claw.setOpen();
                robot.clawBigTilt.setTransfer();
                robot.clawSmallTilt.setTransfer();
                if(!follower.isBusy()&&scored) {
                    scored = false;
                    pathTimer.resetTimer();
                    if(floorRep == 3) {
                        follower.followPath(goSafe, true);
                        follower.setMaxPower(0.9);
                        goSafe.setLinearHeadingInterpolation(ready2Score.getHeading(), Math.toRadians(180));
                        vslideGoBottom = true;
                        setPathState(16);
                    }
                    else if(floorRep==2){
                        follower.followPath(secondBack, true);
                        secondBack.setLinearHeadingInterpolation(wallScore.getHeading(), Math.toRadians(180));
                        vslideGoBottom = true;
                        setPathState(16);
                    }
                    else if(floorRep==1){
                        follower.followPath(secondBack, true);
                        secondBack.setLinearHeadingInterpolation(wallScore.getHeading(), Math.toRadians(190));
                        vslideGoBottom = true;
                        setPathState(16);
                    }
                }
                break;
            case 16:
                if(!follower.isBusy()) {
                    robot.latch.setOut();
                    robot.intakeTilt.setOut();
                    in = false;
                    if(floorRep>1) {
                        robot.hslides.moveEncoderTo(robot.hslides.PRESET1, 0.8f);
                    }
                    else if(floorRep==1) {
                        robot.hslides.moveEncoderTo(robot.hslides.PRESET3, 0.85f);
                    }
                    robot.intake.in();
                    waitFor(300);
                    setPathState(161);
                }
                break;
            case 161:
                if(robot.sensorF.getColor() == colorSensor.Color.YELLOW){
                    robot.intakeTilt.setHigh();
                    hSlideGoBottom = true;
                    robot.intake.in();
                    waitFor(450);
                    robot.intake.out();
                    setPathState(17);
                }
                else if (pathTimer.getElapsedTime()>1900){
                    robot.intake.off();
                    robot.intakeTilt.setTransfer();
                    hSlideGoBottom = true;
                    floorRep-=1;
                    scored = true;
                    setPathState(14);
                }
                break;
            case 17:
                waitFor(300);
                robot.intake.off();
                if(in) {
                    robot.intakeTilt.setTransfer();
                    follower.followPath(floorCycle, true);
                    follower.setMaxPower(0.7);
                    if(floorRep>1) {
                        floorCycle.setConstantHeadingInterpolation(Math.toRadians(180));
                    }
                    else if(floorRep==1) {
                        floorCycle.setLinearHeadingInterpolation(Math.toRadians(190), Math.PI);
                    }
                    waitFor(200);
                    robot.claw.setClose();
                    waitFor(250);
                    robot.intakeTilt.setFlat();
                    robot.vSlides.moveEncoderTo(robot.vSlides.high1, 1f);
                    waitFor(200);
                    robot.intakeTilt.setFlat();
                    robot.depoWrist.setOut();
                    pathTimer.resetTimer();
                    setPathState(171);
                }
                break;
            case 171:
                if(pathTimer.getElapsedTime()>800){
                    robot.clawBigTilt.setBucket();
                    robot.depoHslide.setInit();
                    robot.clawSmallTilt.setRight();
                    robot.intakeTilt.setTransfer();
                    setPathState(172);
                }
                break;
            case 1710:
                if(Math.abs(robot.vSlides.vSlidesL.getCurrentPosition()-robot.vSlides.high1) < 20 && follower.getPose().getX() > (ready2Score.getX() - 1) && follower.getPose().getY() > (ready2Score.getY() - 1)){
                    waitFor(50);
                    robot.claw.setBig();
                    scored = true;
                    setPathState(172);
                }
                break;
            case 172:
                if(!follower.isBusy()){
                    if (floorRep>0) {
                        floorRep-=1;
                        waitFor(300);
                        setPathState(14);
                    }
                    else{
                        vslideGoBottom = true;
                        setPathState(18);
                    }
                }
                break;
            case 18:
                waitFor(450);
                robot.depoWrist.setIn();
                waitFor(550);
                robot.claw.setOpen();
                robot.clawBigTilt.setTransfer();
                vslideGoBottom = true;
                robot.clawSmallTilt.setTransfer();
                setPathState(100);
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
        pathTimer.resetTimer();
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
        telemetry.addLine("Position: " + follower.getPose());
        telemetry.addLine("heading: " + follower.getTotalHeading());
        //telemetry.addLine("color: "+robot.sensorF.getColor());
        //telemetry.addLine("vLimit" + vlimitswitch.getState());
        //telemetry.addLine("hLimit" + hlimitswitch.getState());
        telemetry.addLine("Rep Count"+ floorRep);

        //functions
        if (!hlimitswitch.getState() && hSlideGoBottom) {
            in = true;
            robot.latch.setInit();
            robot.hslides.hslides.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.hslides.hslides.setPower(-1);
            RobotLog.ii(TAG_SL, "Going down");
        } else if (hlimitswitch.getState() && hSlideGoBottom) {
            robot.latch.setInit();
            robot.intakeTilt.setTransfer();
            robot.hslides.hslides.setPower(0);
            robot.hslides.hslides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            hSlideGoBottom = false;
            RobotLog.ii(TAG_SL, "Force stopped");
        }
        if (vlimitswitch.getState() && vslideGoBottom) {
            robot.vSlides.vSlidesL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setPower(-1);
            robot.vSlides.vSlidesL.setPower(-1);
            RobotLog.ii(TAG_SL, "Going down");
        } else if (!vlimitswitch.getState() && vslideGoBottom) {
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
        pathTimer = new Timer();



        //follower init
        follower = new Follower(hardwareMap);
        follower.setStartingPose(startPose);

        //Pose init
        firstBucket();
        buildPaths();

        //robot init
        hlimitswitch = hardwareMap.get(DigitalChannel.class, "hlimitswitch");
        vlimitswitch = hardwareMap.get(DigitalChannel.class, "vlimitswitch");
        robot.intakeTilt.setTransfer();
        robot.clawBigTilt.setTransfer();
        robot.clawSmallTilt.setTransfer();
        robot.claw.setClose();
    }

    //loop de loop but initialized
    @Override
    public void init_loop() {

    }

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