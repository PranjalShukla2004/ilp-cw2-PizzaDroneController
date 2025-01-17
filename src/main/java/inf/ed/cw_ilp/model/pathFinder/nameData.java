package inf.ed.cw_ilp.model.pathFinder;
import inf.ed.cw_ilp.model.Regions.Position;
import inf.ed.cw_ilp.model.orderRelated.Pizza;

import java.util.List;

public class nameData {

    public static class NamedRegion {

        public String name;
        public Position[] vertices;

        public NamedRegion(String name, Position[] positions) {
            this.name = name;
            this.vertices = positions;
        }

        public Position[] getCoordinates() {
            return vertices;
        }
    }

    public static class Restaurant {

        private String name;
        private Position location;
        private List<String> openingDays;
        private List<Pizza> menu;

        public Restaurant(String name, Position location, List<String> openingDays, List<Pizza> menu) {
            this.name = name;
            this.location = location;
            this.openingDays = openingDays;
            this.menu = menu;
        }

        public List<Pizza> getMenu() { return menu; }

        public List<String> getOpeningDays() {
            return openingDays;
        }

        public Position getLocation() { return location; }

        public String getName() {return name; }
    }
}
