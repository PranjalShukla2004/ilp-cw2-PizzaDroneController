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

    public static LngLatAngleRequest combine(double startCoord, double endCoord, double angle) {
        // 1) Wrap start and end in a Position
        Position pos = new Position(startCoord, endCoord);

        // 2) Create the LngLatAngleRequest
        return new LngLatAngleRequest(pos, angle);
    }
}
