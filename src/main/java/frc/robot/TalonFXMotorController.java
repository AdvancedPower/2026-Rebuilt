package frc.robot;

import com.ctre.phoenix6.hardware.TalonFX;

public class TalonFXMotorController implements MotorController {
    private final TalonFX motor;

    public TalonFXMotorController(TalonFX motor) {
        this.motor = motor;
    }

    @Override
    public void set(double speed) {
        motor.set(speed);
    }
}
