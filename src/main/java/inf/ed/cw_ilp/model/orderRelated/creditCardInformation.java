package inf.ed.cw_ilp.model.orderRelated;

public class creditCardInformation {
    public String creditCardNumber;
    private String creditCardExpiry;
    public String cvv;

    public creditCardInformation(String creditCardNumber, String creditCardExpiry, String cvv) {
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
    }
    public String getCreditCardExpiry() {
        return this.creditCardExpiry;
    }

}
