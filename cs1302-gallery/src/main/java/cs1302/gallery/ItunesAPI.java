package cs1302.gallery;

import java.net.http.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import cs1302.gallery.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.io.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.application.Platform;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * This class provides methods for querying the iTunes
 * Search API and obtaining a set of distinct images
 * related to a specific search term and media type.
 * The API results are parsed using the GSON library.
 * The class includes a single public static method,
 * {@code call}, that returns an array of JavaFX {@link Image} objects.
 */
class ItunesAPI {

    private static double progress = 0F;

    /**
     * Makes a call to the iTunes search API using the specified
     * search term and media type to retrieve image URLs.
     * The method throws an exception if fewer than 21 distinct
     * image URLs are found. The method also updates the
     * progress bar in the GalleryApp as images are retrieved.
     * @param t the search term
     * @param m the media type (e.g., "music", "movie", etc.)
     * @param incUrls an array of URLs to include in the search results (optional)
     * @return an array of Image objects
     * @throws Exception if fewer than 21 distinct image URLs are found or if an HTTP error occurs
     */
    public static Image[] call(String t, String m, String[] incUrls) throws Exception {
        progress = 0F;
        String termText = URLEncoder.encode(t, StandardCharsets.UTF_8);
        String mediaText = URLEncoder.encode(m, StandardCharsets.UTF_8);
        String query = String.format("?term=%s&limit=200&media=%s", termText, mediaText);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://itunes.apple.com/search" + query))
            .build();
        HttpResponse<String> response = HTTP_CLIENT
            .send(request, BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException(response.toString());
        } // if
        String jsonString = response.body();
        ItunesResponse itunesResponse = GSON
            .fromJson(jsonString, ItunesResponse.class);
        if (itunesResponse.results.length < 21) {
            throw new IllegalArgumentException(
                (itunesResponse.results.length - 1)  +
                " distinct results found, but 21 or more are needed.");
        }
        Image[] imgArray;
        String[] imgURLs = new String[itunesResponse.results.length];
        for (int i = 0; i < itunesResponse.results.length; i++) {
            ItunesResult result = itunesResponse.results[i];
        }
        int index = 0;
        for (int i = 0; i < itunesResponse.results.length; i++) {
            boolean test = true;
            for (int j = 0; j < itunesResponse.results.length; j++) {
                if (itunesResponse.results[i].artworkUrl100.equals(imgURLs[j])) {
                    test = false;
                } //if
            } //for
            if (test) {
                imgURLs[index] = itunesResponse.results[i].artworkUrl100;
                index++;
            } //if
            progress += .45;
            Platform.runLater(() -> {
                GalleryApp.progressBar.setProgress(progress);
            });
        } //for
        if (index - 1 < 21) {
            throw new IllegalArgumentException(
                (index - 1)  + " distinct results found, but 21 or more are needed.");
        }
        imgArray = new Image[index - 1];
        for (int i = 0; i < index - 1; i ++) {
            imgArray[i] = new Image(imgURLs[i]);
            progress += .5;
            Platform.runLater(() -> {
                GalleryApp.progressBar.setProgress(progress);
            });
        }
        Platform.runLater(() -> {
            GalleryApp.progressBar.setProgress(100F);
        });
        return imgArray;
    } //call

    private static HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();

    private static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();


} // ItunesAPI
