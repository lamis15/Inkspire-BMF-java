package entities;

public class Session {
    private static User currentUser;
    private static Auction currentAuction;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentAuction(Auction auction) {currentAuction = auction;}

    public static Auction getCurrentAuction() { return currentAuction; }

    public static void clearSession() {
        currentUser = null;
    }
}
