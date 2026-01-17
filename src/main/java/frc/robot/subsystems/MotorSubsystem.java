package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.MotorController;

public class MotorSubsystem extends SubsystemBase {
    private final MotorController motor;

    public MotorSubsystem(MotorController motor) {
        this.motor = motor;
    }

    public Command run(double speed) {
        return this.startEnd(() -> motor.set(speed), () -> motor.set(0));
    }
}
