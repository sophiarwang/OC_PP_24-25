package overcharged.opmode;

import static overcharged.config.RobotConstants.TAG_SL;
import static overcharged.config.RobotConstants.TAG_T;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.util.RobotLog;

import overcharged.components.Button;
import overcharged.components.RobotMecanum;
import overcharged.components.colorSensor;
import overcharged.components.hslides;
import overcharged.components.vSlides;
import overcharged.pedroPathing.follower.Follower;
import overcharged.pedroPathing.pathGeneration.Vector;

// TODO:BUTTON MAP
/*
        --=BASE DRIVE=--
    (joysticks move the robot)

    *-Slides & Intake-*
    Right Bumper - hSlide mode On/Off
    Right Joystick Y - hSlide forward/back
    Right Trigger - Intake On/Off
    Left Trigger - Outtake On/Off
    Left Bumper - Transfer Up/Down
    y - reset hSlides and transfer

    *-Misc-*
    Touchpad - starts with only allow red, press to rotate between: - Only Red - Red & Yellow - Only Blue - Blue & Yellow -
    PS Button - factory reset

        --=ARM DRIVER=--
    (buttons do stuff yeah)

    *-Scoring-*
    a - claw Open/Close to Grab/Score
    Dpad Up - Higher Bucket
    Dpad Left - Specimen
    Left Bumper - Lower Bucket

    *-Intake-*
    Dpad Right - grab from wall

    *-Transfer-*
    Dpad Down - Reset vSlides & transfer

    *-Misc-*
    x - Slides go Slightly Down
    b - Slides go Slightly Up

 */

@Config
@TeleOp(name="outreach teleop", group="0Teleop")
public class teleOldOutreach extends OpMode {
    RobotMecanum robot;
    double slowPower = 1;
    long startTime;
    boolean hSlideGoBottom = false;
    long intakeTiltDelay;
    long depoDelay;
    long clawDelay;
    long outDelay;
    long outakeTime;
    long transferDelay;
    long intakeStop;
    int wallStep = 0;
    int resetStep = 0;
    int transferStep = 0;
    int intakeStep = 0;
    int modeCount = 1;
    int tempLocation;

    float turnConstant = 1f;

    boolean intakeDelay = false;
    boolean intakeOn = false;
    boolean intTiltDelay = false;
    boolean cDelay = false;
    boolean clawOpen = true;
    boolean hSlideisOut = false;
    boolean latched = true;
    boolean vslideGoBottom = false;
    boolean vslideOut = false;
    boolean vslideManual = false;
    boolean intakeTransfer = true;
    boolean dDelay = false;
    boolean intakeOutDelay = false;
    boolean sense = false;
    boolean highTransfer = false;
    boolean outH = false;
    boolean bucketSeq = false;
    private DigitalChannel hlimitswitch;
    private DigitalChannel vlimitswitch;
    IntakeMode intakeMode = IntakeMode.OFF;
    SlideLength slideLength = SlideLength.IN;
    SlideHeight slideHeight = SlideHeight.DOWN;
    ScoreType score = ScoreType.NONE;
    boolean latch = true;

    public enum IntakeMode {
        IN,
        OUT,
        OFF;
    }
    public enum SlideLength {
        IN,
        MID,
        LONG;
    }
    public enum SlideHeight {
        DOWN,
        WALL,
        MID,
        LOWER,
        HIGH1,
        HIGH2;
    }

    public enum ScoreType {
        BUCKET,
        SPECIMEN,
        NONE
    }

    @Override
    public void init() {
        gamepad1.setLedColor(255,0,0,500);

        try {
            telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
            robot = new RobotMecanum(this, false, false);
            startTime = System.currentTimeMillis();
            hlimitswitch = hardwareMap.get(DigitalChannel.class, "hlimitswitch");
            vlimitswitch = hardwareMap.get(DigitalChannel.class, "vlimitswitch");

            robot.setBulkReadManual();

            //robot.vSlides.vSlidesB.setTargetPositionPIDFCoefficients(21,0,0,0);
        } catch (Exception e) {
            RobotLog.ee(TAG_T, "Teleop init failed: " + e.getMessage());
            telemetry.addData("Init Failed", e.getMessage());
            telemetry.update();
        }

    }

