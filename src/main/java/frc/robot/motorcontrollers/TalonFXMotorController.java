package frc.robot.motorcontrollers;

import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import edu.wpi.first.units.measure.AngularVelocity;

public class TalonFXMotorController implements MotorController {
    private final TalonFX motor;

    public TalonFXMotorController(TalonFX motor) {
        this.motor = motor;
    }

    public TalonFXMotorController(TalonFX motor, InvertedValue inverted) {
        this(motor);
        setInverted(motor, inverted);
    }

    @Override
    public void setOutput(double output) {
        motor.set(output);
    }

    @Override
    public double getOutput() {
        return motor.getVelocity().getValueAsDouble();
    }

    public TalonFXMotorController withFollower(TalonFX follower, boolean isAligned) {
        var motorInverted = getInverted(motor);
        var followerInverted = isAligned ? motorInverted : invertInvertedValue(motorInverted);
        setInverted(follower, followerInverted);
        follower.setControl(new Follower(motor.getDeviceID(), MotorAlignmentValue.Aligned));
        return this;
    }

    private static InvertedValue getInverted(TalonFX motor) {
        var configuration = new MotorOutputConfigs();
        motor.getConfigurator().refresh(configuration);
        return configuration.Inverted;
    }

    private static void setInverted(TalonFX motor, InvertedValue inverted) {
        motor.getConfigurator().apply(
            new TalonFXConfiguration().withMotorOutput(
                new MotorOutputConfigs().withInverted(inverted)
            )
        );
    }

    private static InvertedValue invertInvertedValue(InvertedValue inverted) {
        return inverted == InvertedValue.CounterClockwise_Positive
            ? InvertedValue.Clockwise_Positive
            : InvertedValue.CounterClockwise_Positive;
    }
}
