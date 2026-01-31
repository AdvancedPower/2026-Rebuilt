package frc.robot.motorcontrollers;

public class MotorControllerGroup implements MotorController {
    private final MotorController[] motors;

    public MotorControllerGroup(MotorController... motors) {
        this.motors = motors;
    }

    @Override
    public void set(double speed) {
        for (var motor : motors) {
            motor.set(speed);
        }
    }
}
