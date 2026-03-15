package frc.robot.motorcontrollers;

import com.ctre.phoenix6.configs.*;
import com.ctre.phoenix6.controls.Follower;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.MotorAlignmentValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

public class TalonFXMotorController implements MotorController {
    private final TalonFX motor;
    private final MotionMagicVoltage motionMagicRequest;

    public TalonFXMotorController(TalonFX motor) {
        this.motor = motor;
        motionMagicRequest = new MotionMagicVoltage(0);
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

    public TalonFXMotorController withMotionMagic(double velocity, double p) {
        var configurator = motor.getConfigurator();

        var slot0 = new Slot0Configs();
        configurator.refresh(slot0);
        configurator.apply(slot0.withKP(p));

        var motionMagic = new MotionMagicConfigs();
        configurator.refresh(motionMagic);
        configurator.apply(motionMagic
            .withMotionMagicCruiseVelocity(velocity)
            .withMotionMagicAcceleration(velocity * 2)
            .withMotionMagicJerk(velocity * 20));

        var motorOutput = new MotorOutputConfigs();
        configurator.refresh(motorOutput);
        configurator.apply(motorOutput.withNeutralMode(NeutralModeValue.Brake));

        return this;
    }

    public TalonFXMotorController withFollower(TalonFX follower, boolean isAligned) {

        var alignment = isAligned ? MotorAlignmentValue.Aligned : MotorAlignmentValue.Opposed;
        follower.setControl(new Follower(motor.getDeviceID(), alignment));
        return this;
    }

    public TalonFXMotorController withCurrentLimits(double statorCurrentLimit, double supplyCurrentLimit, double supplyCurrentLowerLimit) {
        var currentLimits = new CurrentLimitsConfigs();
        var configurator = motor.getConfigurator();
        configurator.refresh(currentLimits);
        configurator.apply(currentLimits
            .withStatorCurrentLimitEnable(true)
            .withStatorCurrentLimit(statorCurrentLimit)
            .withSupplyCurrentLimitEnable(true)
            .withSupplyCurrentLimit(supplyCurrentLimit)
            .withSupplyCurrentLowerLimit(supplyCurrentLowerLimit));
        return this;
    }

    public TalonFXMotorController withVoltageLimit(double forwardVoltage, double reverseVoltage) {
        var voltage = new VoltageConfigs();
        var configurator = motor.getConfigurator();
        configurator.refresh(voltage);
        configurator.apply(voltage
            .withPeakForwardVoltage(forwardVoltage)
            .withPeakReverseVoltage(reverseVoltage));
        return this;
    }

    @Override
    public void setPosition(double position) {
        motor.setControl(motionMagicRequest.withPosition(position));
    }

    @Override
    public double getPosition() {
        return motor.getPosition().getValueAsDouble();
    }

    @Override
    public void stop() {
        motor.stopMotor();
    }

    private static void setInverted(TalonFX motor, InvertedValue inverted) {
        var motorOutput = new MotorOutputConfigs();
        var configurator = motor.getConfigurator();
        configurator.refresh(motorOutput);
        configurator.apply(motorOutput.withInverted(inverted));
    }
}
