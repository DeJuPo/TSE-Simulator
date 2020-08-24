package main.java.de.bsi.seapi.holdertypes;

/**
 * This class defines the holder class that enables the specification of output
 * parameters of the type Long for SE API functions
 * 
 */
public final class LongHolder {

    /**
     * Encapsulated Long value
     */
    private Long value;

    /**
     * This function returns the encapsulated Long value
     * 
     * @return encapsulated Long value
     */
    public Long getValue() {
        return value;
    }

    /**
     * This function sets a new value for the encapsulated Long value
     * 
     * @param value
     *            new value for the encapsulated Long value
     */
    public void setValue(Long value) {
        this.value = value;
    }
}
