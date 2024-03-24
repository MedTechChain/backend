package nl.tudelft.medtechchain.models.queries;

/**
 * An enum class used to represent different types of queries that can be sent to the chain.
 * Currently, only the types "count" and "average" are supported.
 */
public enum QueryType {
    COUNT("count"),
    AVERAGE("average");

    private final String name;

    QueryType(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
