package inf.ed.cw_ilp.model.pathFinder;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.orderRelated.Pizza;

import java.util.ArrayList;
import java.util.List;

public class nameData {
    public static class NamedRegion {
        private String name;
        private List<Position> coordinates;
    }

    public static class Restaurant {
        private String name;
        private Position location;
        private List<String> openingDays;
        private List<Pizza> menu;

            public List<Pizza> getMenu() { return menu; }

        public List<String> getOpeningDays() {
            return openingDays;
        }

        public Position getLocation() { return location; }

        public String getName() {return name; }
    }
}
