package main.java.de.bsi.seapi.holdertypes;

import main.java.de.bsi.seapi.SEAPI.AuthenticationResult;

/**
 * This class defines a holder class that enables the specification of output
 * parameters of SE API functions of the type AuthenticationResult
 */

public final class AuthenticationResultHolder {

    /**
     * the encapsulated value
     */
    private AuthenticationResult value;

    /**
     * This function returns the encapsulated AuthenticationResult value
     * 
     * @return encapsulated AuthenticationResult value
     */
    public AuthenticationResult getValue() {
        return value;
    }

    /**
     * This function sets the encapsulated AuthenticationResult value
     * 
     * @param value
     *            new value for the encapsulated AuthenticationResult value
     */
    public void setValue(AuthenticationResult value) {
        this.value = value;
    }

}
