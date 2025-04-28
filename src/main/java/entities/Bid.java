package entities;

public class Bid {
    private int id;
    private double amount;
    private String time;
    private int auctionId;
    private int userId;
    public Bid(){
    }
    public Bid(double amount, String time, int auctionId, int userId){
        this.amount = amount;
        this.time = time;
        this.auctionId = auctionId;
        this.userId = userId;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public int getAuctionId() {
        return auctionId;
    }
    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
