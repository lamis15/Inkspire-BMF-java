package Controllers.Auction.BackOffice;

import entities.Auction;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import service.AuctionService;
import utils.SceneSwitch;

import java.sql.SQLException;
import java.util.List;

public class AfficherAuction {

    @FXML private TableView<Auction> auctionTable;
    @FXML private TableColumn<Auction, Integer> idColumn;
    @FXML private TableColumn<Auction, String> labelColumn;
    @FXML private TableColumn<Auction, Double> startPriceColumn;
    @FXML private TableColumn<Auction, String> startDateColumn;
    @FXML private TableColumn<Auction, String> endDateColumn;
    @FXML private TableColumn<Auction, String> artworkColumn;
    @FXML private TableColumn<Auction, Void> deleteColumn;
    @FXML private TableColumn<Auction, Void> updateColumn;

    @FXML
    private Button bidsBtn;

    private final AuctionService auctionService = new AuctionService();

    @FXML
    public void initialize() {
        configureTable();
        loadAuctions();
    }

    private void configureTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        labelColumn.setCellValueFactory(new PropertyValueFactory<>("label"));
        startPriceColumn.setCellValueFactory(new PropertyValueFactory<>("startPrice"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        artworkColumn.setCellValueFactory(new PropertyValueFactory<>("artworkId"));

        deleteColumn.setCellFactory(param -> new TableCell<>() {
            private final Button deleteBtn = new Button("delete");

            {
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                deleteBtn.setOnAction(event -> {
                    Auction auction = getTableView().getItems().get(getIndex());
                    deleteAuction(auction);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        updateColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateBtn = new Button("update");

            {
                updateBtn.setStyle("-fx-background-color: #476ee2; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand;");
                updateBtn.setOnAction(event -> {

                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(updateBtn);
                }
            }
        });
    }

    private void loadAuctions() {
        auctionTable.getItems().clear();
        try {
            List<Auction> auctions = auctionService.recuperer();
            auctionTable.getItems().addAll(auctions);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void deleteAuction(Auction auction) {
        try {
            auctionService.supprimer(auction.getId());
            auctionTable.getItems().remove(auction);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
