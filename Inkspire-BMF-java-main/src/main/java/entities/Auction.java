package entities;

public class Auction {
    private int id;
    private String label;
    private String startDate;
    private String endDate;
    private double startPrice;
    private double endPrice;
    private String status;
    private int artworkId;

    public Auction() {

    }
    public Auction(String label, String startDate, String endDate, double startPrice, double endPrice, String status , int artworkId) {
        this.label = label;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startPrice = startPrice;
        this.endPrice = endPrice;
        this.status = status;
        this.artworkId = artworkId;
    }
    public int getId(){
        return this.id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getLabel() {
        return this.label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getStartDate() {
        return this.startDate;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public String getEndDate() {
        return this.endDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public double getStartPrice() {
        return this.startPrice;
    }
    public void setStartPrice(double startPrice) {
        this.startPrice = startPrice;
    }
    public double getEndPrice() {
        return this.endPrice;
    }
    public void setEndPrice(double endPrice) {
        this.endPrice = endPrice;
    }
    public String getStatus() {
        return this.status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public int getArtworkId() {
        return this.artworkId;
    }
    public void setArtworkId(int artworkId) {
        this.artworkId = artworkId;
    }
}
