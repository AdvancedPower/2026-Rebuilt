package frc.robot.motorcontrollers;

public interface MotorController {
    void setOutput(double output);

    double getOutput();

    void setPosition(double position);

    double getPosition();

    void stop();
}
