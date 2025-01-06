package inf.ed.cw_ilp.model.pathFinder;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.orderRelated.Pizza;
import java.util.List;

public class nameData {
    public class NamedRegion {
        private String name;
        private List<Position> coordinates;
    }

    public class Restaurant {
        private String name;
        private Position location;
        private List<Pizza> menu;
    }
}
