
/** *********************************************************************
 **
 ** lookForSelect JavaFX Application.
 **
 **/

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.IllegalStateException;
import java.lang.Runtime;
import java.lang.Thread;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.converter.LocalDateTimeStringConverter;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import javafx.scene.control.ListView;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;


/** *********************************************************************
 ** lookForSelect JavaFX Application.
 **/

public class lookForSelect extends Application {
    // All app static finals.
    static final String   WINDOW_TITLE              = "lookForSelect";
    static final String   WINDOW_ICON               = "lookForSelect.png";

    static final Boolean  WINDOW_ONTOP_DEFAULT      = false;
    static final Double   WINDOW_DEFAULT_POS_X      = 200.0;
    static final Double   WINDOW_DEFAULT_POS_Y      = 200.0;
    static final Double   WINDOW_DEFAULT_WIDTH      = 600.0;
    static final Double   WINDOW_DEFAULT_HEIGHT     = 700.0;

    static final String   WINDOW_ONTOP_PREFNAME     = "Window_OnTop";
    static final String   WINDOW_POS_X_PREFNAME     = "Window_Position_X";
    static final String   WINDOW_POS_Y_PREFNAME     = "Window_Position_Y";
    static final String   WINDOW_WIDTH_PREFNAME     = "Window_Width";
    static final String   WINDOW_HEIGHT_PREFNAME    = "Window_Height";

    static final int LISTVIEW_FONT_SIZE             = 16;

    /** *********************************************************************
     ** Global App var.
     **/
    static final Preferences mPref = Preferences.userRoot().node(WINDOW_TITLE);
    static final ObservableList<String> mStdinStrings =
        FXCollections.observableArrayList();
    static final ListView<String> mListView = new ListView<>();

    static Stage mApplication;
    static Image mApplicationIcon;

    static boolean mShutdownNormal = false;


