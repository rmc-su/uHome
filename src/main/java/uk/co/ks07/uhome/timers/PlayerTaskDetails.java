package uk.co.ks07.uhome.timers;

/**
 * Simple class to encapsulate details of a player cooldown task.
 */
class PlayerTaskDetails {

    private final int taskIndex;
    private final long finishTime;

    /**
     * Constructor with data values.
     *
     * @param taskIndex
     *            Index of the task in the scheduler.
     * @param finishTime
     *            Estimated cooldown finish time with similar semantics as
     *            {@link System#currentTimeMillis}.
     */
    PlayerTaskDetails(int taskIndex, long finishTime) {
        this.taskIndex = taskIndex;
        this.finishTime = finishTime;
    }

    /**
     * @return The planned cooldown finish time, in milliseconds.
     */
    long getFinishTime() {
        return finishTime;
    }

    /**
     * @return Index of the task in the scheduler.
     */
    int getTaskIndex() {
        return taskIndex;
    }
}
