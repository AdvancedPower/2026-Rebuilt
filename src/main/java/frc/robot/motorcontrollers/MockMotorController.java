package frc.robot.motorcontrollers;

public class MockMotorController implements MotorController {

    private double output;
    private double position;

    @Override
    public void setOutput(double output) {
        this.output = output;
    }

    @Override
    public double getOutput() {
        return output;
    }

    @Override
    public void setPosition(double position) {
        this.position = position;
    }

    @Override
    public double getPosition() {
        return position;
    }

    @Override
    public void stop() {
        output = 0;
    }
}
