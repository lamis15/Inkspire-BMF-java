package Controllers.Collections;

import entities.Artwork;
import entities.Collections;
import entities.Donation;
import entities.Session;
import entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import service.ArtworkService;
import service.CollectionsService;
import service.DonationService;
import utils.SceneSwitch;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class CollectionDetails implements javafx.fxml.Initializable {

    @FXML
    private Label titleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label goalLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private ImageView imageView;

    @FXML
    private Button deleteButton;

    @FXML
    private Button modifyButton;

    @FXML
    private Button donateButton;

    @FXML
    private Button backButton;

    @FXML
    private FlowPane artworkContainer;

    @FXML
    private Label ownerNameLabel;

    @FXML
    private Label progressLabel;

    @FXML
    private HBox progressBarContainer;

    @FXML
    private HBox progressBarFill;
    
    @FXML
    private VBox donationsSection;
    
    @FXML
    private VBox donationsContainer;

    private final CollectionsService collectionsService = new CollectionsService();
    private final ArtworkService artworkService = new ArtworkService();
    private final DonationService donationService = new DonationService();

    private Collections collection;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the controller
        System.out.println("Initializing CollectionDetails controller");

        // Configure the FlowPane for proper display of artworks
        artworkContainer.setHgap(15);
        artworkContainer.setVgap(15);
        artworkContainer.setPrefWrapLength(700);
    }

    public void setCollection(Collections collection) {
        System.out.println("Setting collection: " + collection.getId() + " - " + collection.getTitle());
        this.collection = collection;

        // Set collection details
        titleLabel.setText(collection.getTitle());
        descriptionLabel.setText(collection.getDescription());

        if (collection.getGoal() != null) {
            goalLabel.setText("Goal: " + collection.getGoal() + " TND");
        } else {
            goalLabel.setText("No goal set");
        }

        if (collection.getStatus() != null) {
            statusLabel.setText("Status: " + collection.getStatus());
        } else {
            statusLabel.setText("Status: Not active");
        }

        // Load collection image
        if (collection.getImage() != null && !collection.getImage().isEmpty()) {
            try {
                Image image = new Image(collection.getImage());
                imageView.setImage(image);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }

        // Set owner name if available
        if (collection.getUser() != null) {
            String ownerName = collection.getUser().getFirstName() + " " + collection.getUser().getLastName();
            ownerNameLabel.setText("Created by: " + ownerName);
        } else {
            ownerNameLabel.setText("Created by: Unknown");
        }

        // Set up progress bar for current amount vs goal
        if (collection.getGoal() != null && collection.getGoal() > 0) {
            double currentAmount = collection.getCurrentAmount();
            double goalAmount = collection.getGoal();
            double progressPercentage = Math.min((currentAmount / goalAmount) * 100, 100);

            // Update progress label
            progressLabel.setText(String.format("%.2f TND of %.2f TND (%.1f%%)",
                    currentAmount, goalAmount, progressPercentage));

            // Update progress bar width based on percentage
            progressBarFill.prefWidthProperty().bind(
                    progressBarContainer.widthProperty().multiply(progressPercentage / 100));

            // Change color based on progress
            if (progressPercentage < 30) {
                progressBarFill.setStyle("-fx-background-color: #e74c3c; -fx-background-radius: 10 0 0 10;");
            } else if (progressPercentage < 70) {
                progressBarFill.setStyle("-fx-background-color: #f39c12; -fx-background-radius: 10 0 0 10;");
            } else {
                progressBarFill.setStyle("-fx-background-color: #2ecc71; -fx-background-radius: 10 0 0 10;");
            }

            // Show the progress bar components
            progressBarContainer.setVisible(true);
            progressBarFill.setVisible(true);
            progressLabel.setVisible(true);
        } else {
            // Hide progress bar if no goal is set
            progressBarContainer.setVisible(false);
            progressBarFill.setVisible(false);
            progressLabel.setVisible(false);
        }

        // Load artworks that belong to this collection
        loadCollectionArtworks();

        // Set up delete button action
        deleteButton.setOnAction(e -> {
            try {
                collectionsService.supprimer(collection.getId());
                // Navigate back to collections list using mainRouter
                Node mainRouter = deleteButton.getScene().getRoot().lookup("#mainRouter");
                if (mainRouter instanceof Pane) {
                    SceneSwitch.switchScene((Pane) mainRouter, "/AfficherCollections.fxml");
                    System.out.println("Successfully navigated back to collections view after delete");
                } else {
                    System.out.println("Could not find mainRouter for navigation after delete");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Set up modify button action
        modifyButton.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ModifierCollections.fxml"));
                Parent root = loader.load();

                // Pass the collection to the controller
                ModifierCollections controller = loader.getController();
                controller.setCollection(collection);

                // Find the mainRouter and load the modify view
                Node mainRouter = modifyButton.getScene().getRoot().lookup("#mainRouter");
                if (mainRouter instanceof Pane) {
                    ((Pane) mainRouter).getChildren().clear();
                    ((Pane) mainRouter).getChildren().add(root);
                    System.out.println("Successfully loaded modify view");
                } else {
                    System.out.println("Could not find mainRouter for navigation to modify view");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Set up donate button action
        donateButton.setOnAction(e -> onDonateClick());

        // Show/hide buttons based on ownership and user role
        User currentUser = Session.getCurrentUser();
        int currentUserId = (currentUser != null) ? currentUser.getId() : -1;
        boolean isAdmin = (currentUser != null && currentUser.getRole() != null && currentUser.getRole() == 1);
        boolean isOwner = (collection.getUser() != null && collection.getUser().getId() == currentUserId);
        
        if (isOwner && !isAdmin) {
            // Show modify and delete buttons for owner
            modifyButton.setVisible(true);
            deleteButton.setVisible(true);
            donateButton.setVisible(false);
            
            // Show donations section for the owner and load donations
            donationsSection.setVisible(true);
            loadDonationsForCollection();
        } else {
            // Hide modify and delete buttons for non-owners
            modifyButton.setVisible(false);
            deleteButton.setVisible(false);
            
            // Show donate button only for non-admin users
            donateButton.setVisible(!isAdmin);
            
            // Hide donations section for non-owners
            donationsSection.setVisible(false);
        }
    }

    /**
     * Load artworks that belong to this collection
     */
    private void loadCollectionArtworks() {
        try {
            // Clear any existing content
            artworkContainer.getChildren().clear();

            // Get artworks in this collection
            List<Artwork> collectionArtworks = artworkService.getArtworksByCollectionId(collection.getId());

            // Debug log
            System.out.println("Loading artworks for collection ID: " + collection.getId());
            System.out.println("Found " + collectionArtworks.size() + " artworks in this collection");

            // Create a card for each artwork
            for (Artwork artwork : collectionArtworks) {
                try {
                    System.out.println("Processing artwork: " + artwork.getId() + " - " + artwork.getName());

                    // Load the artwork card template
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/ArtworkCard.fxml"));
                    Node artworkCard = loader.load();

                    // Add style class to the card
                    artworkCard.getStyleClass().add("artwork-card");

                    // Find components in the card
                    ImageView artworkImage = (ImageView) ((VBox) artworkCard).lookup("#artworkImage");
                    Label artworkTitle = (Label) ((VBox) artworkCard).lookup("#artworkTitle");
                    Label artworkTheme = (Label) ((VBox) artworkCard).lookup("#artworkTheme");
                    Label artworkDescription = (Label) ((VBox) artworkCard).lookup("#artworkDescription");

                    // Hide the checkbox since we're just displaying, not selecting
                    Node checkbox = ((VBox) artworkCard).lookup("#artworkSelect");
                    if (checkbox != null) {
                        checkbox.setVisible(false);
                        checkbox.setManaged(false);
                    }

                    // Set artwork data
                    if (artwork.getPicture() != null && !artwork.getPicture().isEmpty()) {
                        try {
                            Image image = new Image(artwork.getPicture());
                            artworkImage.setImage(image);
                        } catch (Exception e) {
                            System.out.println("Error loading artwork image: " + e.getMessage());
                            // Use placeholder image if artwork image can't be loaded
                            Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                            artworkImage.setImage(placeholder);
                        }
                    } else {
                        // Use placeholder image if no artwork image
                        Image placeholder = new Image(getClass().getResourceAsStream("/assets/images/placeholder.png"));
                        artworkImage.setImage(placeholder);
                    }

                    // Set text content
                    artworkTitle.setText(artwork.getName());
                    artworkTheme.setText(artwork.getTheme());
                    artworkDescription.setText(artwork.getDescription());

                    // Add the card to the container
                    artworkContainer.getChildren().add(artworkCard);
                    System.out.println("Added artwork card to container");

                } catch (IOException e) {
                    System.out.println("Error creating artwork card: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Show a message if no artworks are in this collection
            if (collectionArtworks.isEmpty()) {
                Label noArtworksLabel = new Label("No artworks in this collection");
                noArtworksLabel.getStyleClass().add("no-artworks-label");
                artworkContainer.getChildren().add(noArtworksLabel);
                System.out.println("No artworks found, added empty message");
            }

        } catch (SQLException e) {
            System.out.println("SQL error loading artworks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClick() {
        // Find the mainRouter in the scene graph
        Node node = backButton.getScene().getRoot().lookup("#mainRouter");

        if (node instanceof Pane) {
            SceneSwitch.switchScene((Pane) node, "/AfficherCollections.fxml");
            System.out.println("Successfully navigated back to collections view");
        } else {
            System.out.println("Could not find mainRouter for navigation");
        }
    }

    /**
     * Handle donate button click to navigate to the donation form
     */
    /**
     * Load donations for the current collection and display them
     */
    private void loadDonationsForCollection() {
        try {
            // Clear existing donations
            donationsContainer.getChildren().clear();
            
            // Get donations for this collection
            List<Donation> donations = donationService.getDonationsByCollectionId(collection.getId());
            
            if (donations.isEmpty()) {
                Label noDonationsLabel = new Label("No donations have been made to this collection yet");
                noDonationsLabel.getStyleClass().add("no-donations-label");
                noDonationsLabel.setStyle("-fx-text-fill: #020741;");
                donationsContainer.getChildren().add(noDonationsLabel);
                return;
            }
            
            // Create a header row
            HBox headerRow = new HBox(10);
            headerRow.getStyleClass().add("donation-header-row");
            headerRow.setPrefWidth(donationsContainer.getPrefWidth());
            
            Label dateHeader = new Label("Date");
            dateHeader.setPrefWidth(200);
            dateHeader.getStyleClass().add("donation-header");
            dateHeader.setStyle("-fx-text-fill: #020741;");
            
            Label donorHeader = new Label("Donor");
            donorHeader.setPrefWidth(200);
            donorHeader.getStyleClass().add("donation-header");
            donorHeader.setStyle("-fx-text-fill: #020741;");
            
            Label amountHeader = new Label("Amount");
            amountHeader.setPrefWidth(100);
            amountHeader.getStyleClass().add("donation-header");
            amountHeader.setStyle("-fx-text-fill: #020741;");
            
            headerRow.getChildren().addAll(dateHeader, donorHeader, amountHeader);
            donationsContainer.getChildren().add(headerRow);
            
            // Add each donation as a row
            for (Donation donation : donations) {
                HBox donationRow = new HBox(10);
                donationRow.getStyleClass().add("donation-row");
                donationRow.setPrefWidth(donationsContainer.getPrefWidth());
                
                // Format date
                String formattedDate = donation.getDate().toLocalDate().toString() + " " + 
                                      donation.getDate().toLocalTime().toString().substring(0, 5);
                Label dateLabel = new Label(formattedDate);
                dateLabel.setPrefWidth(200);
                dateLabel.setStyle("-fx-text-fill: #020741;"); // Explicitly set text color to dark
                
                // Donor name
                String donorName = donation.getUser().getFirstName() + " " + donation.getUser().getLastName();
                Label donorLabel = new Label(donorName);
                donorLabel.setPrefWidth(200);
                donorLabel.setStyle("-fx-text-fill: #020741;"); // Explicitly set text color to dark
                
                // Amount
                Label amountLabel = new Label(String.format("%.2f TND", donation.getAmount()));
                amountLabel.setPrefWidth(100);
                amountLabel.setStyle("-fx-text-fill: #020741;"); // Explicitly set text color to dark
                
                donationRow.getChildren().addAll(dateLabel, donorLabel, amountLabel);
                donationsContainer.getChildren().add(donationRow);
            }
            
            // Add a total row
            HBox totalRow = new HBox(10);
            totalRow.getStyleClass().add("donation-total-row");
            totalRow.setPrefWidth(donationsContainer.getPrefWidth());
            
            Label totalLabel = new Label("Total Donations");
            totalLabel.setPrefWidth(400);
            totalLabel.getStyleClass().add("donation-total-label");
            totalLabel.setStyle("-fx-text-fill: #020741;");
            
            // Calculate total
            double total = donations.stream().mapToDouble(Donation::getAmount).sum();
            Label totalAmountLabel = new Label(String.format("%.2f TND", total));
            totalAmountLabel.setPrefWidth(100);
            totalAmountLabel.getStyleClass().add("donation-total-amount");
            totalAmountLabel.setStyle("-fx-text-fill: #4D81F7;");
            
            totalRow.getChildren().addAll(totalLabel, totalAmountLabel);
            donationsContainer.getChildren().add(totalRow);
            
        } catch (SQLException e) {
            System.out.println("Error loading donations: " + e.getMessage());
            e.printStackTrace();
            
            // Show error message
            Label errorLabel = new Label("Could not load donations. Please try again later.");
            errorLabel.getStyleClass().add("error-label");
            errorLabel.setStyle("-fx-text-fill: #D32F2F;");
            donationsContainer.getChildren().add(errorLabel);
        }
    }
    
    @FXML
    private void onDonateClick() {
        try {
            // Load the donation form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AjouterDonation.fxml"));
            Parent root = loader.load();

            // Find the mainRouter and load the donation view
            Node mainRouter = donateButton.getScene().getRoot().lookup("#mainRouter");
            if (mainRouter instanceof Pane) {
                ((Pane) mainRouter).getChildren().clear();
                ((Pane) mainRouter).getChildren().add(root);

                // Get the controller and pre-select the current collection
                try {
                    Controllers.Donations.AjouterDonation controller = loader.getController();
                    if (controller != null && collection != null) {
                        // Pass the collection to the donation controller
                        controller.preSelectCollection(collection);
                        System.out.println("Pre-selected collection: " + collection.getTitle());
                    }
                } catch (Exception ex) {
                    // Just log the error but continue with navigation
                    System.out.println("Could not pre-select collection: " + ex.getMessage());
                }

                System.out.println("Successfully loaded donation view");
            } else {
                System.out.println("Could not find mainRouter for navigation to donation view");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
