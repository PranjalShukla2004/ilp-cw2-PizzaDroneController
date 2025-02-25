package inf.ed.cw_ilp.utils;

import inf.ed.cw_ilp.model.Regions.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
    // Angle constant for hovering
    public static final Double HOVER_ANGLE = 999.0;

    // Valid angles for drone movement
    public static final List<Double> VALID_ANGLES = new ArrayList<>(Arrays.asList(
            0.0, 22.5, 45.0, 67.5, 90.0, 112.5, 135.0, 157.5,
            180.0, 202.5, 225.0, 247.5, 270.0, 292.5, 315.0, 337.5,360.0
    ));

    public static final Position APPLETON_TOWER = new Position(-3.186874, 55.944494);

    public static final int DELIVERY_COST = 100;

    public static final int MAX_DRONE_MOVES = 2000;
}
