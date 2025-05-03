package overcharged.components;

import static overcharged.config.RobotConstants.TAG_H;
import static overcharged.config.RobotConstants.TAG_SL;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.ArrayList;
import java.util.List;

import overcharged.config.RobotConstants;

public class vSlides {

    public final OcMotorEx vSlidesL;
    public final OcMotorEx vSlidesR;
    public OcSwitch switchSlideDown;
    public final List<OcSwitch> switchs = new ArrayList<>();

    public double start;

    public static int autoLevel = 0;

    public static final int START = 0;
    public static final int PRESET1 = 142;
    public static final int OUT = 1000;
    public static final int wall = 300;
    public static final int mid = 580;//400;
    public static final int lower = 755;
    public static final int high1 = 1495;
    public static final int high2 = 800;
    public static double p = 18;
    public static double i = 0;
    public static double d = 0;
    public static double f = 0;

    public vSlides(HardwareMap hardwareMap) {

        vSlidesR = new OcMotorEx(hardwareMap, "vslidesR", DcMotor.Direction.REVERSE, DcMotor.RunMode.RUN_USING_ENCODER);
        vSlidesL = new OcMotorEx(hardwareMap, "vslidesL", DcMotor.Direction.FORWARD, DcMotor.RunMode.RUN_USING_ENCODER);
        vSlidesR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        vSlidesL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        vSlidesR.setTargetPositionPIDFCoefficients(p, i, d, f);
        vSlidesL.setTargetPositionPIDFCoefficients(p, i, d, f);

        String missing = "";
        int numberMissing = 0;
        OcMotorEx slideF = null;
        OcSwitch vswitch = null;
        try {
            vswitch = new OcSwitch(hardwareMap,"vlimitswitch", true);
            boolean isSwitchNull= switchSlideDown==null ? true : false;
            switchs.add(vswitch);
            RobotLog.ii(TAG_H, "limitSwitch(isNull)? " + isSwitchNull);
        } catch (Exception e) {
            RobotLog.ee(RobotConstants.TAG_R,  "missing: limitSwitch " + e.getMessage());
            missing = missing + ", vlimitswitch";
            numberMissing++;
            boolean isSwitchNull= switchSlideDown==null ? true : false;
            RobotLog.ii(TAG_H, "limitSwitch(catch)? " + isSwitchNull);
        }
        this.switchSlideDown = vswitch;
        start = vSlidesR.getCurrentPosition();
        start = vSlidesL.getCurrentPosition();

        RobotLog.ii(TAG_SL, "Initialized the Slide component numberMissing=" + numberMissing + " missing=" + missing);
    }

    public void outF() {
        vSlidesR.setPower(1);
    }

    public void offF() {
        vSlidesL.setPower(0);
    }

    public void inF() {
        vSlidesR.setPower(-1f);
    }

    public void outB() {
        vSlidesL.setPower(1);
    }

    public void offB() {
        vSlidesR.setPower(0);
    }

    public void inB() {
        vSlidesL.setPower(-1f);
    }



    public boolean slideDown() {
        return switchSlideDown.isTouch() && vSlidesR.getCurrentPosition() <= start;
    }

    public void down(){
        vSlidesL.setPower(-0.7f);
        vSlidesR.setPower(-0.7f);
    }
    public void up(){
        vSlidesL.setPower(0.9f);
        vSlidesR.setPower(0.9f);
    }
    public void setPowerBoth(float power){
        vSlidesL.setPower(power);
        vSlidesR.setPower(power);
    }
    public void moveEncoderTo(int pos, float power) {
        vSlidesR.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        vSlidesL.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        vSlidesR.setTargetPosition(pos);
        vSlidesL.setTargetPosition(pos);
        vSlidesR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        vSlidesL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        vSlidesR.setPower(power);
        vSlidesL.setPower(power);
    }

