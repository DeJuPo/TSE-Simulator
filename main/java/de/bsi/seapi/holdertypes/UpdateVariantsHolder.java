package main.java.de.bsi.seapi.holdertypes;

import main.java.de.bsi.seapi.SEAPI.UpdateVariants;

/**
 * This class defines a holder class that enables the specification of output
 * parameters of SE API functions of the type UpdateVariants
 */

public final class UpdateVariantsHolder {

    /**
     * the encapsulated value
     */
    private UpdateVariants value;

    /**
     * This function returns the encapsulated UpdateVariants value
     * 
     * @return encapsulated UpdateVariants value
     */
    public UpdateVariants getValue() {
        return value;
    }

    /**
     * This function sets the encapsulated UpdateVariants value
     * 
     * @param value
     *            new value for the encapsulated UpdateVariants value
     */
    public void setValue(UpdateVariants value) {
        this.value = value;
    }
}
