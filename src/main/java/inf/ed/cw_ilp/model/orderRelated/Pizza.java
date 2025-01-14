package inf.ed.cw_ilp.model.orderRelated;

public class Pizza {
    private String name;
    private int priceInPence;

    public Pizza(String name, int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }

    public long getPriceInPence() { return priceInPence; }

    public String getName() { return name; }
}
