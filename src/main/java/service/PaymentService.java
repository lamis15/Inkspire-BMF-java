package service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class PaymentService {
    private static final String STRIPE_SECRET_KEY = "sk_test_e51RJYErEIfxTgrZdz0tqrFK2oLQt3ooEVU1nXMvEQ3rqRH0fyvoQtDueZGEqUbdv3RyzyHZzUDhVBCkdHx25KSIVU00HRBIkGII";

    public String createCheckoutSession(long amountInCents, String currency, String description) throws StripeException {
        Stripe.apiKey = STRIPE_SECRET_KEY;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost/success.html")
                .setCancelUrl("http://localhost/failed.html")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amountInCents)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(description)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return session.getUrl();
    }
    public void openInBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
            } else {
                System.err.println("Cannot open browser on this platform");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