    @Override
    public void loop() {
        // Clear bulk cache
        robot.clearBulkCache();
        long timestamp = System.currentTimeMillis();

        // Joystick and bumper handling
        double y = gamepad1.left_stick_y;
        double x = -gamepad1.left_stick_x * 1.1;
        double rx = -gamepad1.right_stick_x*turnConstant;
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

        // Check if left bumper is pressed to enable hslide control
        if (hSlideisOut) {
            // Use the left joystick Y-axis to control hslide movement
            float slidePower = -gamepad1.right_stick_y;  // Invert to match expected joystick behavior

            // Control the hslide movement with the joystick
            if (Math.abs(slidePower) > 0.1) { // Add deadzone check
                robot.hslides.hslides.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                robot.hslides.hslides.setPower(slidePower);
                hSlideisOut = true;
            } else {
                // Stop the hslides if joystick is not being pushed
                robot.hslides.hslides.setPower(0);
            }
        }

        // Regular robot movement control when left bumper is not pressed
        double frontLeftPower = ((y + x + rx) / denominator) * slowPower;
        double backLeftPower = ((y - x + rx) / denominator) * slowPower;
        double frontRightPower = ((y - x - rx) / denominator) * slowPower;
        double backRightPower = ((y + x - rx) / denominator) * slowPower;

        robot.driveLeftFront.setPower(frontLeftPower);
        robot.driveLeftBack.setPower(backLeftPower);
        robot.driveRightFront.setPower(frontRightPower);
        robot.driveRightBack.setPower(backRightPower);

        if(gamepad1.right_bumper && Button.SLIDE_RESET.canPress(timestamp)){
            outH = true;
            robot.clawBigTilt.setSlides();
            robot.latch.setOut();
            turnConstant = 0.60f;
            robot.hslides.moveEncoderTo(robot.hslides.OUT,1f);
            if (robot.hslides.getPower() == 0){
                hSlideisOut = true;
            }
        }

        // Logic for bringing hslides back in
        if (!hlimitswitch.getState() && hSlideGoBottom) {
            outH = false;
            turnConstant = 1f;
            robot.latch.setInit();
            robot.hslides.hslides.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.hslides.hslides.setPower(-1f);
            RobotLog.ii(TAG_SL, "Going down");
        } else if (hlimitswitch.getState() && hSlideGoBottom) {
            robot.intake.off();
            intakeMode = IntakeMode.OFF;
            robot.hslides.hslides.setPower(0);
            robot.latch.setInit();
            latched = true;
            robot.intakeTilt.setTransfer();
            robot.hslides.hslides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            hSlideisOut = false;
            clawDelay = System.currentTimeMillis();
            cDelay = true;
            hSlideGoBottom = false;
            sense = false;
            RobotLog.ii(TAG_SL, "Force stopped");
        }
        if (hlimitswitch.getState() && highTransfer){
            robot.intakeTilt.setTransfer();
            highTransfer = false;
        }

        // Change intake tilt
        if (gamepad1.left_bumper && Button.TRANSFER.canPress(timestamp)) {
            if (!intakeTransfer) {
                robot.intakeTilt.setTransfer();
                intakeTransfer = true;
                sense = false;
                if(hlimitswitch.getState() && latched) {
                    clawDelay = System.currentTimeMillis();
                    cDelay = true;
                    sense = true;
                }
            } else {
                robot.intakeTilt.setFlat();
                if(hSlideisOut) {
                    intakeTiltDelay = System.currentTimeMillis();
                    intTiltDelay = true;
                }
                else{
                    robot.intakeTilt.setInOut();
                }
                robot.intake.in();
                intakeMode = IntakeMode.IN;
                sense = true;
                intakeTransfer = false;
            }
        }

        if(intakeOutDelay){
            intakeOutDelay = false;
            robot.intake.in();
            intakeMode = IntakeMode.IN;
            intakeStep = 0;
            intakeStep++;
            outakeTime = System.currentTimeMillis();
        }
        if(intakeStep == 1 && System.currentTimeMillis()-outakeTime>250){ //210
            robot.intake.out();
            //HSLIDE BOTTOM
            hSlideGoBottom = true;
            intakeMode = IntakeMode.OUT;
            intakeStep++;
            outakeTime = System.currentTimeMillis();
        }
        if(intakeStep == 2 && System.currentTimeMillis()-outakeTime>180){
            robot.intake.in();
            intakeMode = IntakeMode.OFF;
            intakeStep = 0;
            outakeTime = 0;
        }

        // Intake On and Off (in)
        if (gamepad1.right_trigger > 0.9 && Button.INTAKE.canPress(timestamp)) {//gamepad1.right_bumper && Button.INTAKE.canPress(timestamp)){
            if (intakeMode == IntakeMode.OFF||intakeMode == IntakeMode.OUT) {
                robot.intake.in();
                intakeMode = IntakeMode.IN;
            } else{
                robot.intake.off();
                intakeMode = IntakeMode.OFF;
            }
        }

        if(gamepad1.left_trigger > 0.9 && Button.INTAKEOUT.canPress(timestamp)){//bumper && Button.INTAKEOUT.canPress(timestamp)){
            if(intakeMode == IntakeMode.OFF|| intakeMode == IntakeMode.IN) {
                robot.intake.out();
                intakeMode = IntakeMode.OUT;
            }
            else {
                robot.intake.off();
                intakeMode = IntakeMode.OFF;
            }
        }

        //H Slides go back
        if(gamepad1.y && Button.TRANSFER.canPress(timestamp)){
            transferNow();
        }

        if(intakeTransfer && cDelay && System.currentTimeMillis()-clawDelay>120){ // Transfer System
            cDelay = false;
            sense = false;
            robot.depoWrist.setIn();
            robot.clawBigTilt.setTransfer();
            robot.clawSmallTilt.setTransfer();
            transferStep = 0;
            transferStep++;
            clawDelay = System.currentTimeMillis();
        }
        if (transferStep ==1 & System.currentTimeMillis()-clawDelay>80){
            robot.intakeTilt.setTransfer();
            transferStep++;
            clawDelay = System.currentTimeMillis();
        }
        if (transferStep ==2 & System.currentTimeMillis()-clawDelay>90){
            robot.claw.setClose();
            clawOpen = false;
            transferStep = 0;
            clawDelay = 0;
        }

        if(!intakeTransfer && intTiltDelay && System.currentTimeMillis()- intakeTiltDelay>390){ //delay in submersible
            robot.intakeTilt.setOut();
            intTiltDelay = false;
        }

        if (gamepad2.a && Button.CLAW.canPress(timestamp)) { // claw
            if(!clawOpen) {
                robot.claw.setOpen();
                clawOpen = true;
            }
            else if(clawOpen){
                robot.claw.setClose();
                clawOpen = false;
            }
        }

        // Bucket(High & Low) sequence
        if (slideHeight == SlideHeight.HIGH1 && System.currentTimeMillis()-depoDelay>290 &dDelay || slideHeight == SlideHeight.LOWER && System.currentTimeMillis()-depoDelay>290 &dDelay) { // Depo to Bucket
            vslideOut = true;
            robot.depoWrist.setOut();
            robot.clawSmallTilt.setOut();
            robot.depoHslide.setInit();
            score = ScoreType.BUCKET;
            bucketSeq = true;
            depoDelay = System.currentTimeMillis();
            dDelay = false;
        }
        if (bucketSeq && slideHeight == SlideHeight.HIGH1 && System.currentTimeMillis()-depoDelay>130 || bucketSeq && slideHeight == SlideHeight.LOWER && System.currentTimeMillis()-depoDelay>100){
            bucketSeq = false;
            depoDelay = 0;
            robot.clawBigTilt.setBucket();
            robot.intakeTilt.setTransfer();
        }

        if (gamepad2.dpad_up && Button.BTN_LEVEL2.canPress(timestamp)){ // Lower Bucket
            robot.claw.setClose();
            clawOpen = false;

            slideHeight = SlideHeight.LOWER;

            robot.vSlides.moveEncoderTo(robot.vSlides.lower, 1f);
            vslideOut = true;
            dDelay = true;
            depoDelay = System.currentTimeMillis();
        }

        if(gamepad2.dpad_down && Button.SLIDE_RESET.canPress(timestamp)) { // Slide reset
            clawOpen = true;
            vslideOut = false;
            robot.depoHslide.setInit();
            if(slideHeight == SlideHeight.DOWN || slideHeight == SlideHeight.WALL || slideHeight == SlideHeight.LOWER) {
                vslideGoBottom = true;
                robot.depoWrist.setIn();
                depoDelay = System.currentTimeMillis();
                resetStep++;
            }
            else{
                robot.depoWrist.setIn();
                robot.claw.setOpen();
                robot.clawSmallTilt.setTransfer();
                robot.clawBigTilt.setTransfer();
                robot.intakeTilt.setTransfer();
                slideHeight = SlideHeight.DOWN;
                depoDelay = System.currentTimeMillis();
                dDelay=true;
            }
        }

        if(slideHeight == SlideHeight.DOWN && System.currentTimeMillis()-depoDelay>200 &&dDelay){
            vslideGoBottom = true;
            depoDelay =0;
            dDelay =false;
        }
        if(resetStep==1 && System.currentTimeMillis() - depoDelay > 350){
            robot.clawSmallTilt.setTransfer();
            robot.clawBigTilt.setTransfer();
            robot.claw.setOpen();
            clawOpen = true;
            depoDelay = System.currentTimeMillis();
            resetStep++;
            robot.intakeTilt.setOut();
            intakeTransfer = false;
        }
        if(resetStep==2 && System.currentTimeMillis() - depoDelay > 350){
            robot.intakeTilt.setTransfer();
            slideHeight = SlideHeight.DOWN;
            gamepad2.rumble(500);
            resetStep=0;
            depoDelay = 0;
        }

        if(vslideGoBottom){ //Reset vSlide check
            slideBottom();
            vslideOut = false;
        }

        // Intake Delay
        if(intakeDelay && System.currentTimeMillis()-outDelay>450){
            intakeDelay = false;
            outDelay =0;
            intakeMode = IntakeMode.IN;
            robot.intake.in();
        }

        /// Telems TODO: DO NOT DELETE ANYTHING
        //telemetry.addData("intake mode", intakeMode);
        //telemetry.addData("h limit switch: ",   hlimitswitch.getState());
        //telemetry.addData("h slide motor power", robot.hslides.getPower());
        //telemetry.addData("v limit switch: ",   vlimitswitch.getState());
        //telemetry.addData("vslideRPower:", robot.vSlides.vSlidesL.getPower());
        //telemetry.addData("vslideLPower:", robot.vSlides.vSlidesR.getPower());
        //telemetry.addData("vslideLencoder: ", robot.vSlides.vSlidesL.getCurrentPosition());
        //telemetry.addData("vslideRencoder: ", robot.vSlides.vSlidesR.getCurrentPosition());
        //telemetry.addData("h slide power:", robot.hslides.hslides.getPower());
        //telemetry.addData("hslide pos: ", robot.hslides.hslides.getCurrentPosition());
        //telemetry.addData("driveLF", robot.driveLeftFront.getCurrentPosition());
        //telemetry.addData("driveLB", robot.driveLeftBack.getCurrentPosition());
        //telemetry.addData("driveRF", robot.driveRightFront.getCurrentPosition());
        //telemetry.addData("intake", robot.intake.intake.getCurrentPosition());
        //telemetry.addData("driveRB", robot.driveRightBack.getCurrentPosition());
        //telemetry.addData("hslidePower", robot.hslides.getPower());

        telemetry.addData("sensorF color", robot.sensorF.getColor());

        //telemetry.addData("sensorF h", robot.sensorF.getHSV()[0]);
        //telemetry.addData("sensorF s", robot.sensorF.getHSV()[1]);
        //telemetry.addData("sensorF v", robot.sensorF.getHSV()[2]);
        //telemetry.addData("sensorF wavelength", robot.sensorF.getWavelength());

        telemetry.update();
    }

