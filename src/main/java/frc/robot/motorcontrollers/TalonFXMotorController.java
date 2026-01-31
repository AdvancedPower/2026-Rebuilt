package frc.robot.motorcontrollers;

import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;

public class TalonFXMotorController implements MotorController {
    private final TalonFX motor;

    public TalonFXMotorController(TalonFX motor) {
        this.motor = motor;
    }

    public TalonFXMotorController(TalonFX motor, InvertedValue inverted) {
        this(motor);
        motor.getConfigurator().apply(
            new TalonFXConfiguration().withMotorOutput(
                new MotorOutputConfigs().withInverted(inverted)
            )
        );
    }

    @Override
    public void set(double speed) {
        motor.set(speed);
    }
}
