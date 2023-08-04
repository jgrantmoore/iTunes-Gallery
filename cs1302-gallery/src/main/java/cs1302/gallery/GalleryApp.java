package cs1302.gallery;

import java.net.http.HttpClient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.text.Text;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.TilePane;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import javafx.geometry.VPos;
import javafx.scene.text.TextAlignment;
import javafx.geometry.Pos;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import cs1302.gallery.ItunesAPI;
import cs1302.gallery.ImageGrid;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * Represents an iTunes Gallery App.
 */
public class GalleryApp extends Application {

    /** HTTP client. */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns a HttpClient object

    /** Google {@code Gson} object for parsing JSON-formatted strings. */
    public static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()                          // enable nice output when printing
        .create();                                    // builds and returns a Gson object

    private Stage stage;
    private Scene scene;
    private HBox root;
    private VBox display;

    private HBox topBar;
    private Button playBtn;
    private Separator verticalRule;
    private Text searchText;
    private TextField searchBar;
    private ComboBox<String> dropDown;
    private Button getImagesBtn;

    private TextFlow topText;

    private ImageGrid imageGrid;

    private HBox bottomBar;
    public static ProgressBar progressBar;
    private Text licenseText;

    private boolean isPlaying = false;

    private String errorUri;

    Runnable pauseRunnable = () -> {
        Platform.runLater(() -> {
            playBtn.setText("Play");
        });
        imageGrid.pause();
    }; //playRunnable


    Runnable getImagesRunnable = () -> {
        pauseRunnable.run();
        ItunesAPI API = new ItunesAPI();
        String termText = URLEncoder.encode(searchBar.getText(), StandardCharsets.UTF_8);
        String mediaText = URLEncoder.encode(dropDown.getValue(), StandardCharsets.UTF_8);
        String query = String.format("?term=%s&limit=200&media=%s", termText, mediaText);
        errorUri = "https://itunes.apple.com/search" + query;
        Platform.runLater(() -> {
            topText.getChildren().clear();
            this.topText.getChildren().add(
                new Text("Getting Images..."));
            getImagesBtn.setDisable(true);
        });
        try {
            imageGrid.update(API.call(
                searchBar.getText(), dropDown.getValue(), imageGrid.getUrl()));
            Platform.runLater(() -> {
                topText.getChildren().clear();
                this.topText.getChildren().add(
                    new Text("https://itunes.apple.com/search" + query));
                getImagesBtn.setDisable(false);
                playBtn.setDisable(false);
            });
        } catch (IllegalArgumentException e) {
            Platform.runLater(() -> {
                topText.getChildren().clear();
                this.topText.getChildren().add(
                    new Text("Last attempt to get images failed..."));
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeight(300);
                alert.setWidth(500);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("URI: " + errorUri + "\n\nException: " + e.toString());
                alert.show();
                getImagesBtn.setDisable(false);
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                topText.getChildren().clear();
                this.topText.getChildren().add(
                    new Text("Last attempt to get images failed..."));
                Alert alert = new Alert(AlertType.ERROR);
                alert.setHeight(300);
                alert.setWidth(500);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("URI: " + errorUri + "\n\nException: " + e.toString());
                alert.show();
                getImagesBtn.setDisable(false);
            });
        }
    };


    /**
     * Constructs a {@code GalleryApp} object.
     */
    public GalleryApp() {
        this.stage = null;
        this.scene = null;
        this.root = new HBox(5);
        this.display = new VBox(3);
        display.setAlignment(Pos.CENTER_LEFT);

        this.topBar = new HBox(5);
        topBar.setAlignment(Pos.CENTER_LEFT);
        this.playBtn = new Button("Play");
        this.verticalRule = new Separator();
        verticalRule.setOrientation(javafx.geometry.Orientation.VERTICAL);
        this.searchText = new Text("Search:");
        this.searchBar = new TextField("Daft Punk");
        this.dropDown = new ComboBox<String>();
        this.getImagesBtn = new Button("Get Images");

        this.topText = new TextFlow();

        this.imageGrid = new ImageGrid();

        this.bottomBar = new HBox(5);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        this.progressBar = new ProgressBar(0F);
        this.licenseText = new Text("Images provided by iTunes Search API.");
        bottomBar.setHgrow(this.progressBar, Priority.ALWAYS);

    } // GalleryApp

    /** {@inheritDoc} */
    @Override
    public void init() {
        System.out.println("init() called");
        this.topBar.getChildren().addAll(
            playBtn, verticalRule, searchText, searchBar, dropDown, getImagesBtn);
        topBar.setHgrow(searchBar, Priority.ALWAYS);
        this.topText.getChildren().add(
            new Text("Type in a term, select a media type, then click the \"Get Images\" button."));
        this.bottomBar.getChildren().addAll(progressBar, licenseText);
        bottomBar.setHgrow(progressBar, Priority.ALWAYS);
        this.display.getChildren().addAll(topBar, topText, imageGrid, bottomBar);
        this.root.getChildren().add(display);
        root.setHgrow(display, Priority.ALWAYS);
        dropDown.getItems().addAll("movie", "podcast", "music", "musicVideo", "audiobook",
            "shortFilm", "tvShow", "software", "ebook", "all");
        dropDown.getSelectionModel().select(2);
        playBtn.setDisable(true);
        progressBar.setPrefWidth(350);
        this.getImagesBtn.setOnAction(event -> {
            Thread thread = new Thread(getImagesRunnable);
            thread.setDaemon(true);
            thread.start();
        });
        Runnable playRunnable = () -> {
            Platform.runLater(() -> {
                playBtn.setText("Pause");
            });
            imageGrid.play();
        }; //playRunnable
        this.playBtn.setOnAction(event -> {
            Thread thread;
            if (isPlaying) {
                thread = new Thread(pauseRunnable);
                isPlaying = false;
            } else {
                thread = new Thread(playRunnable);
                isPlaying = true;
            }
            thread.setDaemon(true);
            thread.start();
        });
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        this.scene = new Scene(this.root, 600, 560);
        this.stage.setOnCloseRequest(event -> Platform.exit());
        this.stage.setTitle("GalleryApp!");
        this.stage.setScene(this.scene);
        this.stage.sizeToScene();
        this.stage.show();
        Platform.runLater(() -> this.stage.setResizable(false));
    } // start

    /** {@inheritDoc} */
    @Override
    public void stop() {
        // feel free to modify this method
        System.out.println("stop() called");
    } // stop

} // GalleryApp
