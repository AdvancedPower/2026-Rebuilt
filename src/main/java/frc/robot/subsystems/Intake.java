package frc.robot.subsystems;

import edu.wpi.first.wpilibj2.command.Command;

public class Intake {
    private final MotorSubsystem motor;

    public Intake(MotorSubsystem motor) {
        this.motor = motor;
    }

    public Command intake() {
        return motor.run(() -> 1.0);
    }

    public Command eject() {
        return motor.run(() -> -0.9);
    }
}
