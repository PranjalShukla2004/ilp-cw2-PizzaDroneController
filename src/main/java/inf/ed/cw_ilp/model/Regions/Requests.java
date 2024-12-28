package inf.ed.cw_ilp.model.Regions;

public record Requests(){
    /**
     * Represents a request for a pair of positions.
     */
    public record LngLatPairRequest(Position position1, Position position2) {}

    /**
     * Represents a request with a position and an angle.
     */
    public record LngLatAngleRequest(Position start, double angle) {}

    /**
     * Represents a request with a position and a region.
     */
    public record LngLatRegionRequest(Position position, Region region) {}
}