    /** *********************************************************************
     ** Main Start stage. Set title, icon, etc.
     **/
    @Override
    public void start(Stage stage) {
        mApplication = stage;
        mApplication.setTitle(WINDOW_TITLE);

        setApplicationIcon(mApplication);

        setApplicationProperties(mApplication);

        mApplication.setScene(new Scene(createApplicationScene(),
            WINDOW_DEFAULT_WIDTH, WINDOW_DEFAULT_HEIGHT));
        createApplicationPropertyListeners(mApplication);

        mApplication.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
            }
        });

        setApplicationShutdownHook();

        mApplication.show();

        ensureCorrectInitialPosition(); // Hack, see method.

        // Fast out. Zero items means nothing to display. A single
        // item is immediatley assumed "selected" for action.
        if (mStdinStrings.size() < 2) {
            if (mStdinStrings.size() == 1) {
                System.out.println(mStdinStrings.get(0));
            }
            mApplication.close();
            // Unreach.
        }
    }

    /** *********************************************************************
     ** Stop for normal app close.
     **/
    @Override
    public void stop() {
        mShutdownNormal = true;
    }

    /** *********************************************************************
     ** Helper method loads Window Icon.
     **/
    public void setApplicationIcon(Stage app) {
        mApplication.getIcons().add(new Image(WINDOW_ICON));
    }

    /** *********************************************************************
     ** Restore window property changes @ app start / restart.
     **/
    public void setApplicationProperties(Stage app) {
        app.setAlwaysOnTop(getWindowOnTopValue());

        app.setX(getWindowPosX());
        app.setY(getWindowPosY());

        app.setWidth(getWindowWidth());
        app.setHeight(getWindowHeight());
    }

    /** *********************************************************************
     ** Undocumented framework feature:
     **
     ** Our app scene will position at (0, 0) during insitial show(). It
     ** receives a moveTo(0, 0) request generated internally.
     **
     ** For correct positioning without screen jank:
     **
     **    Option #1 : BEST!  Suppresses reception of incorrect moveTo()'s.
     **                WORST! I don't know why.
     **
     **        { new Dialog<String>(); }
     **
     **    Option #2 : Repeats code already in setApplicationProperties(),
     **                but handles moveTo()'s & fits framework intentions.
     **
     **        mApplication.setX(getWindowPosX());
     **        mApplication.setY(getWindowPosY());
     **/
    public void ensureCorrectInitialPosition() {
        new Dialog<String>();
    }

    /** *********************************************************************
     ** Create main Form / display scene.
     **/
    public VBox createApplicationScene() {
        final VBox sceneBox = new VBox();
        sceneBox.setAlignment(Pos.CENTER);

        // Capture input selection strings from STDIN
        final InputStreamReader instream = new InputStreamReader(System.in);
        final BufferedReader buffer = new BufferedReader(instream);
        try {
            String stdinString = buffer.readLine();
            while (stdinString != null && !stdinString.isEmpty()) {
                mStdinStrings.add(stdinString);
                stdinString = buffer.readLine();
            }
        } catch (Exception e) { }

        mStdinStrings.sort(String.CASE_INSENSITIVE_ORDER);

        // Set desired ListCell FONT size to LISTVIEW_FONT_SIZE.
        mListView.setCellFactory(cell -> {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        setText(item);
                        setFont(Font.font(LISTVIEW_FONT_SIZE));
                    }
                }
            };
        });

        // Set the strings into the View.
        mListView.setItems(mStdinStrings);

        // Add double click listener for item View "selection".
        mListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Process main user request, and exit.
                if (event.getClickCount() == 2) {
                    processUserSelection();
                    mApplication.close();
                }
            }
        });

        // Finalize the scene box and return it.
        sceneBox.getChildren().add(mListView);
        return sceneBox;
    }

    /** *********************************************************************
     ** Process main user request.
     **/
    public static void processUserSelection() {
        System.out.println(mListView.getSelectionModel().getSelectedItem());
    }

    /** *********************************************************************
     ** Capture application property changes for restart.
     **/
    public void createApplicationPropertyListeners(Stage app) {
        app.alwaysOnTopProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue o, Boolean oV, Boolean newValue) {
                setWindowOnTopValue(newValue);
            }});

        app.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue o, Number oV, Number newValue) {
                // System.err.println("Setting X to " + newValue.intValue() + ") requested.");
                setWindowPosX(newValue.doubleValue());
            }});
        app.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue o, Number oV, Number newValue) {
                // System.err.println("Setting Y to " + newValue.intValue() + ") requested.");
                setWindowPosY(newValue.doubleValue());
            }});

        app.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue o, Number oV, Number newValue) {
                setWindowWidth(newValue.doubleValue());
            }});
        app.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue o, Number oV, Number newValue) {
                setWindowHeight(newValue.doubleValue());
            }});
    }

    /** *********************************************************************
     ** Application shutdown hook executes after either/or ;
     **     A)   Normal app shutdown & we've executed stop().
     **     B) Abnormal app shutdown (proc kill) & we've NOT executed stop().
     **/
    public void setApplicationShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
            }
        });
    }

    /** *********************************************************************
     ** Helper methods ... all Preferences getter / setters.
     **/
    public Boolean getWindowOnTopValue() {
        return mPref.getBoolean(WINDOW_ONTOP_PREFNAME, WINDOW_ONTOP_DEFAULT);
    }
    public void setWindowOnTopValue(Boolean onTopValue) {
        mPref.putBoolean(WINDOW_ONTOP_PREFNAME, onTopValue);
        try {
            mPref.flush(); // seriously reuired.
        } catch (BackingStoreException e) {
            throw new IllegalStateException(
                "Java VM Preferences services are unavailable to this app - fatal.");
        }
    }

    public Double getWindowPosX() {
        return mPref.getDouble(WINDOW_POS_X_PREFNAME, WINDOW_DEFAULT_POS_X);
    }
    public void setWindowPosX(Double x) {
        mPref.putDouble(WINDOW_POS_X_PREFNAME, x);
        try {
            mPref.flush(); // seriously reuired.
        } catch (BackingStoreException e) {
            throw new IllegalStateException(
                "Java VM Preferences services are unavailable to this app - fatal.");
        }
    }

    public Double getWindowPosY() {
        return mPref.getDouble(WINDOW_POS_Y_PREFNAME, WINDOW_DEFAULT_POS_Y);
    }
    public void setWindowPosY(Double y) {
        mPref.putDouble(WINDOW_POS_Y_PREFNAME, y);
        try {
            mPref.flush(); // seriously reuired.
        } catch (BackingStoreException e) {
            throw new IllegalStateException(
                "Java VM Preferences services are unavailable to this app - fatal.");
        }
    }

    public Double getWindowWidth() {
        return mPref.getDouble(WINDOW_WIDTH_PREFNAME, WINDOW_DEFAULT_WIDTH);
    }
    public void setWindowWidth(Double w) {
        mPref.putDouble(WINDOW_WIDTH_PREFNAME, w);
        try {
            mPref.flush(); // seriously reuired.
        } catch (BackingStoreException e) {
            throw new IllegalStateException(
                "Java VM Preferences services are unavailable to this app - fatal.");
        }
    }

    public Double getWindowHeight() {
        return mPref.getDouble(WINDOW_HEIGHT_PREFNAME, WINDOW_DEFAULT_HEIGHT);
    }
    public void setWindowHeight(Double h) {
        mPref.putDouble(WINDOW_HEIGHT_PREFNAME, h);
        try {
            mPref.flush(); // seriously reuired.
        } catch (BackingStoreException e) {
            throw new IllegalStateException(
                "Java VM Preferences services are unavailable to this app - fatal.");
        }
    }
}