    public void setPower(float power)
    {
        int cnt = 1;
        //try {
        if (vSlidesR != null) {
            RobotLog.ii(TAG_SL, "Set slide left motor power to " + power);
            vSlidesR.setPower(getPowerR());
            if (power == 0f) {
                vSlidesR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            }
        } else {
            RobotLog.ii(TAG_SL, "Not setting power for motorL");
        }
        cnt = 2;
        if (vSlidesL != null) {
            RobotLog.ii(TAG_SL, "Set slide right motor power to " + power);
            vSlidesL.setPower(getPowerL());
            if (power == 0f) {
                vSlidesL.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            }
        } else {
            RobotLog.ii(TAG_SL, "Not setting power for motorR");
        }
    }

    public void reset(OcMotorEx motor) {
        motor.setPower(0f);
        motor.setPower(0f);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.resetPosition();
    }
    public float getPowerR() {
        return vSlidesR.getPower();
    }

    public float getPowerL() {
        return vSlidesL.getPower();
    }


    public void forceStop() {
        //if (prev_state != State.STOP) {
        RobotLog.ii(TAG_SL, "Force stop the Slide component");
        vSlidesR.setPower(0f);
        vSlidesL.setPower(0f);
    }
}

/*public class vSlides {

    public OcMotorEx vSlidesF;
    public OcMotorEx vSlides;
    public OcSwitch switchSlideDown;
    public final List<OcSwitch> switchs = new ArrayList<>();

    public double start;

    public static int autoLevel = 0;

    public static final int START = 0;
    public static final int PRESET1 = 142;
    public static final int OUT = 1000;
    public static final int wall = 300;
    public static final int mid = 400;
    public static final int high1 = 600;
    public static final int high2 = 800;
    public static double p = 18;
    public static double i = 0;
    public static double d = 0;
    public static double f = 0;

    public vSlides(HardwareMap hardwareMap){
        vSlides = new OcMotorEx(hardwareMap, "vslidesR", DcMotor.Direction.REVERSE, DcMotor.RunMode.RUN_USING_ENCODER);
        vSlides = new OcMotorEx(hardwareMap, "vslidesL", DcMotor.Direction.FORWARD, DcMotor.RunMode.RUN_USING_ENCODER);
        vSlides.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        vSlides.setTargetPositionPIDFCoefficients(p, i, d, f);

        String missing = "";
        int numberMissing = 0;
        OcMotorEx slideL = null;
        OcMotorEx slideR = null;
        OcSwitch vswitch = null;
        try {
            vswitch = new OcSwitch(hardwareMap,"vlimitswitch", true);
            boolean isSwitchNull= switchSlideDown==null ? true : false;
            switchs.add(vswitch);
            RobotLog.ii(TAG_H, "limitSwitch(isNull)? " + isSwitchNull);
        } catch (Exception e) {
            RobotLog.ee(RobotConstants.TAG_R,  "missing: limitSwitch " + e.getMessage());
            missing = missing + ", vlimitswitch";
            numberMissing++;
            boolean isSwitchNull= switchSlideDown==null ? true : false;
            RobotLog.ii(TAG_H, "limitSwitch(catch)? " + isSwitchNull);
        }
        this.switchSlideDown = vswitch;
        start = vSlides.getCurrentPosition();

        RobotLog.ii(TAG_SL, "Initialized the Slide component numberMissing=" + numberMissing + " missing=" + missing);
    }

    public void up(){
        vSlides.setPower(1);
    }

    public void off(){
        vSlides.setPower(0);
    }

    public void down(){
        vSlides.setPower(-1f);
    }

    public void moveEncoderTo(int pos, float power){
        vSlides.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        vSlides.setTargetPosition(pos);
        vSlides.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        vSlides.setPower(1);
    }

    public boolean slideReachedBottom() {
        if (switchSlideDown.isDisabled()) return vSlides.getCurrentPosition() <= start;
        return switchSlideDown.isTouch();
    }
    public void setPower(float power){
        vSlides.setPower(power);}

    public void reset(OcMotorEx motor) {
        motor.setPower(0f);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.resetPosition();
    }

    public void forceStop() {
        //if (prev_state != State.STOP) {
        RobotLog.ii(TAG_SL, "Force stop the Slide component");
        setPower(0f);
    }
}*/

