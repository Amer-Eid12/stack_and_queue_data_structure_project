import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {

    FlightLinkedList flightList = new FlightLinkedList();

    Label titleLabel = new Label("Flight Passengers");
    TableView<Passenger> passengerTable = new TableView<>();
    TableView<Flight> flightTable = new TableView<>();
    TableView<Passenger> regularPassengersTable = new TableView<>();
    TableView<Passenger> vipPassengersTable = new TableView<>();
    Label flightPassengersLabel = new Label("Flight Passengers");
    TableView<LogEntry> logTable = new TableView<>();
    Passenger checkInPassenger = new Passenger();

    TextField canceledVIPField = new TextField();
    TextField canceledRegularField = new TextField();
    TextField VIPQueueField = new TextField();
    TextField RegualrQueueField = new TextField();
    TextField VIpBoardedField = new TextField();
    TextField RegularBoardedField = new TextField();

    private Stack undoStack = new Stack();
    private Stack redoStack = new Stack();

    FlightNode currentFlightNode;

    public static void main(String[] args) throws Exception {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setScene(createFirstScene(primaryStage));
        primaryStage.show();
    }

    public void readPassengersFromFile(String fileName, FlightLinkedList flightList) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    int passengerID = Integer.parseInt(data[0]);
                    String name = data[1];
                    int flightID = Integer.parseInt(data[2]);
                    String status = data[3];

                    try {
                        Flight flight = flightList.getFlightByFlightID(flightID);
                        if (flight != null) {
                            Passenger passenger = new Passenger(passengerID, name, flightID, status);
                            if ("Regular".equalsIgnoreCase(status)) {
                                flight.getRegularQueue().enqueue(passenger);
                            } else if ("VIP".equalsIgnoreCase(status)) {
                                flight.getVIPQueue().enqueue(passenger);
                            }
                        } else {
                            System.out.println("Flight not found for passenger: " + name);
                        }
                    } catch (NullPointerException e) {
                        System.out.println("Error assigning passenger " + name + " to flight: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFlightsFromFile(String fileName, FlightLinkedList flightList) {
        try (Scanner in = new Scanner(new File(fileName))) {
            in.nextLine();
            while (in.hasNextLine()) {
                String line = in.nextLine();
                String[] data = line.split(",");

                if (data.length == 3) {
                    int flightID = Integer.parseInt(data[0]);
                    String destination = data[1];
                    String status = data[2];

                    flightList.addFlight(
                            flightID,
                            destination,
                            status,
                            new Queue(),
                            new Queue(),
                            new Stack(),
                            new Stack(),
                            new PassengersLinkedList(),
                            new PassengersLinkedList());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePassengersToFile(String fileName, FlightLinkedList flightList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("Passenger ID,Passenger Name,Flight ID,Status");
            bw.newLine();
            for (int i = 0; i < flightList.getSize(); i++) {
                Flight flight = flightList.getFlightByIndex(i);
                if (flight != null) {
                    Queue VIPList = flight.getVIPQueue();
                    Queue regularList = flight.getRegularQueue();

                    // Write all VIP passengers
                    if (VIPList != null && !VIPList.isEmpty()) {
                        PassengerNode current = VIPList.getFront();
                        while (current != null) {
                            Passenger passenger = current.getData();
                            bw.write(passenger.getPassengerID() + "," + passenger.getName() + "," +
                                    passenger.getFlightID() + "," + passenger.getStatus());
                            bw.newLine();
                            current = current.next;
                        }
                    }

                    // Write all Regular passengers
                    if (VIPList != null && !regularList.isEmpty()) {
                        PassengerNode current = regularList.getFront();
                        while (current != null) {
                            Passenger passenger = current.getData();
                            bw.write(passenger.getPassengerID() + "," + passenger.getName() + "," +
                                    passenger.getFlightID() + "," + passenger.getStatus());
                            bw.newLine();
                            current = current.next;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFlightsToFile(String fileName, FlightLinkedList flightList) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            bw.write("Flight ID,Destination,Status");
            bw.newLine();
            for (int i = 0; i < flightList.getSize(); i++) {
                Flight flight = flightList.getFlightByIndex(i);
                if (flight != null) {
                    bw.write(flight.getFlightID() + "," +
                            flight.getDestination() + "," +
                            flight.getStatus());
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLogFile(TableView<LogEntry> logTable) {
        logTable.getItems().clear();
        File logFile = new File("log.txt");

        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] logDetails = line.split("\\|");
                    if (logDetails.length == 5) {
                        LogEntry logEntry = new LogEntry(
                                logDetails[0].trim(), // Date
                                logDetails[1].trim(), // Action
                                logDetails[2].trim(), // Name
                                logDetails[3].trim(), // Flight
                                logDetails[4].trim() // Details
                        );
                        logTable.getItems().add(logEntry);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading log file: " + e.getMessage());
            }
        }
    }

    private Scene createFirstScene(Stage primaryStage) {
        Pane firstPane = new Pane();
        firstPane.setPrefSize(333, 453);
        firstPane.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Label titleLabel = new Label("Airport Check-in and Boarding System");
        titleLabel.setFont(new Font("System Bold", 16));
        titleLabel.setLayoutX(13);
        titleLabel.setLayoutY(70);

        Button loadFlightsDataButton = new Button("Load Flights data");
        loadFlightsDataButton.setLayoutX(93);
        loadFlightsDataButton.setLayoutY(195);
        loadFlightsDataButton.setPrefHeight(30);
        loadFlightsDataButton.setPrefWidth(147);
        loadFlightsDataButton.setStyle("-fx-background-radius: 0.5;");
        loadFlightsDataButton.setFont(new Font(15));

        loadFlightsDataButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Data File");

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                readFlightsFromFile(selectedFile.getName(), flightList);
            }
        });

        Button loadPassengersDataButton = new Button("Load Passengers Data");
        loadPassengersDataButton.setLayoutX(93);
        loadPassengersDataButton.setLayoutY(265);
        loadPassengersDataButton.setStyle("-fx-background-radius: 0.5;");
        loadPassengersDataButton.setFont(new Font(15));
        loadPassengersDataButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Data File");

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                readPassengersFromFile(selectedFile.getName(), flightList);
            }
        });

        Button launchSystemButton = new Button("Launch The System");
        launchSystemButton.setLayoutX(93);
        launchSystemButton.setLayoutY(335);
        launchSystemButton.setStyle("-fx-background-radius: 0.5;");
        launchSystemButton.setFont(new Font(15));

        launchSystemButton.setOnAction(e -> primaryStage.setScene(createSecondScene(primaryStage)));

        firstPane.getChildren().addAll(titleLabel, loadFlightsDataButton, loadPassengersDataButton, launchSystemButton);

        return new Scene(firstPane, 333, 453);
    }

    private Scene createSecondScene(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        tabPane.setPrefSize(849, 526);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(createPassengerManagementTab(primaryStage));
        tabPane.getTabs().add(createFlightManagementTab(primaryStage));
        tabPane.getTabs().add(createOperationsTab());
        tabPane.getTabs().add(createLogFileManagementTab());
        tabPane.getTabs().add(createStatisticsTab());

        return new Scene(tabPane, 849, 526);
    }

    private Tab createPassengerManagementTab(Stage primaryStage) {
        Tab tab = new Tab("Passenger Management Tab");
        tab.setClosable(false);

        Pane root = new Pane();
        root.setPrefSize(200.0, 180.0);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        if (flightList.getFirst() == null) {
            titleLabel = new Label("Flight Passengers");
            titleLabel.setFont(new Font("System Bold", 25));
            titleLabel.setLayoutX(334.0);
            titleLabel.setLayoutY(43.0);
        } else {
            titleLabel = new Label(Integer.toString(flightList.getFlightByIndex(0).getFlightID()) + " Passengers");
            titleLabel.setFont(new Font("System Bold", 25));
            titleLabel.setLayoutX(334.0);
            titleLabel.setLayoutY(43.0);
            currentFlightNode = flightList.getFirst();
            fillPassengerTabTableView(flightList);
        }

        titleLabel.setFont(new Font("System Bold", 25));
        titleLabel.setLayoutX(334.0);
        titleLabel.setLayoutY(43.0);

        Button deletePassengerButton = new Button("Delete Passenger");
        deletePassengerButton.setFont(new Font("System Bold", 14));
        deletePassengerButton.setStyle("-fx-background-color: transparent;");
        deletePassengerButton.setLayoutX(217.0);
        deletePassengerButton.setLayoutY(433.0);
        addButtonEffect(deletePassengerButton);
        deletePassengerButton.setOnAction(e -> {
            deletePassenger();
            updateStatistics();
        });

        Button updatePassengerButton = new Button("Update Passenger");
        updatePassengerButton.setFont(new Font("System Bold", 14));
        updatePassengerButton.setStyle("-fx-background-color: transparent;");
        updatePassengerButton.setLayoutX(381.0);
        updatePassengerButton.setLayoutY(433.0);
        addButtonEffect(updatePassengerButton);
        updatePassengerButton.setOnAction(e -> {
            createUpdatePassengerForm();
            updateStatistics();
        });

        Button savePassengersButton = new Button("Save Passengers");
        savePassengersButton.setFont(new Font("System Bold", 14));
        savePassengersButton.setStyle("-fx-background-color: transparent;");
        savePassengersButton.setLayoutX(14.0);
        savePassengersButton.setLayoutY(14.0);
        addButtonEffect(savePassengersButton);
        savePassengersButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Data File");

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                writePassengersToFile(selectedFile.getName(), flightList);
            }
        });

        Button addPassengerButton = new Button("Add Passenger");
        addPassengerButton.setFont(new Font("System Bold", 14));
        addPassengerButton.setStyle("-fx-background-color: transparent;");
        addPassengerButton.setLayoutX(556.0);
        addPassengerButton.setLayoutY(433.0);
        addButtonEffect(addPassengerButton);
        addPassengerButton.setOnAction(e -> {
            createAddPassengerForm();
            updateStatistics();
        });

        Button displayAllPassengersButton = new Button("Display All Passengers");
        displayAllPassengersButton.setFont(new Font("System Bold", 14));
        displayAllPassengersButton.setStyle("-fx-background-color: transparent;");
        displayAllPassengersButton.setLayoutX(367.0);
        displayAllPassengersButton.setLayoutY(397.0);
        addButtonEffect(displayAllPassengersButton);
        displayAllPassengersButton.setOnAction(e -> {
            fillAllPassengerTabTableView(flightList);
            titleLabel.setText("All Passengers");
        });

        passengerTable.setLayoutX(217.0);
        passengerTable.setLayoutY(125.0);
        passengerTable.setPrefSize(437.0, 261.0);

        TableColumn<Passenger, Integer> idColumn = new TableColumn<>("Passenger ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("passengerID"));

        TableColumn<Passenger, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Passenger, Double> flightIDColumn = new TableColumn<>("Flight ID");
        flightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));

        TableColumn<Passenger, Double> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        passengerTable.getColumns().addAll(idColumn, nameColumn, flightIDColumn, statusColumn);

        Button rightButton = new Button();
        rightButton.setStyle("-fx-background-color: transparent;");
        rightButton.setLayoutX(772.0);
        rightButton.setLayoutY(222.0);
        rightButton.setPrefSize(55.0, 55.0);
        ImageView rightImage = new ImageView(new Image("right.png"));
        ImageView greyRightImageView = new ImageView(new Image("rightGrey.png"));
        rightImage.setFitHeight(60.0);
        rightImage.setFitWidth(60.0);
        greyRightImageView.setFitHeight(60);
        greyRightImageView.setFitWidth(60);
        rightButton.setGraphic(rightImage);
        rightButton.setOnMouseEntered(e -> {
            rightButton.setGraphic(greyRightImageView);
            rightButton.setStyle("-fx-background-color: TRANSPARENT;");
        });
        rightButton.setOnMouseExited(e -> {
            rightButton.setGraphic(rightImage);
            rightButton.setStyle("-fx-background-color: TRANSPARENT;");
        });

        Button leftButton = new Button();
        leftButton.setStyle("-fx-background-color: transparent;");
        leftButton.setLayoutY(224.0);
        leftButton.setPrefSize(38.0, 20.0);
        ImageView leftImage = new ImageView(new Image("left.png"));
        ImageView greyLeftImageView = new ImageView(new Image("leftGrey.png"));
        leftImage.setFitHeight(60.0);
        leftImage.setFitWidth(60.0);
        greyLeftImageView.setFitHeight(60);
        greyLeftImageView.setFitWidth(60);
        leftButton.setGraphic(leftImage);
        leftButton.setOnMouseEntered(e -> {
            leftButton.setGraphic(greyLeftImageView);
            leftButton.setStyle("-fx-background-color: TRANSPARENT;");
        });
        leftButton.setOnMouseExited(e -> {
            leftButton.setGraphic(leftImage);
            leftButton.setStyle("-fx-background-color: TRANSPARENT;");
        });

        Button searchButton = new Button();
        searchButton.setStyle("-fx-background-color: transparent;");
        searchButton.setLayoutX(217.0);
        searchButton.setLayoutY(93.0);
        ImageView searchImage = new ImageView(new Image("search.png"));
        ImageView greySearchImageView = new ImageView(new Image("greySearch.png"));
        searchImage.setFitHeight(25.0);
        searchImage.setFitWidth(25.0);
        greySearchImageView.setFitHeight(25);
        greySearchImageView.setFitWidth(25);
        searchButton.setGraphic(searchImage);
        searchButton.setOnMouseEntered(e -> {
            searchButton.setGraphic(greySearchImageView);
            searchButton.setStyle("-fx-background-color: TRANSPARENT;");
        });
        searchButton.setOnMouseExited(e -> {
            searchButton.setGraphic(searchImage);
            searchButton.setStyle("-fx-background-color: TRANSPARENT;");
        });

        TextField searchTextField = new TextField();
        searchTextField.setLayoutX(259.0);
        searchTextField.setLayoutY(97.0);
        searchTextField.setPrefSize(111.0, 25.0);
        searchTextField.setStyle(
                "-fx-border-color: transparent transparent black transparent; -fx-border-width: 0 0 2 0; -fx-background-color: transparent;");
        searchTextField.setPromptText("Search...");

        searchButton.setOnAction(e -> searchPassenger(searchTextField));

        rightButton.setOnAction(e -> {
            if (currentFlightNode != null) {
                currentFlightNode = currentFlightNode.getNext();
                titleLabel.setText(Integer.toString(currentFlightNode.getData().getFlightID()) + " Passengers");
                fillPassengerTabTableView(flightList);
                flightPassengersLabel
                        .setText(Integer.toString(currentFlightNode.getData().getFlightID()) + " Passengers");
                fillOperationsTabTableView(flightList);
            }
        });

        leftButton.setOnAction(e -> {
            if (currentFlightNode != null) {
                currentFlightNode = currentFlightNode.getPrev();
                titleLabel.setText(Integer.toString(currentFlightNode.getData().getFlightID()) + " Passengers");
                fillPassengerTabTableView(flightList);
                flightPassengersLabel
                        .setText(Integer.toString(currentFlightNode.getData().getFlightID()) + " Passengers");
                fillOperationsTabTableView(flightList);
            }
        });

        root.getChildren().addAll(
                titleLabel, deletePassengerButton, updatePassengerButton, savePassengersButton, addPassengerButton,
                passengerTable, rightButton, leftButton, displayAllPassengersButton,
                searchButton, searchTextField);

        tab.setContent(root);
        return tab;
    }

    private Tab createFlightManagementTab(Stage primaryStage) {
        Tab tab = new Tab("Flight Management Tab");
        tab.setClosable(false);

        Pane root = new Pane();
        root.setPrefSize(200.0, 180.0);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Button addFlightButton = new Button("Add Flight");
        addFlightButton.setFont(new Font("System Bold", 14));
        addFlightButton.setStyle("-fx-background-radius: 20; -fx-background-color: transparent;");
        addFlightButton.setLayoutX(197.0);
        addFlightButton.setLayoutY(393.0);
        addFlightButton.setPrefSize(95.0, 40.0);
        addButtonEffect(addFlightButton);
        addFlightButton.setOnAction(e -> createAddFlightForm());

        Button updateFlightButton = new Button("Update Flight");
        updateFlightButton.setFont(new Font("System Bold", 14));
        updateFlightButton.setStyle("-fx-background-radius: 20; -fx-background-color: transparent;");
        updateFlightButton.setLayoutX(365.0);
        updateFlightButton.setLayoutY(393.0);
        updateFlightButton.setPrefSize(112.0, 40.0);
        addButtonEffect(updateFlightButton);
        updateFlightButton.setOnAction(e -> createUpdateFlightForm());

        Button deleteFlightButton = new Button("Delete Flight");
        deleteFlightButton.setFont(new Font("System Bold", 14));
        deleteFlightButton.setStyle("-fx-background-radius: 20; -fx-background-color: transparent;");
        deleteFlightButton.setLayoutX(542.0);
        deleteFlightButton.setLayoutY(393.0);
        deleteFlightButton.setPrefSize(109.0, 40.0);
        addButtonEffect(deleteFlightButton);
        deleteFlightButton.setOnAction(e -> deleteFlight());

        Button saveFlightsButton = new Button("Save Flights");
        saveFlightsButton.setFont(new Font("System Bold", 14));
        saveFlightsButton.setStyle("-fx-background-color: transparent;");
        saveFlightsButton.setLayoutX(13.0);
        saveFlightsButton.setLayoutY(14.0);
        addButtonEffect(saveFlightsButton);
        saveFlightsButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Data File");

            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text Files", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                readFlightsFromFile(selectedFile.getName(), flightList);
            }
        });

        Label flightDetailsLabel = new Label("Flights Details");
        flightDetailsLabel.setFont(new Font("System Bold", 20));
        flightDetailsLabel.setLayoutX(355.0);
        flightDetailsLabel.setLayoutY(83.0);

        flightTable.setLayoutX(245.0);
        flightTable.setLayoutY(164.0);
        flightTable.setPrefSize(352.0, 200.0);

        TableColumn<Flight, String> flightIDColumn = new TableColumn<>("Flight ID");
        flightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));
        flightIDColumn.setPrefWidth(111.5);

        TableColumn<Flight, String> destinationColumn = new TableColumn<>("Destination");
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("Destination"));
        destinationColumn.setPrefWidth(128.0);

        TableColumn<Flight, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("Status"));
        statusColumn.setPrefWidth(112.5);

        flightTable.getColumns().addAll(flightIDColumn, destinationColumn, statusColumn);

        fillFlightTabTableView(flightList);

        TextField searchTextField = new TextField();
        searchTextField.setLayoutX(281.0);
        searchTextField.setLayoutY(135.0);
        searchTextField.setPrefSize(103.0, 25.0);
        searchTextField.setStyle(
                "-fx-border-color: transparent transparent black transparent; -fx-border-width: 0 0 2 0; -fx-background-color: transparent;");
        searchTextField.setPromptText("Search...");

        Button searchButton = new Button();
        searchButton.setStyle("-fx-background-color: transparent;");
        searchButton.setLayoutX(238.0);
        searchButton.setLayoutY(135.0);
        ImageView searchImage = new ImageView(new Image("search.png"));
        ImageView greySearchImageView = new ImageView(new Image("greySearch.png"));
        searchImage.setFitHeight(25.0);
        searchImage.setFitWidth(25.0);
        greySearchImageView.setFitHeight(25);
        greySearchImageView.setFitWidth(25);
        searchButton.setGraphic(searchImage);
        searchButton.setOnMouseEntered(e -> {
            searchButton.setGraphic(greySearchImageView);
            searchButton.setStyle("-fx-background-color: TRANSPARENT;");
        });
        searchButton.setOnMouseExited(e -> {
            searchButton.setGraphic(searchImage);
            searchButton.setStyle("-fx-background-color: TRANSPARENT;");
        });

        searchButton.setOnAction(e -> {
            searchFlight(searchTextField);
        });

        root.getChildren().addAll(
                addFlightButton, updateFlightButton, deleteFlightButton, saveFlightsButton,
                flightDetailsLabel, flightTable, searchTextField, searchButton);

        tab.setContent(root);
        return tab;
    }

    private Tab createOperationsTab() {
        Tab tab = new Tab("Operations Tab");
        tab.setClosable(false);

        Pane root = new Pane();
        root.setPrefSize(200.0, 180.0);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        regularPassengersTable.setLayoutX(147.0);
        regularPassengersTable.setLayoutY(136.0);
        regularPassengersTable.setPrefSize(285.0, 261.0);

        TableColumn<Passenger, String> regPassengerIDColumn = new TableColumn<>("Passenger ID");
        regPassengerIDColumn.setCellValueFactory(new PropertyValueFactory<>("passengerID"));
        regPassengerIDColumn.setPrefWidth(74.97);

        TableColumn<Passenger, String> regNameColumn = new TableColumn<>("Name");
        regNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        regNameColumn.setPrefWidth(64.91);

        TableColumn<Passenger, String> regFlightIDColumn = new TableColumn<>("Flight ID");
        regFlightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));
        regFlightIDColumn.setPrefWidth(64.0);

        TableColumn<Passenger, String> regStatusColumn = new TableColumn<>("Status");
        regStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        regStatusColumn.setPrefWidth(84.11);

        regularPassengersTable.getColumns().addAll(regPassengerIDColumn, regNameColumn, regFlightIDColumn,
                regStatusColumn);

        vipPassengersTable.setLayoutX(436.0);
        vipPassengersTable.setLayoutY(136.0);
        vipPassengersTable.setPrefSize(285.0, 261.0);

        TableColumn<Passenger, String> vipPassengerIDColumn = new TableColumn<>("Passenger ID");
        vipPassengerIDColumn.setCellValueFactory(new PropertyValueFactory<>("passengerID"));
        vipPassengerIDColumn.setPrefWidth(74.97);

        TableColumn<Passenger, String> vipNameColumn = new TableColumn<>("Name");
        vipNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        vipNameColumn.setPrefWidth(65.83);

        TableColumn<Passenger, String> vipFlightIDColumn = new TableColumn<>("Flight ID");
        vipFlightIDColumn.setCellValueFactory(new PropertyValueFactory<>("flightID"));
        vipFlightIDColumn.setPrefWidth(63.08);

        TableColumn<Passenger, String> vipStatusColumn = new TableColumn<>("Status");
        vipStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        vipStatusColumn.setPrefWidth(79.54);

        vipPassengersTable.getColumns().addAll(vipPassengerIDColumn, vipNameColumn, vipFlightIDColumn, vipStatusColumn);

        fillOperationsTabTableView(flightList);

        flightPassengersLabel.setFont(new Font("System Bold", 25));
        flightPassengersLabel.setLayoutX(334.0);
        flightPassengersLabel.setLayoutY(56.0);

        Label regularPassengersLabel = new Label("Regular Passengers");
        regularPassengersLabel.setFont(new Font("System Bold", 12));
        regularPassengersLabel.setLayoutX(235.0);
        regularPassengersLabel.setLayoutY(116.0);

        Label vipPassengersLabel = new Label("VIP Passengers");
        vipPassengersLabel.setFont(new Font("System Bold", 12));
        vipPassengersLabel.setLayoutX(536.0);
        vipPassengersLabel.setLayoutY(116.0);

        Button cancelPassengerButton = new Button("Cancel Passenger");
        cancelPassengerButton.setFont(new Font("System Bold", 14));
        cancelPassengerButton.setStyle("-fx-background-color: transparent;");
        cancelPassengerButton.setLayoutX(522.0);
        cancelPassengerButton.setLayoutY(447.0);
        addButtonEffect(cancelPassengerButton);

        Button checkInPassengerButton = new Button("Check-in Passenger");
        checkInPassengerButton.setFont(new Font("System Bold", 14));
        checkInPassengerButton.setStyle("-fx-background-color: transparent;");
        checkInPassengerButton.setLayoutX(198.0);
        checkInPassengerButton.setLayoutY(447.0);
        addButtonEffect(checkInPassengerButton);

        Button boardPassengerButton = new Button("Board Passenger");
        boardPassengerButton.setFont(new Font("System Bold", 14));
        boardPassengerButton.setStyle("-fx-background-color: transparent;");
        boardPassengerButton.setLayoutX(372.0);
        boardPassengerButton.setLayoutY(447.0);
        addButtonEffect(boardPassengerButton);

        Button undoButton = new Button();
        undoButton.setStyle("-fx-background-color: transparent;");
        undoButton.setLayoutX(372.0);
        undoButton.setLayoutY(397.0);
        ImageView undoImage = new ImageView(new Image("undo-button.png"));
        ImageView greyUndoImageView = new ImageView(new Image("grey-undo-button.png"));
        undoImage.setFitHeight(30.0);
        undoImage.setFitWidth(30.0);
        greyUndoImageView.setFitHeight(30);
        greyUndoImageView.setFitWidth(30);
        undoButton.setGraphic(undoImage);
        undoButton.setOnMouseEntered(e -> {
            undoButton.setGraphic(greyUndoImageView);
            undoButton.setStyle("-fx-background-color: TRANSPARENT;");
        });
        undoButton.setOnMouseExited(e -> {
            undoButton.setGraphic(undoImage);
            undoButton.setStyle("-fx-background-color: TRANSPARENT;");
        });

        Button redoButton = new Button();
        redoButton.setStyle("-fx-background-color: transparent;");
        redoButton.setLayoutX(450.0);
        redoButton.setLayoutY(397.0);
        ImageView redoImage = new ImageView(new Image("redo-button.png"));
        ImageView greyRedoImage = new ImageView(new Image("grey-redo-button.png"));
        redoImage.setFitHeight(30.0);
        redoImage.setFitWidth(30.0);
        greyRedoImage.setFitHeight(30);
        greyRedoImage.setFitWidth(30);
        redoButton.setGraphic(redoImage);
        redoButton.setOnMouseEntered(e -> {
            redoButton.setGraphic(greyRedoImage);
            redoButton.setStyle("-fx-background-color: TRANSPARENT;");
        });
        redoButton.setOnMouseExited(e -> {
            redoButton.setGraphic(redoImage);
            redoButton.setStyle("-fx-background-color: TRANSPARENT;");
        });

        root.getChildren().addAll(
                regularPassengersTable, vipPassengersTable,
                flightPassengersLabel, regularPassengersLabel, vipPassengersLabel,
                checkInPassengerButton, boardPassengerButton, cancelPassengerButton,
                undoButton, redoButton);

        checkInPassengerButton.setOnAction(e -> {
            try {
                createCheckInPassengerForm();

                logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "Check-in", checkInPassenger.getName(),
                        Integer.toString(checkInPassenger.getFlightID()),
                        String.format("Checked-in %s for Flight %s", checkInPassenger.getName(),
                                checkInPassenger.getFlightID()));

                loadLogFile(logTable);
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
                ex.printStackTrace();
            }
        });

        boardPassengerButton.setOnAction(e -> {
            try {
                String[] labelParts = titleLabel.getText().split(" ");
                int flightID = Integer.parseInt(labelParts[0]);
                Flight flight = flightList.getFlightByFlightID(flightID);

                if (flight == null) {
                    showAlert(Alert.AlertType.WARNING, "Flight Not Found", "No flight found for ID: " + flightID);
                    return;
                }

                Passenger selected = regularPassengersTable.getSelectionModel().getSelectedItem();

                if (selected != null) {
                    if (selected.getStatus().equalsIgnoreCase("vip")) {
                        flight.getVIPQueue().remove(selected);
                    } else {
                        flight.getRegularQueue().remove(selected);
                    }
                } else {
                    if (!flight.getVIPQueue().isEmpty()) {
                        selected = flight.getVIPQueue().dequeue();
                    } else if (!flight.getRegularQueue().isEmpty()) {
                        selected = flight.getRegularQueue().dequeue();
                    }
                }

                if (selected != null) {
                    flight.getBoardedPassengersList().addFirst(selected);
                    undoStack.push(new Flight(flightID, selected, "BOARD"));
                    redoStack = new Stack();

                    logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                            "Boarding", selected.getName(),
                            Integer.toString(selected.getFlightID()),
                            String.format("Boarded %s for Flight %s", selected.getName(), selected.getFlightID()));
                    System.out.println("Passenger boarded: " + selected.getName());

                    loadLogFile(logTable);
                    fillOperationsTabTableView(flightList);
                    updateStatistics();
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "No Passengers", "No passengers available to board.");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
                ex.printStackTrace();
            }
        });

        cancelPassengerButton.setOnAction(e -> {
            try {
                String[] labelParts = titleLabel.getText().split(" ");
                int flightID = Integer.parseInt(labelParts[0]);
                Flight flight = flightList.getFlightByFlightID(flightID);

                Passenger selected = vipPassengersTable.getSelectionModel().getSelectedItem();

                if (selected == null) {
                    selected = regularPassengersTable.getSelectionModel().getSelectedItem();
                }

                if (selected == null) {
                    showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a passenger to cancel.");
                    return;
                }

                if (selected.getStatus().equalsIgnoreCase("vip")) {
                    flight.getVIPQueue().remove(selected);
                } else {
                    flight.getRegularQueue().remove(selected);
                }

                flight.getCanceledPassengersList().addFirst(selected);

                undoStack.push(new Flight(flightID, selected, "CANCEL"));
                redoStack = new Stack();
                System.out.println("Passenger canceled: " + selected.getName());

                logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        "Cancel", selected.getName(),
                        Integer.toString(selected.getFlightID()),
                        String.format("Canceled %s for Flight %s", selected.getName(), selected.getFlightID()));

                loadLogFile(logTable);
                fillOperationsTabTableView(flightList);
                updateStatistics();

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred.");
                ex.printStackTrace();
            }
        });

        undoButton.setOnAction(e -> {
            if (!undoStack.isEmpty()) {
                Flight lastOperation = undoStack.pop();
                int flightID = Integer.parseInt(titleLabel.getText().replaceAll("\\D", ""));
                Flight currentFlight = flightList.getFlightByFlightID(flightID);

                switch (lastOperation.getAction()) {
                    case "CHECK_IN":
                        if (lastOperation.getPassenger().getStatus().equalsIgnoreCase("vip")) {
                            currentFlight.getVIPQueue().remove(lastOperation.getPassenger());
                        } else {
                            currentFlight.getRegularQueue().remove(lastOperation.getPassenger());
                        }
                        logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                "Undo", lastOperation.getPassenger().getName(),
                                Integer.toString(lastOperation.getPassenger().getFlightID()),
                                String.format("Undo Check-in %s for Flight %s",
                                        lastOperation.getPassenger().getName(),
                                        lastOperation.getPassenger().getFlightID()));
                        loadLogFile(logTable);
                        break;

                    case "BOARD":
                        currentFlight.getBoardedPassengersList().remove(lastOperation.getPassenger());
                        if (lastOperation.getPassenger().getStatus().equalsIgnoreCase("vip")) {
                            currentFlight.getVIPQueue().enqueue(lastOperation.getPassenger());
                        } else {
                            currentFlight.getRegularQueue().enqueue(lastOperation.getPassenger());
                        }
                        logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                "Undo", lastOperation.getPassenger().getName(),
                                Integer.toString(lastOperation.getPassenger().getFlightID()),
                                String.format("Undo Boarding %s for Flight %s",
                                        lastOperation.getPassenger().getName(),
                                        lastOperation.getPassenger().getFlightID()));
                        loadLogFile(logTable);
                        break;

                    case "CANCEL":
                        currentFlight.getCanceledPassengersList().remove(lastOperation.getPassenger());
                        if (lastOperation.getPassenger().getStatus().equalsIgnoreCase("vip")) {
                            currentFlight.getVIPQueue().enqueue(lastOperation.getPassenger());
                        } else {
                            currentFlight.getRegularQueue().enqueue(lastOperation.getPassenger());
                        }
                        logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                "Undo", lastOperation.getPassenger().getName(),
                                Integer.toString(lastOperation.getPassenger().getFlightID()),
                                String.format("Undo Cancel %s for Flight %s",
                                        lastOperation.getPassenger().getName(),
                                        lastOperation.getPassenger().getFlightID()));
                        loadLogFile(logTable);
                        break;
                }

                fillOperationsTabTableView(flightList);
                updateStatistics();
                redoStack.push(lastOperation);
                System.out.println("Undo performed for action: " + lastOperation.getAction());
            } else {
                System.out.println("No actions to undo.");
            }
        });

        redoButton.setOnAction(e -> {
            if (!redoStack.isEmpty()) {
                Flight lastUndone = redoStack.pop();
                int flightID = Integer.parseInt(titleLabel.getText().replaceAll("\\D", ""));
                Flight currentFlight = flightList.getFlightByFlightID(flightID);

                switch (lastUndone.getAction()) {
                    case "CHECK_IN":
                        if (lastUndone.getPassenger().getStatus().equalsIgnoreCase("vip")) {
                            currentFlight.getVIPQueue().enqueue(lastUndone.getPassenger());
                        } else {
                            currentFlight.getRegularQueue().enqueue(lastUndone.getPassenger());
                        }
                        logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                "Redo", lastUndone.getPassenger().getName(),
                                Integer.toString(lastUndone.getPassenger().getFlightID()),
                                String.format("Redo Check-in %s for Flight %s",
                                        lastUndone.getPassenger().getName(),
                                        lastUndone.getPassenger().getFlightID()));
                        loadLogFile(logTable);
                        break;

                    case "BOARD":
                        currentFlight.getBoardedPassengersList().addFirst(lastUndone.getPassenger());
                        if (lastUndone.getPassenger().getStatus().equalsIgnoreCase("vip")) {
                            currentFlight.getVIPQueue().dequeue();
                        } else {
                            currentFlight.getRegularQueue().dequeue();
                        }
                        logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                "Redo", lastUndone.getPassenger().getName(),
                                Integer.toString(lastUndone.getPassenger().getFlightID()),
                                String.format("Redo Boarding %s for Flight %s",
                                        lastUndone.getPassenger().getName(),
                                        lastUndone.getPassenger().getFlightID()));
                        loadLogFile(logTable);
                        break;

                    case "CANCEL":
                        if (lastUndone.getPassenger().getStatus().equalsIgnoreCase("vip")) {
                            currentFlight.getVIPQueue().remove(lastUndone.getPassenger());
                        } else {
                            currentFlight.getRegularQueue().remove(lastUndone.getPassenger());
                        }
                        currentFlight.getCanceledPassengersList().addFirst(lastUndone.getPassenger());
                        logAction(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                                "Redo", lastUndone.getPassenger().getName(),
                                Integer.toString(lastUndone.getPassenger().getFlightID()),
                                String.format("Redo Cancel %s for Flight %s",
                                        lastUndone.getPassenger().getName(),
                                        lastUndone.getPassenger().getFlightID()));
                        loadLogFile(logTable);
                        break;
                }

                fillOperationsTabTableView(flightList);
                updateStatistics();
                undoStack.push(lastUndone);
                System.out.println("Redo performed for action: " + lastUndone.getAction());
            } else {
                System.out.println("No actions to redo.");
            }
        });

        tab.setContent(root);
        return tab;
    }

    private Tab createLogFileManagementTab() {
        Tab tab = new Tab("Log File Management Tab");
        tab.setClosable(false);

        Pane root = new Pane();
        root.setPrefSize(200.0, 180.0);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        logTable.setLayoutX(78.0);
        logTable.setLayoutY(160.0);
        logTable.setPrefSize(692.0, 200.0);

        TableColumn<LogEntry, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        dateColumn.setPrefWidth(151.77);

        TableColumn<LogEntry, String> actionColumn = new TableColumn<>("Action");
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        actionColumn.setPrefWidth(119.77);

        TableColumn<LogEntry, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(86.86);

        TableColumn<LogEntry, String> flightColumn = new TableColumn<>("Flight");
        flightColumn.setCellValueFactory(new PropertyValueFactory<>("flight"));
        flightColumn.setPrefWidth(76.80);

        TableColumn<LogEntry, String> detailsColumn = new TableColumn<>("Details");
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));
        detailsColumn.setPrefWidth(258.74);

        logTable.getColumns().addAll(dateColumn, actionColumn, nameColumn, flightColumn, detailsColumn);

        loadLogFile(logTable);

        Label logFileContentLabel = new Label("Log File Content");
        logFileContentLabel.setFont(new Font("System Bold", 20));
        logFileContentLabel.setLayoutX(350.0);
        logFileContentLabel.setLayoutY(90.0);

        Button saveButton = new Button("Save");
        saveButton.setFont(new Font("System Bold", 20));
        saveButton.setStyle("-fx-background-color: transparent;");
        saveButton.setLayoutX(389.0);
        saveButton.setLayoutY(407.0);
        addButtonEffect(saveButton);

        saveButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Log File");
            fileChooser.setInitialFileName("log_export.txt");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                try {
                    Files.copy(Paths.get("log.txt"), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Log file exported successfully to " + selectedFile.getPath());
                } catch (IOException ex) {
                    System.err.println("Error exporting log file: " + ex.getMessage());
                }
            }
        });

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                loadLogFile(logTable);
            }
        });

        root.getChildren().addAll(logTable, logFileContentLabel, saveButton);

        tab.setContent(root);
        return tab;
    }

    private Tab createStatisticsTab() {
        Tab tab = new Tab("Statistics");
        tab.setClosable(false);

        Pane root = new Pane();
        root.setPrefSize(200.0, 180.0);
        root.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Label label1 = new Label("Total Canceled VIP Passengers:");
        label1.setFont(new Font("System Bold", 18));
        label1.setLayoutX(227.0);
        label1.setLayoutY(79.0);

        Label label2 = new Label("Total Canceled Regular Passengers:");
        label2.setFont(new Font("System Bold", 18));
        label2.setLayoutX(189.0);
        label2.setLayoutY(118.0);

        Label label3 = new Label("Total VIP Passengers In The Queue:");
        label3.setFont(new Font("System Bold", 18));
        label3.setLayoutX(189.0);
        label3.setLayoutY(208.0);

        Label label4 = new Label("Total Regular Passengers In The Queue:");
        label4.setFont(new Font("System Bold", 18));
        label4.setLayoutX(151.0);
        label4.setLayoutY(248.0);

        Label label5 = new Label("Total VIP Passengers Who Have Boarded:");
        label5.setFont(new Font("System Bold", 18));
        label5.setLayoutX(136.0);
        label5.setLayoutY(345.0);

        Label label6 = new Label("Total Regular Passengers Who Have Boarded:");
        label6.setFont(new Font("System Bold", 18));
        label6.setLayoutX(101.0);
        label6.setLayoutY(385.0);

        canceledVIPField.setLayoutX(499.0);
        canceledVIPField.setLayoutY(81.0);

        canceledRegularField.setLayoutX(499.0);
        canceledRegularField.setLayoutY(119.0);

        VIPQueueField.setLayoutX(499.0);
        VIPQueueField.setLayoutY(210.0);

        RegualrQueueField.setLayoutX(499.0);
        RegualrQueueField.setLayoutY(250.0);

        VIpBoardedField.setLayoutX(499.0);
        VIpBoardedField.setLayoutY(348.0);

        RegularBoardedField.setLayoutX(499.0);
        RegularBoardedField.setLayoutY(387.0);

        root.getChildren().addAll(
                label1, label2, label3, label4, label5, label6,
                canceledVIPField, canceledRegularField, VIPQueueField, RegualrQueueField, VIpBoardedField,
                RegularBoardedField);

        updateStatistics();
        tab.setContent(root);
        return tab;
    }

    private void addButtonEffect(Button button) {
        button.setOnMouseEntered(e -> button.setStyle("-fx-text-fill: #686D76; -fx-background-color: transparent;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-text-fill: black; -fx-background-color: transparent;"));
    }

    public void fillPassengerTabTableView(FlightLinkedList flightLinkedList) {
        String[] labelParts = titleLabel.getText().split(" ");
        try {
            int flightID = Integer.parseInt(labelParts[0]);
            Flight flight = flightLinkedList.getFlightByFlightID(flightID);

            if (flight == null) {
                System.out.println("Flight not found for FlightID: " + flightID);
                return;
            }

            Queue regularQueue = flight.getRegularQueue();
            Queue vipQueue = flight.getVIPQueue();

            ObservableList<Passenger> passengersList = FXCollections.observableArrayList();
            if (vipQueue != null && !vipQueue.isEmpty()) {
                PassengerNode current = vipQueue.getFront();
                while (current != null) {
                    passengersList.add(current.getData());
                    current = current.next;
                }
            }
            if (regularQueue != null && !regularQueue.isEmpty()) {
                PassengerNode current = regularQueue.getFront();
                while (current != null) {
                    passengersList.add(current.getData());
                    current = current.next;
                }
            }

            passengerTable.setItems(passengersList);

        } catch (NumberFormatException e) {
            System.out.println("Invalid FlightID format in titleLabel: " + titleLabel.getText());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error occurred while populating the Passenger TableView.");
            e.printStackTrace();
        }
    }

    public void fillAllPassengerTabTableView(FlightLinkedList flightLinkedList) {
        try {
            ObservableList<Passenger> passengersList = FXCollections.observableArrayList();

            for (int i = 0; i < flightLinkedList.getSize(); i++) {
                Flight flight = flightLinkedList.getFlightByIndex(i);
                if (flight == null) {
                    continue;
                }

                Queue vipQueue = flight.getVIPQueue();
                if (vipQueue != null && !vipQueue.isEmpty()) {
                    PassengerNode current = vipQueue.getFront();
                    while (current != null) {
                        passengersList.add(current.getData());
                        current = current.next;
                    }
                }

                Queue regularQueue = flight.getRegularQueue();
                if (regularQueue != null && !regularQueue.isEmpty()) {
                    PassengerNode current = regularQueue.getFront();
                    while (current != null) {
                        passengersList.add(current.getData());
                        current = current.next;
                    }
                }
            }

            passengerTable.setItems(passengersList);

        } catch (Exception e) {
            System.out.println("An error occurred while populating the Passenger TableView.");
            e.printStackTrace();
        }
    }

    public void createAddPassengerForm() {
        Stage stage = new Stage();
        Pane pane = new Pane();
        pane.setPrefSize(308.0, 362.0);
        pane.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Label titleLabel = new Label("Add Passenger");
        titleLabel.setLayoutX(82.0);
        titleLabel.setLayoutY(41.0);
        titleLabel.setFont(new Font("System Bold", 22));

        Label passengerIdLabel = new Label("Passenger ID");
        passengerIdLabel.setLayoutX(14.0);
        passengerIdLabel.setLayoutY(102.0);
        passengerIdLabel.setFont(new Font("System Bold", 12));

        Label passengerNameLabel = new Label("Passenger Name");
        passengerNameLabel.setLayoutX(14.0);
        passengerNameLabel.setLayoutY(147.0);
        passengerNameLabel.setFont(new Font("System Bold", 12));

        Label flightLabel = new Label("Flight");
        flightLabel.setLayoutX(17.0);
        flightLabel.setLayoutY(188.0);
        flightLabel.setFont(new Font("System Bold", 12));

        Label statusLabel = new Label("Status");
        statusLabel.setLayoutX(16.0);
        statusLabel.setLayoutY(233.0);
        statusLabel.setFont(new Font("System Bold", 12));

        TextField passengerIdField = new TextField();
        passengerIdField.setLayoutX(160.0);
        passengerIdField.setLayoutY(98.0);
        passengerIdField.setPrefWidth(129.0);
        passengerIdField.setPrefHeight(24.0);

        TextField passengerNameField = new TextField();
        passengerNameField.setLayoutX(160.0);
        passengerNameField.setLayoutY(143.0);
        passengerNameField.setPrefWidth(129.0);
        passengerNameField.setPrefHeight(24.0);

        ComboBox<String> flightComboBox = new ComboBox<>();
        flightComboBox.setLayoutX(160.0);
        flightComboBox.setLayoutY(184.0);
        flightComboBox.setPrefWidth(129.0);
        flightComboBox.setPrefHeight(24.0);
        for (int i = 0; i < flightList.getSize(); i++) {
            flightComboBox.getItems().add(Integer.toString(flightList.getFlightByIndex(i).getFlightID()));
        }

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setLayoutX(160.0);
        statusComboBox.setLayoutY(229.0);
        statusComboBox.setPrefWidth(129.0);
        statusComboBox.setPrefHeight(24.0);
        statusComboBox.getItems().addAll("Regular", "VIP");

        Button addButton = new Button("Add");
        addButton.setLayoutX(128.0);
        addButton.setLayoutY(307.0);
        addButton.setStyle("-fx-background-color: transparent;");
        addButton.setFont(new Font("System Bold", 15));
        addButtonEffect(addButton);
        addButton.setOnAction(e -> {
            try {
                if (statusComboBox.getValue().equalsIgnoreCase("Regular")) {
                    Passenger passenger = new Passenger(Integer.parseInt(passengerIdField.getText()),
                            passengerNameField.getText(), Integer.parseInt(flightComboBox.getValue()),
                            statusComboBox.getValue());
                    flightList.getFlightByFlightID(Integer.parseInt(flightComboBox.getValue())).getRegularQueue()
                            .enqueue(passenger);
                    showAlert(AlertType.INFORMATION, "Passenger added", "Passenger Added Successfully");
                    fillPassengerTabTableView(flightList);
                } else {
                    Passenger passenger = new Passenger(Integer.parseInt(passengerIdField.getText()),
                            passengerNameField.getText(), Integer.parseInt(flightComboBox.getValue()),
                            statusComboBox.getValue());
                    flightList.getFlightByFlightID(Integer.parseInt(flightComboBox.getValue())).getRegularQueue()
                            .enqueue(passenger);
                    showAlert(AlertType.INFORMATION, "Passenger added", "Passenger Added Successfully");
                    fillPassengerTabTableView(flightList);
                }
                stage.close();
                updateStatistics();
            } catch (NumberFormatException E) {
                showAlert(AlertType.ERROR, "Error", "Invalid Input");
            } catch (NullPointerException E) {
                showAlert(AlertType.ERROR, "Error", "Invalid Input");
            }
        });

        pane.getChildren().addAll(
                titleLabel, passengerIdLabel, passengerNameLabel, flightLabel, statusLabel,
                passengerIdField, passengerNameField, flightComboBox, statusComboBox, addButton);

        Scene scene = new Scene(pane);
        stage.setTitle("Add Passenger Form");
        stage.setScene(scene);
        stage.show();
    }

    public void createCheckInPassengerForm() {
        Stage stage = new Stage();
        Pane pane = new Pane();
        pane.setPrefSize(308.0, 362.0);
        pane.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Label titleLabel = new Label("Check-in Passenger");
        titleLabel.setLayoutX(82.0);
        titleLabel.setLayoutY(41.0);
        titleLabel.setFont(new Font("System Bold", 22));

        Label passengerIdLabel = new Label("Passenger ID");
        passengerIdLabel.setLayoutX(14.0);
        passengerIdLabel.setLayoutY(102.0);
        passengerIdLabel.setFont(new Font("System Bold", 12));

        Label passengerNameLabel = new Label("Passenger Name");
        passengerNameLabel.setLayoutX(14.0);
        passengerNameLabel.setLayoutY(147.0);
        passengerNameLabel.setFont(new Font("System Bold", 12));

        Label flightLabel = new Label("Flight");
        flightLabel.setLayoutX(17.0);
        flightLabel.setLayoutY(188.0);
        flightLabel.setFont(new Font("System Bold", 12));

        Label statusLabel = new Label("Status");
        statusLabel.setLayoutX(16.0);
        statusLabel.setLayoutY(233.0);
        statusLabel.setFont(new Font("System Bold", 12));

        TextField passengerIdField = new TextField();
        passengerIdField.setLayoutX(160.0);
        passengerIdField.setLayoutY(98.0);
        passengerIdField.setPrefWidth(129.0);
        passengerIdField.setPrefHeight(24.0);

        TextField passengerNameField = new TextField();
        passengerNameField.setLayoutX(160.0);
        passengerNameField.setLayoutY(143.0);
        passengerNameField.setPrefWidth(129.0);
        passengerNameField.setPrefHeight(24.0);

        ComboBox<String> flightComboBox = new ComboBox<>();
        flightComboBox.setLayoutX(160.0);
        flightComboBox.setLayoutY(184.0);
        flightComboBox.setPrefWidth(129.0);
        flightComboBox.setPrefHeight(24.0);
        for (int i = 0; i < flightList.getSize(); i++) {
            flightComboBox.getItems().add(Integer.toString(flightList.getFlightByIndex(i).getFlightID()));
        }

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setLayoutX(160.0);
        statusComboBox.setLayoutY(229.0);
        statusComboBox.setPrefWidth(129.0);
        statusComboBox.setPrefHeight(24.0);
        statusComboBox.getItems().addAll("Regular", "VIP");

        Button addButton = new Button("Check-in");
        addButton.setLayoutX(128.0);
        addButton.setLayoutY(307.0);
        addButton.setStyle("-fx-background-color: transparent;");
        addButton.setFont(new Font("System Bold", 15));
        addButtonEffect(addButton);
        addButton.setOnAction(e -> {
            try {
                if (statusComboBox.getValue().equalsIgnoreCase("Regular")) {
                    checkInPassenger = new Passenger(Integer.parseInt(passengerIdField.getText()),
                            passengerNameField.getText(), Integer.parseInt(flightComboBox.getValue()),
                            statusComboBox.getValue());
                    flightList.getFlightByFlightID(Integer.parseInt(flightComboBox.getValue())).getRegularQueue()
                            .enqueue(checkInPassenger);
                    undoStack.push(
                            new Flight(Integer.parseInt(flightComboBox.getValue()), checkInPassenger, "CHECK-IN"));
                    redoStack = new Stack();
                    showAlert(AlertType.INFORMATION, "Passenger Checked-in", "Passenger Checked-in Successfully");
                } else {
                    checkInPassenger = new Passenger(Integer.parseInt(passengerIdField.getText()),
                            passengerNameField.getText(), Integer.parseInt(flightComboBox.getValue()),
                            statusComboBox.getValue());
                    flightList.getFlightByFlightID(Integer.parseInt(flightComboBox.getValue())).getRegularQueue()
                            .enqueue(checkInPassenger);
                    undoStack.push(
                            new Flight(Integer.parseInt(flightComboBox.getValue()), checkInPassenger, "CHECK-IN"));
                    redoStack = new Stack();
                    showAlert(AlertType.INFORMATION, "Passenger Checked-in", "Passenger Checked-in Successfully");

                }
                fillPassengerTabTableView(flightList);
                fillOperationsTabTableView(flightList);
                stage.close();
                updateStatistics();
            } catch (NumberFormatException E) {
                showAlert(AlertType.ERROR, "Error", "Invalid Input");
            } catch (NullPointerException E) {
                showAlert(AlertType.ERROR, "Error", "Invalid Input");
            }
        });

        pane.getChildren().addAll(
                titleLabel, passengerIdLabel, passengerNameLabel, flightLabel, statusLabel,
                passengerIdField, passengerNameField, flightComboBox, statusComboBox, addButton);

        Scene scene = new Scene(pane);
        stage.setTitle("Check-in Passenger Form");
        stage.setScene(scene);
        stage.show();
    }

    public void createUpdatePassengerForm() {
        Passenger selectedPassenger = passengerTable.getSelectionModel().getSelectedItem();

        Stage stage = new Stage();
        Pane pane = new Pane();
        pane.setPrefSize(308.0, 362.0);
        pane.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Label titleLabel = new Label("Update Passenger");
        titleLabel.setLayoutX(82.0);
        titleLabel.setLayoutY(41.0);
        titleLabel.setFont(new Font("System Bold", 22));

        Label passengerIdLabel = new Label("Passenger ID");
        passengerIdLabel.setLayoutX(14.0);
        passengerIdLabel.setLayoutY(102.0);
        passengerIdLabel.setFont(new Font("System Bold", 12));

        Label passengerNameLabel = new Label("Passenger Name");
        passengerNameLabel.setLayoutX(14.0);
        passengerNameLabel.setLayoutY(147.0);
        passengerNameLabel.setFont(new Font("System Bold", 12));

        Label flightLabel = new Label("Flight");
        flightLabel.setLayoutX(17.0);
        flightLabel.setLayoutY(188.0);
        flightLabel.setFont(new Font("System Bold", 12));

        Label statusLabel = new Label("Status");
        statusLabel.setLayoutX(16.0);
        statusLabel.setLayoutY(233.0);
        statusLabel.setFont(new Font("System Bold", 12));

        if (selectedPassenger != null) {
            TextField passengerIdField = new TextField(Integer.toString(selectedPassenger.getPassengerID()));
            passengerIdField.setLayoutX(160.0);
            passengerIdField.setLayoutY(98.0);
            passengerIdField.setPrefWidth(129.0);
            passengerIdField.setPrefHeight(24.0);

            TextField passengerNameField = new TextField(selectedPassenger.getName());
            passengerNameField.setLayoutX(160.0);
            passengerNameField.setLayoutY(143.0);
            passengerNameField.setPrefWidth(129.0);
            passengerNameField.setPrefHeight(24.0);

            ComboBox<String> flightComboBox = new ComboBox<>();
            flightComboBox.setLayoutX(160.0);
            flightComboBox.setLayoutY(184.0);
            flightComboBox.setPrefWidth(129.0);
            flightComboBox.setPrefHeight(24.0);
            for (int i = 0; i < flightList.getSize(); i++) {
                flightComboBox.getItems().add(Integer.toString(flightList.getFlightByIndex(i).getFlightID()));
            }
            flightComboBox.setValue(Integer.toString(selectedPassenger.getFlightID()));

            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.setLayoutX(160.0);
            statusComboBox.setLayoutY(229.0);
            statusComboBox.setPrefWidth(129.0);
            statusComboBox.setPrefHeight(24.0);
            statusComboBox.getItems().addAll("Regular", "VIP");
            statusComboBox.setValue(selectedPassenger.getStatus());

            Button addButton = new Button("Update");
            addButton.setLayoutX(128.0);
            addButton.setLayoutY(307.0);
            addButton.setStyle("-fx-background-color: transparent;");
            addButton.setFont(new Font("System Bold", 15));
            addButtonEffect(addButton);
            addButton.setOnAction(e -> {
                try {
                    selectedPassenger.setPassengerID(Integer.parseInt(passengerIdField.getText()));
                    selectedPassenger.setName(passengerNameField.getText());
                    selectedPassenger.setFlightID(Integer.parseInt(flightComboBox.getValue()));
                    selectedPassenger.setStatus(statusComboBox.getValue());
                    fillPassengerTabTableView(flightList);
                    stage.close();
                    updateStatistics();
                } catch (NumberFormatException E) {
                    showAlert(AlertType.ERROR, "Error", "Please enter valid number for passenger ID");
                }
            });

            pane.getChildren().addAll(
                    titleLabel, passengerIdLabel, passengerNameLabel, flightLabel, statusLabel,
                    passengerIdField, passengerNameField, flightComboBox, statusComboBox, addButton);

            Scene scene = new Scene(pane);
            stage.setTitle("Add Passenger Form");
            stage.setScene(scene);
            stage.show();
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a passenger to update.");
        }
    }

    public void searchPassenger(TextField searchField) {
        String[] labelParts = titleLabel.getText().split(" ");
        String search = searchField.getText().toLowerCase();
        ObservableList<Passenger> updatedPassengersList = FXCollections.observableArrayList();

        Flight flight = flightList.getFlightByFlightID(Integer.parseInt(labelParts[0]));
        if (flight == null) {
            System.out.println("Flight not found.");
            return;
        }

        Queue regularPassengers = flight.getRegularQueue();
        Queue VIPPassengers = flight.getVIPQueue();

        if (regularPassengers != null && !regularPassengers.isEmpty()) {
            PassengerNode current = regularPassengers.getFront();
            while (current != null) {
                if (current.getData().getName().toLowerCase().contains(search)) {
                    updatedPassengersList.add(current.getData());
                }
                else if(Integer.toString(current.getData().getPassengerID()).contains(search)){
                    updatedPassengersList.add(current.getData());
                }
                current = current.next;
            }
        }

        if (VIPPassengers != null && !VIPPassengers.isEmpty()) {
            PassengerNode current = VIPPassengers.getFront();
            while (current != null) {
                if (current.getData().getName().toLowerCase().contains(search)) {
                    updatedPassengersList.add(current.getData());
                }
                else if(Integer.toString(current.getData().getPassengerID()).contains(search)){
                    updatedPassengersList.add(current.getData());
                }
                current = current.next;
            }
        }

        passengerTable.setItems(updatedPassengersList);
    }

    private void deletePassenger() {
        String[] labelParts = titleLabel.getText().split(" ");

        Passenger selectedPassenger = passengerTable.getSelectionModel().getSelectedItem();

        boolean removed = false;
        Queue regularQueue = new Queue();
        Queue VIPQueue = new Queue();
        if (selectedPassenger != null) {
            if (selectedPassenger.getStatus().equalsIgnoreCase("Regular")) {
                regularQueue = flightList.getFlightByFlightID(Integer.parseInt(labelParts[0]))
                        .getRegularQueue();
                removed = regularQueue.remove(selectedPassenger);
            } else {
                VIPQueue = flightList.getFlightByFlightID(Integer.parseInt(labelParts[0]))
                        .getVIPQueue();
                removed = VIPQueue.remove(selectedPassenger);
            }

            if (removed) {
                ObservableList<Passenger> updatedPassengerList = FXCollections.observableArrayList();

                if (regularQueue != null && !regularQueue.isEmpty()) {
                    PassengerNode current = regularQueue.getFront();
                    while (current != null) {
                        updatedPassengerList.add(current.getData());
                        current = current.next;
                    }
                }

                if (VIPQueue != null && !VIPQueue.isEmpty()) {
                    PassengerNode current = VIPQueue.getFront();
                    while (current != null) {
                        updatedPassengerList.add(current.getData());
                        current = current.next;
                    }
                }

                passengerTable.setItems(updatedPassengerList);
                showAlert(Alert.AlertType.INFORMATION, "Deletion Successful", "Passenger has been deleted.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Delete Failed", "Failed to delete passenger.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a passenger to delete.");
        }
    }

    public static void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void fillFlightTabTableView(FlightLinkedList flightLinkedList) {
        ObservableList<Flight> updatedFlightList = FXCollections.observableArrayList();
        FlightNode current = flightLinkedList.getFirst();
        if (flightList != null && current != null) {
            do {
                updatedFlightList.add(current.getData());
                current = current.getNext();
            } while (current != flightLinkedList.getFirst());

            flightTable.setItems(updatedFlightList);

        }
    }

    public void searchFlight(TextField seachField) {
        String search = seachField.getText().toLowerCase();
        ObservableList<Flight> updatedFlightList = FXCollections.observableArrayList();
        FlightNode current = flightList.getFirst();
        if (flightList != null && current != null) {
            do {
                if (current.getData().getDestination().toLowerCase().contains(search)) {
                    updatedFlightList.add(current.getData());
                }
                else if((Integer.toString(current.getData().getFlightID()).toLowerCase().contains(search))){
                    updatedFlightList.add(current.getData());
                }
                current = current.getNext();
            } while (current != flightList.getFirst());
            flightTable.setItems(updatedFlightList);
        }
    }

    private void deleteFlight() {
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();

        if (selectedFlight != null) {
            FlightLinkedList flightLinkedList = flightList;

            boolean removed = flightLinkedList.remove(selectedFlight);

            if (removed) {
                ObservableList<Flight> updatedFlightsList = FXCollections.observableArrayList();

                FlightNode current = flightLinkedList.getFirst();
                if (current != null) {
                    do {
                        updatedFlightsList.add(current.getData());
                        current = current.getNext();
                    } while (current != flightLinkedList.getFirst());
                }

                flightTable.setItems(updatedFlightsList);
                showAlert(Alert.AlertType.INFORMATION, "Deletion Successful", "Flight has been deleted.");
            } else {
                showAlert(Alert.AlertType.WARNING, "Delete Failed", "Failed to delete Flight.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a flight to delete.");
        }
    }

    private void createAddFlightForm() {
        Stage stage = new Stage();
        Pane pane = new Pane();
        pane.setPrefSize(308.0, 302.0);
        pane.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Label titleLabel = new Label("Add Flight");
        titleLabel.setLayoutX(98.0);
        titleLabel.setLayoutY(36.0);
        titleLabel.setFont(new Font("System Bold", 22));

        Label flightIdLabel = new Label("Flight ID");
        flightIdLabel.setLayoutX(14.0);
        flightIdLabel.setLayoutY(102.0);
        flightIdLabel.setFont(new Font("System Bold", 12));

        Label destinationLabel = new Label("Destination");
        destinationLabel.setLayoutX(14.0);
        destinationLabel.setLayoutY(147.0);
        destinationLabel.setFont(new Font("System Bold", 12));

        Label statusLabel = new Label("Status");
        statusLabel.setLayoutX(17.0);
        statusLabel.setLayoutY(188.0);
        statusLabel.setFont(new Font("System Bold", 12));

        TextField flightIdField = new TextField();
        flightIdField.setLayoutX(160.0);
        flightIdField.setLayoutY(98.0);
        flightIdField.setPrefHeight(24.0);
        flightIdField.setPrefWidth(129.0);

        TextField destinationField = new TextField();
        destinationField.setLayoutX(160.0);
        destinationField.setLayoutY(143.0);
        destinationField.setPrefHeight(24.0);
        destinationField.setPrefWidth(129.0);

        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.setLayoutX(160.0);
        statusComboBox.setLayoutY(184.0);
        statusComboBox.setPrefHeight(24.0);
        statusComboBox.setPrefWidth(129.0);
        statusComboBox.getItems().addAll("Active", "Inactive");

        Button addButton = new Button("Add");
        addButton.setLayoutX(128.0);
        addButton.setLayoutY(245.0);
        addButton.setStyle("-fx-background-color: transparent;");
        addButton.setFont(new Font("System Bold", 15));
        addButtonEffect(addButton);

        addButton.setOnAction(e -> {
            try {
                Flight flight = new Flight(Integer.parseInt(flightIdField.getText()), destinationField.getText(),
                        statusComboBox.getValue(), null, null, null, null, null, null);
                flightList.addFirst(flight);
                showAlert(AlertType.INFORMATION, "Flight added", "Flight Added Successfully");
                fillFlightTabTableView(flightList);
                stage.close();

            } catch (NumberFormatException E) {
                showAlert(AlertType.ERROR, "Error", "Please enter valid number for Flight ID");
            }

        });

        pane.getChildren().addAll(
                titleLabel, flightIdLabel, destinationLabel, statusLabel,
                flightIdField, destinationField, statusComboBox, addButton);

        Scene scene = new Scene(pane);
        stage.setTitle("Add Flight Form");
        stage.setScene(scene);
        stage.show();
    }

    private void createUpdateFlightForm() {
        Stage stage = new Stage();
        Flight selectedFlight = flightTable.getSelectionModel().getSelectedItem();

        Pane pane = new Pane();
        pane.setPrefSize(308.0, 302.0);
        pane.setStyle("-fx-background-color: radial-gradient(center 50% 50%, radius 70%, B3C8CF, #7FA1C3);");

        Label titleLabel = new Label("Update Flight");
        titleLabel.setLayoutX(82.0);
        titleLabel.setLayoutY(36.0);
        titleLabel.setFont(new Font("System Bold", 22));

        Label flightIdLabel = new Label("Flight ID");
        flightIdLabel.setLayoutX(14.0);
        flightIdLabel.setLayoutY(102.0);
        flightIdLabel.setFont(new Font("System Bold", 12));

        Label destinationLabel = new Label("Destination");
        destinationLabel.setLayoutX(14.0);
        destinationLabel.setLayoutY(147.0);
        destinationLabel.setFont(new Font("System Bold", 12));

        Label statusLabel = new Label("Status");
        statusLabel.setLayoutX(17.0);
        statusLabel.setLayoutY(188.0);
        statusLabel.setFont(new Font("System Bold", 12));

        if (selectedFlight != null) {
            TextField flightIdField = new TextField(Integer.toString(selectedFlight.getFlightID()));
            flightIdField.setLayoutX(160.0);
            flightIdField.setLayoutY(98.0);
            flightIdField.setPrefHeight(24.0);
            flightIdField.setPrefWidth(129.0);

            TextField destinationField = new TextField(selectedFlight.getDestination());
            destinationField.setLayoutX(160.0);
            destinationField.setLayoutY(143.0);
            destinationField.setPrefHeight(24.0);
            destinationField.setPrefWidth(129.0);

            ComboBox<String> statusComboBox = new ComboBox<>();
            statusComboBox.setLayoutX(160.0);
            statusComboBox.setLayoutY(184.0);
            statusComboBox.setPrefHeight(24.0);
            statusComboBox.setPrefWidth(129.0);
            statusComboBox.getItems().addAll("Active", "Inactive");
            statusComboBox.setValue(selectedFlight.getStatus());

            Button updateButton = new Button("Update");
            updateButton.setLayoutX(118.0);
            updateButton.setLayoutY(244.0);
            updateButton.setStyle("-fx-background-color: transparent;");
            updateButton.setFont(new Font("System Bold", 15));
            addButtonEffect(updateButton);

            pane.getChildren().addAll(
                    titleLabel, flightIdLabel, destinationLabel, statusLabel,
                    flightIdField, destinationField, statusComboBox, updateButton);

            updateButton.setOnAction(e -> {
                try {
                    selectedFlight.setFlightID(Integer.parseInt(flightIdField.getText()));
                    selectedFlight.setDestination(destinationField.getText());
                    selectedFlight.setStatus(statusComboBox.getValue());
                    fillFlightTabTableView(flightList);
                    stage.close();

                } catch (NumberFormatException E) {
                    showAlert(AlertType.ERROR, "Error", "Please enter valid number for Flight ID");
                }
            });

            Scene scene = new Scene(pane);
            stage.setTitle("Update Flight Form");
            stage.setScene(scene);
            stage.show();
        } else {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a flight to update.");
        }
    }

    public void fillOperationsTabTableView(FlightLinkedList flightLinkedList) {
        String[] labelParts = titleLabel.getText().split(" ");
        try {
            int flightID = Integer.parseInt(labelParts[0]);
            Flight flight = flightLinkedList.getFlightByFlightID(flightID);

            if (flight == null) {
                System.out.println("Flight not found for FlightID: " + flightID);
                return;
            }

            Queue regularQueue = flight.getRegularQueue();
            Queue vipQueue = flight.getVIPQueue();

            ObservableList<Passenger> regularPassengersList = FXCollections.observableArrayList();
            ObservableList<Passenger> vipPassengersList = FXCollections.observableArrayList();

            if (regularQueue != null && !regularQueue.isEmpty()) {
                PassengerNode current = regularQueue.getFront();
                while (current != null) {
                    regularPassengersList.add(current.getData());
                    current = current.next;
                }
            }

            if (vipQueue != null && !vipQueue.isEmpty()) {
                PassengerNode current = vipQueue.getFront();
                while (current != null) {
                    vipPassengersList.add(current.getData());
                    current = current.next;
                }
            }

            regularPassengersTable.setItems(regularPassengersList);
            vipPassengersTable.setItems(vipPassengersList);

        } catch (NumberFormatException e) {
            System.out.println("Invalid FlightID format in titleLabel: " + titleLabel.getText());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("An error occurred while populating the Passenger TableView.");
            e.printStackTrace();
        }
    }

    private void logAction(String date, String action, String name, String flight, String details) {
        try (FileWriter writer = new FileWriter("log.txt", true)) {
            writer.write(String.format("%s | %s | %s | %s | %s%n", date, action, name, flight, details));
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        int totalVIPQueue = 0;
        int totalRegularQueue = 0;
        int totalBoardedVIP = 0;
        int totalBoardedRegular = 0;
        int totalCanceledVIP = 0;
        int totalCanceledRegular = 0;

        for (int i = 0; i < flightList.getSize(); i++) {
            Flight flight = flightList.getFlightByIndex(i);
            if (flight != null) {
                if (flight.getVIPQueue() != null) {
                    totalVIPQueue += flight.getVIPQueue().getSize();
                }
                if (flight.getRegularQueue() != null) {
                    totalRegularQueue += flight.getRegularQueue().getSize();
                }

                PassengersLinkedList boardedList = flight.getBoardedPassengersList();
                if (boardedList != null) {
                    for (int j = 0; j < boardedList.getSize(); j++) {
                        Passenger passenger = boardedList.getPassengerByIndex(j);
                        if (passenger != null && passenger.getStatus() != null) {
                            if (passenger.getStatus().equalsIgnoreCase("vip")) {
                                totalBoardedVIP++;
                            } else {
                                totalBoardedRegular++;
                            }
                        }
                    }
                }

                PassengersLinkedList canceledList = flight.getCanceledPassengersList();
                if (canceledList != null) {
                    for (int j = 0; j < canceledList.getSize(); j++) {
                        Passenger passenger = canceledList.getPassengerByIndex(j);
                        if (passenger != null && passenger.getStatus() != null) {
                            if (passenger.getStatus().equalsIgnoreCase("vip")) {
                                totalCanceledVIP++;
                            } else {
                                totalCanceledRegular++;
                            }
                        }
                    }
                }
            }
        }

        VIPQueueField.setText(Integer.toString(totalVIPQueue));
        RegualrQueueField.setText(Integer.toString(totalRegularQueue));
        VIpBoardedField.setText(Integer.toString(totalBoardedVIP));
        RegularBoardedField.setText(Integer.toString(totalBoardedRegular));
        canceledVIPField.setText(Integer.toString(totalCanceledVIP));
        canceledRegularField.setText(Integer.toString(totalCanceledRegular));
    }

}
