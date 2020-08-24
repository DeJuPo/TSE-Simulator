package main.java.de.bsi.seapi.holdertypes;

/**
 * This class defines a holder class that enables the specification of output
 * parameters of SE API functions of the type Short
 */

public final class ShortHolder {

    /**
     * the encapsulated value
     */
    private Short value;

    /**
     * This function returns the encapsulated Short value
     * 
     * @return the encapsulated Short value
     */
    public Short getValue() {
        return value;
    }

    /**
     * This function sets the encapsulated Short value
     * 
     * @param value
     *            new value for the encapsulated Short value
     */
    public void setValue(Short value) {
        this.value = value;
    }

}
