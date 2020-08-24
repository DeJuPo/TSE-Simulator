package main.java.de.bsi.seapi.holdertypes;

import main.java.de.bsi.seapi.SEAPI.SyncVariants;

/**
 * This class defines a holder class that enables the specification of output
 * parameters of SE API functions of the type SyncVariants
 */

public final class SyncVariantsHolder {

    /**
     * the encapsulated value
     */
    private SyncVariants value;

    /**
     * This function returns the encapsulated SyncVariants value
     * 
     * @return encapsulated SyncVariants value
     */
    public SyncVariants getValue() {
        return value;
    }

    /**
     * This function sets the encapsulated SyncVariants value
     * 
     * @param value
     *            new value for the encapsulated SyncVariants value
     */
    public void setValue(SyncVariants value) {
        this.value = value;
    }
}
