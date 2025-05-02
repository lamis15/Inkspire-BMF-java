package Controllers.user;
import entities.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import service.ResetTokenService;
import service.UserService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Properties;
import java.security.SecureRandom;
import java.util.Base64;


public class ResetPassword {

    private final UserService service = new UserService();
    private final ResetTokenService ResetToeknService = new ResetTokenService();
    @FXML private TextField Restmail;
    @FXML private Label emailError  ;
    @FXML private Label sendingStatus  ;

    @FXML
    private void initialize() {
        Restmail.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}")) {
                sendingStatus.setText("Invalid email format * ");
                sendingStatus.setStyle("-fx-text-fill: red;");
            } else {
                sendingStatus.setText("");
            }
        });
    }


    public static void sendEmail(String toEmail, String subject, String body) {
        final String fromEmail = "jasserhav@gmail.com";
        final String password = "xfnplkztxwrhjjxy";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(fromEmail, false));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            msg.setSubject(subject);
            msg.setText(body);
            msg.setSentDate(new java.util.Date());
            Transport.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // token mtaa l reset :
    public static String generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    @FXML
    private void resetpassword(ActionEvent event) throws SQLException {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(15);

        String email = Restmail.getText();
        boolean UseEexists = service.emailExists(email);


        if(!UseEexists){
            sendingStatus.setText("Email not found in our records. * ");
            sendingStatus.setStyle("-fx-text-fill: red;");
            return ;
        }

        String token = generateToken();
        Integer UserId = service.GetUserId(email)  ;
        ResetToeknService.CreateToken(UserId, token ,now ,expiry );

        String subject = "Reset Your Password";
        String body = "Hello,\n\nTo reset your password, use this code or token:\n\n" +
                " http://127.0.0.1:3000/" + token + "\n\nThis code will expire in 15 minutes.";
        service.SaveToken(email,token) ;
        sendEmail(email, subject, body);
        sendingStatus.setText("Sent successfully !");
        sendingStatus.setStyle("-fx-text-fill: green;");
    }

    @FXML
    private void goBack(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SceneManager.switchTo(stage,"/UserUtils/SigninUser.fxml");
    }


}