    public void slideBottom() { //Slide bottom
        if (vlimitswitch.getState() && vslideGoBottom) {
            robot.vSlides.vSlidesL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setPower(-0.6f);
            robot.vSlides.vSlidesL.setPower(-0.6f);
            RobotLog.ii(TAG_SL, "Going down");
        } else if (!vlimitswitch.getState() && vslideGoBottom) {
            robot.vSlides.vSlidesR.setPower(0);
            robot.vSlides.vSlidesL.setPower(0);
            robot.vSlides.vSlidesL.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            robot.vSlides.vSlidesR.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
            vslideGoBottom = false;
            vslideManual = false;
            RobotLog.ii(TAG_SL, "Force stopped");
        }
    }

    public void transferNow(){
        intakeOutDelay = true;
        robot.clawBigTilt.setTransfer();
        robot.intakeTilt.setHigh();
        robot.latch.setInit();
        robot.depoWrist.setIn();
        outakeTime = System.currentTimeMillis();
        robot.intake.in();
        intakeMode = IntakeMode.IN;
        highTransfer = true;
        intakeTransfer = true;
        robot.claw.setOpen();
        clawOpen = true;
        vslideOut = false;
        slideLength = SlideLength.IN;
        intakeTransfer = true;
    }
}
//早上好中国现在我有冰淇淋
