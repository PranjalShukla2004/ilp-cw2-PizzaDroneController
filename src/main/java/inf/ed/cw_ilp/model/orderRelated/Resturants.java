package inf.ed.cw_ilp.model.orderRelated;

import inf.ed.cw_ilp.model.Regions.Position;
import java.util.List;

public record Resturants
        (String name,
         Position location,
         List<String> openDays,
         List<Pizza> menu) {
}
