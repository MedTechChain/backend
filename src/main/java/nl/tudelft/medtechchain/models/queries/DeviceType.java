package nl.tudelft.medtechchain.models.queries;

/**
 * An enum class used to represent different types of devices that can be queried on the chain.
 * Currently, only the types "bedside_monitor", "wearable_device" and "both" are supported.
 */
public enum DeviceType {
    BEDSIDE_MONITOR("bedside_monitor"),
    WEARABLE_DEVICE("wearable_device"),
    BOTH("both");

    private final String name;

    DeviceType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
