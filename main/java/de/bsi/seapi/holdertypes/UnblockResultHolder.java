package main.java.de.bsi.seapi.holdertypes;

import main.java.de.bsi.seapi.SEAPI.UnblockResult;

/**
 * This class defines a holder class that enables the specification of output
 * parameters of SE API functions of the type UnblockResultHolder
 */

public final class UnblockResultHolder {

    /**
     * the encapsulated value
     */
    private UnblockResult value;

   
    /**
     * This function returns the encapsulated UnblockResult value
     * 
     * @return the encapsulated UnblockResult value
     */
    public UnblockResult getValue() {
        return value;
    }

    /**
     * This function sets the encapsulated UnblockResult value
     * 
     * @param value
     *            new value for the encapsulated UnblockResult value
     */
    public void setValue(UnblockResult value) {
        this.value = value;
    }
}
