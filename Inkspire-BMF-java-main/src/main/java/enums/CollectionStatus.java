package enums;

/**
 * Enum representing the possible status values for a collection.
 */
public enum CollectionStatus {
    /**
     * Collection is in progress - a goal is set and not yet reached.
     */
    IN_PROGRESS("inProgress"),
    
    /**
     * Collection goal has been reached.
     */
    REACHED("reached"),
    
    /**
     * No goal is set for the collection.
     */
    NO_GOAL("noGoal");
    
    private final String value;
    
    CollectionStatus(String value) {
        this.value = value;
    }
    
    /**
     * Get the string value of the status.
     * 
     * @return The string representation of the status
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Convert a string value to the corresponding CollectionStatus enum.
     * 
     * @param value The string value to convert
     * @return The corresponding CollectionStatus, or NO_GOAL if the value doesn't match any status
     */
    public static CollectionStatus fromString(String value) {
        if (value == null) {
            return NO_GOAL;
        }
        
        for (CollectionStatus status : CollectionStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        
        return NO_GOAL;
    }
    
    /**
     * Determine the appropriate status based on the goal and current amount.
     * 
     * @param goal The funding goal amount
     * @param currentAmount The current amount raised
     * @return The appropriate CollectionStatus
     */
    public static CollectionStatus determineStatus(Double goal, Double currentAmount) {
        if (goal == null) {
            return NO_GOAL;
        }
        
        if (currentAmount != null && currentAmount >= goal) {
            return REACHED;
        }
        
        return IN_PROGRESS;
    }
}
