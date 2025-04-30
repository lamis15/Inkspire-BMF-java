package Controllers.user;

import entities.Session;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import service.UserService;
import utils.SceneSwitch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class AfficherUsers {

    @FXML
    private FlowPane cardsContainer;

    @FXML
    private VBox rootVBox;

    private final UserService service = new UserService();

    @FXML
    public void initialize() {
        try {
            List<User> list = service.recuperer();

            for (User user : list) {
                VBox card = createUserCard(user);
                cardsContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createUserCard(User user) {
        VBox card = new VBox();
        card.setSpacing(10);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");

        Label nameLabel = new Label(user.getFirstName());
        nameLabel.setFont(Font.font(16));
        nameLabel.setStyle("-fx-font-weight: bold;");

        Label emailLabel = new Label(user.getEmail());
        emailLabel.setTextFill(Color.DARKGRAY);

        Label roleLabel = new Label("Role: " + getRoleString(user.getRole()));
        roleLabel.setTextFill(Color.GRAY);

        card.getChildren().addAll(nameLabel, emailLabel, roleLabel);

        return card;
    }

    private String getRoleString(int role) {
        return switch (role) {
            case 0 -> "Admin";
            case 1 -> "User";
            default -> "Unknown";
        };
    }


    public void onModifyClick(ActionEvent actionEvent) {
        SceneSwitch.switchScene(rootVBox, "/UserUtils/ModifierUser.fxml" );
    }

    public void onDeleteClick(ActionEvent actionEvent) {

    }
}
