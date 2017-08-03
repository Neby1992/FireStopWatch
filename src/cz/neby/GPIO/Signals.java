package cz.neby.GPIO;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.slf4j.LoggerFactory;
import cz.neby.listeners.GpioGuiListener;

/**
 * Class contains method for register GPIO pins.
 *
 * @author Neby
 */
public class Signals {

    private final GpioController gpio;
    private final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Signals.class.getName());

    /**
     * Constructor inits GPIO instace and aray list
     */
    public Signals() {
        gpio = GpioFactory.getInstance();
    }

    /**
     * Method adds signal to arraylist and init GPIO data listener, all results
     * are stored in database
     *
     * @param pin GPIO port
     * @param target target state listener
     * @return registred instance if PIN.
     */
    public GpioPinDigitalInput registerPin(Pin pin, GpioGuiListener target) {
        LOGGER.debug("Signal on " + pin.getName() + " was registred");
        GpioPinDigitalInput listenPin = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_DOWN);
        listenPin.setShutdownOptions(true);
        listenPin.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                target.stateChange(event.getState().isHigh());
            }
        });
        return listenPin;
    }
}
