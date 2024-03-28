package nl.tudelft.medtechchain.models.queries;

/**
 * An enum class used to represent different types of queries that can be sent to the chain.
 * Currently, only the types "count" and "average" are supported.
 */
public enum QueryType {
    COUNT,
    AVERAGE,
    HISTOGRAM;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
