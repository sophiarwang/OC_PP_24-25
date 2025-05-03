package overcharged.components;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

public class depoWrist {
    public OcServo depoWrist;
    public static final float IN = 162f;//125f;
    public static final float OUT = 85f;

    public depoWrist(HardwareMap hardwareMap) {
        depoWrist = new OcServo(hardwareMap, "depoWrist", IN);
    }
    public void setPosition(float pos){
        depoWrist.setPosition(pos);
    }

    public void setIn() { depoWrist.setPosition(IN); }

    public void setOut() { depoWrist.setPosition(OUT); }
}
