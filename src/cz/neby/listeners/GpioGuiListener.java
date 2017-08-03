package cz.neby.listeners;

/**
 * GPIO interface is used for generated action from GPIO
 *
 * @author nebazp
 */
public interface GpioGuiListener {

    /**
     * Method should be calls when GPIO events is generated
     *
     * @param isHigh true if logical true else false
     */
    public void stateChange(boolean isHigh);
}
