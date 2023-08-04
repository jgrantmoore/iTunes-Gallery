
package cs1302.gallery;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.lang.InterruptedException;


/**
 * A JavaFX TilePane that displays a grid of images.
 *
 * This class extends TilePane and provides functionality to display a grid of
 * images in a 5x4 layout, with a maximum size of 600x480 pixels. By default,
 * all 20 images are set to a default image resource, but the grid can be
 * updated with a new set of images using the update() method. The play() method
 * starts an animation that randomly displays images from the updated set in
 * the grid tiles at 2-second intervals, until the pause() method is called to
 * stop the animation. The getUrl() method returns an array of URLs for the
 * images currently displayed in the grid.
 *
 * @see TilePane
 */
class ImageGrid extends TilePane {

    private ImageView[] displayImgs = new ImageView[20];
    private String[] urlList = new String[20];
    private int[] displayed = new int[20];
    private Image[] fullArray;
    private boolean isPlaying = false;

    /**
     * Constructs a new ImageGrid object.
     *
     * This constructor creates a new ImageGrid object with default settings,
     * including a 5x4 layout with a maximum size of 600x480 pixels, and 20 tiles
     * displaying a default image resource. Each tile is represented by an ImageView
     * object with an ID corresponding to its index in the grid. The default image
     * resource is loaded from the "resources/default.png" file path.
     */
    public ImageGrid() {

        super();
        this.setPrefColumns(5);
        this.setPrefRows(4);
        this.setMaxWidth(600);
        this.setMaxHeight(480);


        Image defImg = new Image("file:resources/default.png");
        for (int i = 0; i < 20; i++) {
            displayed[i] = i;
            urlList[i] = "file:resources/default.png";
            ImageView newDef = new ImageView(defImg);
            newDef.setId(String.valueOf(i));
            displayImgs[i] = newDef;
            displayImgs[i].setFitWidth(120);
            displayImgs[i].setFitHeight(120);
            this.getChildren().add(displayImgs[i]);
        } //for

    } //ImageGrid()

    /**
     * Returns an array of URLs for the images currently displayed in the grid.
     *
     * This method returns an array of strings representing the URLs of the images
     * currently displayed in the grid tiles. The URLs are ordered by the index of
     * their corresponding tiles in the grid, from left to right and top to bottom.
     * If an image has not been updated in the grid, its corresponding URL will be
     * "file:resources/default.png".
     *
     * @return an array of strings representing the URLs of the images in the grid
     */
    public String[] getUrl() {
        return urlList;
    }

    /**
     * Updates the images displayed in the grid with the specified array.
     *
     * This method updates the images displayed in the grid tiles with the images
     * in the specified array. The array should have exactly 20 Image objects,
     * representing the images to be displayed in the 20 tiles of the grid. Each
     * tile is represented by an ImageView object, which will display the
     * corresponding Image object. If the specified array is null or has an
     * incorrect length, an IllegalArgumentException will be thrown.
     *
     * @param imgArray an array of exactly 20 Image objects to be displayed in the grid
     * @throws IllegalArgumentException if imgArray is null or has an incorrect length
     */
    public void update(Image[] imgArray) {
        fullArray = imgArray;
        for (int i = 0; i < 20; i++) {
            displayImgs[i].setImage(imgArray[i]);

        } //for

    } //update

    /**
     * Plays a slideshow of images in the grid tiles.
     *
     * This method starts a slideshow of images in the grid tiles. It randomly
     * selects an image from the array of images previously set using the
     * `update()` method, and displays it in a randomly selected tile of the grid.
     * The slideshow continues until the `pause()` method is called. The display
     * time for each image is fixed at 2 seconds. If the array of images has not
     * been set using the `update()` method, an IllegalStateException will be
     * thrown.
     *
     * @throws IllegalStateException if the array of images
     * has not been set using the `update()` method
     */
    public void play() {
        Random rand = new Random();
        isPlaying = true;
        int n = 1;



        while (isPlaying) {
            while (isDisplayed(n)) {
                n = rand.nextInt(fullArray.length);
            }
            int randTile = rand.nextInt(20);


            displayImgs[randTile].setImage(fullArray[n]);
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }

            displayed[randTile] = n;

        }

    }
    /**
     * Pauses the slideshow of images in the grid tiles.
     *
     * This method stops the slideshow of images in the grid tiles that was started
     * by calling the `play()` method. If the slideshow is already paused, this
     * method has no effect.
     */

    public void pause() {
        isPlaying = false;
    }

    /**
     * Returns whether an image with the given index is currently displayed in any tile of the grid.
     *
     * This method returns true if an image with the given index is currently displayed
     * in any tile of the grid, and false otherwise. The index is used to check the
     * `displayed` array, which keeps track of which images are currently displayed
     * in the grid. If the given index is not found in the `displayed` array, this
     * method returns false.
     *
     * @param n the index of the image to check
     * @return true if the image is currently displayed in any tile of the grid, false otherwise
     */
    private boolean isDisplayed(int n) {
        for (int i = 0; i < 20; i++) {
            if (n == displayed[i]) {
                return true;
            }
        }
        return false;
    }

} //ImageGrid
