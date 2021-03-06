package com.uddernetworks.mspaint.gui.window.search;

import com.uddernetworks.mspaint.main.MainGUI;
import com.uddernetworks.mspaint.settings.Setting;
import com.uddernetworks.mspaint.settings.SettingsManager;
import com.uddernetworks.newocr.character.ImageLetter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SearchListCell extends ListCell<SearchResult> {

    @FXML
    private AnchorPane anchor;

    @FXML
    private FlowPane flowText;

    @FXML
    private Label fileName;

    @FXML
    private Label lineNumber;

    private FXMLLoader fxmlLoader;
    private Text beforeText = new Text();
    private Text highlightedText = new Text();
    private Text afterText = new Text();
    private MainGUI mainGUI;

    public SearchListCell(MainGUI mainGUI) {
        this.mainGUI = mainGUI;
    }

    @Override
    public void updateItem(SearchResult searchResult, boolean empty) {
        super.updateItem(searchResult, empty);

        if (empty || searchResult == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (fxmlLoader == null) {
                fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("gui/SearchCell.fxml"));
                fxmlLoader.setController(this);

                try {
                    fxmlLoader.load();
                } catch (IOException e) {
                    e.printStackTrace();
                }


                flowText.getChildren().add(beforeText);
                flowText.getChildren().add(highlightedText);
                flowText.getChildren().add(afterText);

                this.mainGUI.getThemeManager().onDarkThemeChange(anchor, Map.of(
                        ".search-label", "dark",
                        ".found-context", "dark"
                ));
            }

            lineNumber.setAlignment(Pos.TOP_RIGHT);
            fileName.setAlignment(Pos.TOP_RIGHT);

            List<ImageLetter> contextLine = searchResult.getContextLine();
            int beforeIndex = searchResult.getFoundPosition();
            int afterIndex = beforeIndex + searchResult.getImageLetters().size();

            String beforeContext = beforeIndex != 0 ? SearchResult.imageLettersToString(contextLine.subList(0, beforeIndex)) : "";
            String afterContext = afterIndex <= contextLine.size() ? SearchResult.imageLettersToString(contextLine.subList(afterIndex, contextLine.size())) : "";

            beforeText.setText(beforeContext);
            beforeText.getStyleClass().add("found-context");

            highlightedText.setText(SearchResult.imageLettersToString(searchResult.getImageLetters()));
            highlightedText.getStyleClass().add("search-highlight");

            afterText.setText(afterContext);
            afterText.getStyleClass().add("found-context");

            fileName.setText(searchResult.getFile().getName());
            lineNumber.setText(searchResult.getLineNumber() + "\n");

            if (SettingsManager.getInstance().getSetting(Setting.DARK_THEME)) {
                beforeText.getStyleClass().add("dark");
                highlightedText.getStyleClass().add("dark");
                afterText.getStyleClass().add("dark");
            }

            setText(null);
            setGraphic(anchor);
        }
    }
}