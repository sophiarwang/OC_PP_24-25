package overcharged.components;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

public class clawSmallTilt {
    public OcServo clawSmallTilt;
    public VoltageSensor intakeVolt;
    public static final float INIT = 128f;//230f;
    public static final float TRANSFER = 128f;//130f;//188f;//183f;//177f;//171f;//175f;
    public static final float FLAT = 93f; //TODO: This is transfer Sequence
    public static final float SPEC = 83f;//140f;//117f;//158f; //TODO: This is specimen
    public static final float BUCKET = 149f;//175f;//52f;
    public static final float WALL = 133f;//138f;
    public static final float MOVE_TO_WALL = 97f;
    public static final float LEFT = 172f;
    public static final float RIGHT = 128f;

    public clawSmallTilt(HardwareMap hardwareMap) {
        clawSmallTilt = new OcServo(hardwareMap, "clawSmallTilt", TRANSFER);
    }
    public void setPosition(float pos){
        clawSmallTilt.setPosition(pos);
    }

    public void setInit() { clawSmallTilt.setPosition(INIT); }

    public void setTransfer() { clawSmallTilt.setPosition(TRANSFER); }

    public void setFlat() { clawSmallTilt.setPosition(SPEC); }

    public void setOut() { clawSmallTilt.setPosition(BUCKET); }

    public void setTranSeq() { clawSmallTilt.setPosition(FLAT); }

    public void setWall() { clawSmallTilt.setPosition(WALL);}

    public void setLeft() { clawSmallTilt.setPosition(LEFT);}

    public void setRight() { clawSmallTilt.setPosition(RIGHT);}

}
