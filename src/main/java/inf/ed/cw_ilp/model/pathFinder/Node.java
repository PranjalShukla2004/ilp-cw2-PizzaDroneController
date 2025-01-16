package inf.ed.cw_ilp.model.pathFinder;

import inf.ed.cw_ilp.model.Regions.Position;

public class Node {
    private final Position position;
    private Node parent;
    private double start_cost; // Cost from start
    private double heuristic_cost; // Heuristic to end
    private double f; // f = g + h

    public Node(Position position, Node parent, double start_cost, double heuristic_cost) {
        this.position = position;
        this.parent = parent;
        this.start_cost = start_cost;
        this.heuristic_cost = heuristic_cost;
        this.f = start_cost + heuristic_cost;
    }

    public Position getPosition() {
        return position;
    }

    public double get_start_cost() {
        return start_cost;
    }

    public void set_start_cost(double g) {
        this.start_cost = g;
        this.f = this.start_cost + this.heuristic_cost;
    }

    public double get_heuristic_cost() {
        return heuristic_cost;
    }

    public void set_heuristic_cost(double heuristic_cost) {
        this.heuristic_cost = heuristic_cost;
        this.f = this.start_cost + this.heuristic_cost;
    }

    public double getF() {
        return f;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
