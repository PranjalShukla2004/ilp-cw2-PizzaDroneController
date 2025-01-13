package inf.ed.cw_ilp.model.pathFinder;

import inf.ed.cw_ilp.model.Regions.Position;

public class Node {
    private Node parent;
    private Position position;
    private double start_cost;
    private double heuristic_cost;
    private double net_cost;
    private double angle;

    /**
     * Constructs a Node object with the given parameters.
     *
     * @param parent The parent node in the path.
     * @param lngLat The geographical coordinates of the node.
     * @param start_cost      The cost from the start node to this node.
     * @param heuristic_cost      The heuristic cost from this node to the goal node.
     * @param angle  The angle associated with the move to this node.
     */
    public Node(Node parent, Position lngLat, double start_cost, double heuristic_cost, double angle) {
        this.parent = parent;
        this.start_cost = start_cost;
        this.heuristic_cost = heuristic_cost;
        this.net_cost = start_cost + heuristic_cost;
        this.angle = angle;
    }

    public Node getParent() {
        return parent;
    }

    public Position getLngLat() {
        return position;
    }

    public double get_start_cost() {
        return start_cost;
    }

    public double get_heuristic_cost() {
        return heuristic_cost;
    }

    public double get_net_cost() {
        return net_cost;
    }

    public double getAngle() {
        return angle;
    }

    public void setParent(Node parent){
        this.parent = parent;
    }

    public void set_Start_cost(double start_cost){
        this.start_cost = start_cost;
    }

    public void setAngle(double angle){
        this.angle = angle;
    }
}
