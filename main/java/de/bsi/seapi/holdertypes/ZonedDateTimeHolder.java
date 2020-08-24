package main.java.de.bsi.seapi.holdertypes;

import java.time.ZonedDateTime;

/**
 * This class defines a holder class that enables the specification of output
 * parameters of SE API functions of the type ZonedDateTime
 */

public final class ZonedDateTimeHolder {

    /**
     * the encapsulated value
     */
    private ZonedDateTime value;

   
    /**
     * This function returns the encapsulated ZonedDateTime value
     * 
     * @return encapsulated ZonedDateTime value
     */
    public ZonedDateTime getValue() {
        return value;
    }

    /**
     * This function sets the encapsulated ZonedDateTime value
     * 
     * @param value
     *            new value for the encapsulated ZonedDateTime value
     */
    public void setValue(ZonedDateTime value) {
        this.value = value;
    }
}
