package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AddressableLED;
import edu.wpi.first.wpilibj.AddressableLEDBuffer;
import edu.wpi.first.wpilibj.LEDPattern;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Leds extends SubsystemBase {
    private final AddressableLED m_led;
    private final AddressableLEDBuffer m_buffer;

    public Leds(int port, int length) {
        m_led = new AddressableLED(port);
        m_buffer = new AddressableLEDBuffer(length);
        m_led.setLength(length);
        m_led.start();
        setDefaultCommand(runPattern(solidColor(Color.kBlack)));
    }

    @Override
    public void periodic() {
        m_led.setData(m_buffer);
    }

    public Command runPattern(LEDPattern pattern) {
        return run(() -> pattern.applyTo(m_buffer));
    }

    public static LEDPattern solidColor(Color color) {
        return LEDPattern.solid(color);
    }
}
