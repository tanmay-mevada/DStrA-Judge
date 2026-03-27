package com.minijudge;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.Modality;
import com.minijudge.dao.ProblemDAO;
import com.minijudge.dao.SubmissionDAO;
import com.minijudge.judge.JudgeEngine;
import com.minijudge.model.Problem;
import com.minijudge.model.Submission;
import java.util.List;
import java.io.*;
import java.nio.file.*;
import java.util.Optional;

public class App extends Application {

    private Stage primaryStage;
    private Scene mainScene; 
    
    private ProblemDAO problemDAO = new ProblemDAO();
    private SubmissionDAO submissionDAO = new SubmissionDAO();
    private JudgeEngine judgeEngine = new JudgeEngine();

    // ── Color Palette ─────────────────────────────────────────
    private static final String BG_DARK      = "#0f0f1e";
    private static final String BG_CARD      = "#1a1a2e";
    private static final String BG_SURFACE   = "#252538";
    private static final String BG_ELEVATED  = "#2d2d42";
    private static final String ACCENT_BLUE  = "#5a9fff";
    private static final String ACCENT_PURPLE= "#a78bfa";
    private static final String ACCENT_GREEN = "#10e5b0";
    private static final String ACCENT_AMBER = "#ffa454";
    private static final String ACCENT_RED   = "#ff6b7a";
    private static final String TEXT_PRIMARY = "#f0f0ff";
    private static final String TEXT_MUTED   = "#8b8ba7";
    private static final String TEXT_DIM     = "#4a4a66";
    private static final String BORDER       = "#3a3a52";
    private static final String BORDER_LIGHT = "#4a4a66";

    // Standard UI Fonts
    private static final String FONT_UI = "Segoe UI, Helvetica, Arial, sans-serif";
    private static final String FONT_CODE = "Consolas, Courier New, monospace";

    // ── Tabular Column Widths ─────────────────────────────────
    private static final double COL_NUM_WIDTH = 60;
    private static final double COL_TAGS_WIDTH = 220;
    private static final double COL_DIFF_WIDTH = 130;
    private static final double COL_LIMIT_WIDTH = 100;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("⚡ DStrA Judge");
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        
        mainScene = new Scene(new Region(), 1000, 680);
        stage.setScene(mainScene);
        
        showProblemList();
        stage.show();
    }

    // ── Shared UI Components ──────────────────────────────────
    private HBox createTopNavBar(String currentViewTitle) {
        HBox navbar = new HBox(16);
        navbar.setPadding(new Insets(0, 40, 0, 40));
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setPrefHeight(60);
        navbar.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-width: 0 0 1 0;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0, 0, 2);"
        );

        HBox logo = new HBox(8);
        logo.setAlignment(Pos.CENTER_LEFT);
        logo.setCursor(javafx.scene.Cursor.HAND);
        logo.setOnMouseClicked(e -> showProblemList());
        
        Label bolt = new Label("⚡");
        bolt.setFont(Font.font(20));
        
        Label logoText = new Label("DStrA");
        logoText.setFont(Font.font(FONT_UI, FontWeight.BOLD, 18));
        logoText.setTextFill(Color.web(ACCENT_GREEN));
        
        Label logoText2 = new Label("Judge");
        logoText2.setFont(Font.font(FONT_UI, FontWeight.BOLD, 18));
        logoText2.setTextFill(Color.web(ACCENT_BLUE));
        logo.getChildren().addAll(bolt, logoText, logoText2);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        navbar.getChildren().addAll(logo, spacer);

        if (currentViewTitle != null && !currentViewTitle.isEmpty()) {
            Label title = new Label(currentViewTitle);
            title.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));
            title.setTextFill(Color.web(TEXT_MUTED));
            navbar.getChildren().add(title);
        }

        return navbar;
    }

    private WebView createMonacoEditor(String initialCode, String language) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        webView.setContextMenuEnabled(false);
        VBox.setVgrow(webView, Priority.ALWAYS);

        String safeCode = initialCode.replace("\\", "\\\\")
                                     .replace("`", "\\`")
                                     .replace("$", "\\$");

        String langMode = language.toLowerCase();
        if (langMode.equals("c++")) langMode = "cpp";

        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <style>
                    html, body { margin: 0; padding: 0; height: 100%%; overflow: hidden; background-color: %s; }
                    #container { width: 100%%; height: 100%%; }
                </style>
            </head>
            <body>
                <div id="container"></div>
                <script src="https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs/loader.min.js"></script>
                <script>
                    require.config({ paths: { 'vs': 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.44.0/min/vs' }});
                    require(['vs/editor/editor.main'], function() {
                        monaco.editor.defineTheme('dstra-dark', {
                            base: 'vs-dark',
                            inherit: true,
                            rules: [],
                            colors: { 
                                'editor.background': '%s',
                                'editor.lineHighlightBackground': '%s'
                            }
                        });
                        
                        window.editor = monaco.editor.create(document.getElementById('container'), {
                            value: `%s`,
                            language: '%s',
                            theme: 'dstra-dark',
                            automaticLayout: true,
                            minimap: { enabled: false },
                            fontSize: 15,
                            fontFamily: 'Consolas, Courier New, monospace',
                            scrollBeyondLastLine: false,
                            roundedSelection: false,
                            padding: { top: 16 }
                        });
                    });
                    
                    function getCode() { return window.editor ? window.editor.getValue() : ""; }
                    function setCode(newCode) { if (window.editor) window.editor.setValue(newCode); }
                    function formatCode() { if (window.editor) window.editor.getAction('editor.action.formatDocument').run(); }
                    function toggleSuggestions(enable) {
                        if (window.editor) {
                            window.editor.updateOptions({
                                quickSuggestions: enable,
                                suggestOnTriggerCharacters: enable,
                                wordBasedSuggestions: enable ? 'all' : 'off',
                                parameterHints: { enabled: enable }
                            });
                        }
                    }
                </script>
            </body>
            </html>
            """.formatted(BG_DARK, BG_DARK, BG_CARD, safeCode, langMode);

        engine.loadContent(html);
        return webView;
    }

    // ── Screen 1: Problem List ────────────────────────────────
    private void showProblemList() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox navbar = createTopNavBar("Problem Repository");

        VBox hero = new VBox(12);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(40, 50, 30, 50));
        hero.setStyle("-fx-background-color: " + BG_DARK + ";");

        Label heroTitle = new Label("🚀 Coding Challenges");
        heroTitle.setFont(Font.font(FONT_UI, FontWeight.BOLD, 32));
        heroTitle.setTextFill(Color.web(TEXT_PRIMARY));

        List<Problem> problems = problemDAO.getAllProblems();
        
        // ── Search & Filter Bar ──
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER);
        filterBar.setPadding(new Insets(20, 0, 10, 0));
        filterBar.setMaxWidth(1100);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by title or #tag...");
        searchField.setFont(Font.font(FONT_UI, 14));
        searchField.setPrefWidth(400);
        searchField.setStyle(
            "-fx-background-color: " + BG_ELEVATED + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-prompt-text-fill: " + TEXT_MUTED + ";" +
            "-fx-border-color: " + BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 16;"
        );

        ToggleGroup diffGroup = new ToggleGroup();
        HBox diffToggles = new HBox(6);
        diffToggles.setAlignment(Pos.CENTER);
        
        ToggleButton btnAll = createFilterToggle("All", diffGroup, true);
        ToggleButton btnEasy = createFilterToggle("Easy", diffGroup, false);
        ToggleButton btnMed = createFilterToggle("Medium", diffGroup, false);
        ToggleButton btnHard = createFilterToggle("Hard", diffGroup, false);
        
        diffToggles.getChildren().addAll(btnAll, btnEasy, btnMed, btnHard);
        
        Region filterSpacer = new Region();
        HBox.setHgrow(filterSpacer, Priority.ALWAYS);
        
        filterBar.getChildren().addAll(searchField, filterSpacer, diffToggles);

        hero.getChildren().addAll(heroTitle, filterBar);

        // ── List Container ──
        VBox listContainer = new VBox(0);
        listContainer.setMaxWidth(1100); 
        listContainer.setAlignment(Pos.CENTER);
        
        VBox wrapper = new VBox(listContainer);
        wrapper.setAlignment(Pos.TOP_CENTER);
        wrapper.setStyle("-fx-background-color: " + BG_CARD + "; -fx-background-radius: 12 12 0 0; -fx-padding: 0;");

        HBox header = new HBox(0); 
        header.setPadding(new Insets(16, 32, 16, 32));
        header.setStyle("-fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        Label hNum = makeHeaderLabel("#", COL_NUM_WIDTH);
        Label hTitle = makeHeaderLabel("Title", -1);
        HBox titleCol = new HBox(hTitle);
        titleCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleCol, Priority.ALWAYS); 
        Label hTags = makeHeaderLabel("Tags", COL_TAGS_WIDTH);
        Label hDiff = makeHeaderLabel("Difficulty", COL_DIFF_WIDTH);
        Label hTime = makeHeaderLabel("Limit", COL_LIMIT_WIDTH);

        header.getChildren().addAll(hNum, titleCol, hTags, hDiff, hTime);

        // Function to refresh list based on filters
        Runnable applyFilters = () -> {
            listContainer.getChildren().clear();
            listContainer.getChildren().add(header);
            
            String query = searchField.getText().toLowerCase().trim();
            ToggleButton selectedDiff = (ToggleButton) diffGroup.getSelectedToggle();
            String diffFilter = selectedDiff == null ? "All" : selectedDiff.getText();
            
            int displayCount = 0;
            for (int i = 0; i < problems.size(); i++) {
                Problem p = problems.get(i);
                
                boolean matchesSearch = query.isEmpty() || 
                                        p.getTitle().toLowerCase().contains(query) || 
                                        (p.getTags() != null && p.getTags().toLowerCase().contains(query));
                
                boolean matchesDiff = diffFilter.equals("All") || p.getDifficulty().equals(diffFilter);
                
                if (matchesSearch && matchesDiff) {
                    HBox row = makeProblemRow(p, i);
                    listContainer.getChildren().add(row);
                    fadeIn(row, displayCount * 30);
                    displayCount++;
                }
            }
            
            if (displayCount == 0) {
                Label noRes = new Label("No problems found.");
                noRes.setFont(Font.font(FONT_UI, 14));
                noRes.setTextFill(Color.web(TEXT_MUTED));
                noRes.setPadding(new Insets(40));
                listContainer.getChildren().add(noRes);
            }
        };

        // Attach listeners
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());
        diffGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> applyFilters.run());

        // Initial render
        applyFilters.run();

        ScrollPane scroll = new ScrollPane(wrapper);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: " + BG_DARK + "; -fx-background-color: " + BG_DARK + "; -fx-border-color: transparent;");

        VBox topSection = new VBox(navbar, hero);
        root.setTop(topSection);
        root.setCenter(scroll);

        mainScene.setRoot(root);
        
        // Auto-focus search field
        Platform.runLater(searchField::requestFocus);
    }

    private ToggleButton createFilterToggle(String text, ToggleGroup group, boolean selected) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.setFont(Font.font(FONT_UI, FontWeight.BOLD, 12));
        
        String baseStyle = "-fx-background-color: " + BG_ELEVATED + "; -fx-text-fill: " + TEXT_MUTED + "; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 6 16;";
        String selectedStyle = "-fx-background-color: " + ACCENT_BLUE + "; -fx-text-fill: #ffffff; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 6 16;";
        
        btn.setStyle(selected ? selectedStyle : baseStyle);
        
        btn.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                btn.setStyle(selectedStyle);
            } else {
                btn.setStyle(baseStyle);
            }
        });
        
        return btn;
    }

    private HBox makeProblemRow(Problem p, int index) {
        HBox row = new HBox(0); 
        row.setPadding(new Insets(20, 32, 20, 32));
        row.setAlignment(Pos.CENTER_LEFT);
        String baseStyle = "-fx-background-color: transparent; -fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0; -fx-cursor: hand;";
        row.setStyle(baseStyle);

        Label num = new Label(String.format("%02d", p.getProblemId()));
        num.setFont(Font.font(FONT_CODE, FontWeight.BOLD, 14));
        num.setTextFill(Color.web(TEXT_MUTED));
        num.setPrefWidth(COL_NUM_WIDTH);
        num.setMinWidth(COL_NUM_WIDTH);

        Label title = new Label(p.getTitle());
        title.setFont(Font.font(FONT_UI, FontWeight.BOLD, 15));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        
        HBox titleCol = new HBox(title);
        titleCol.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleCol, Priority.ALWAYS);

        HBox tagBox = new HBox(8);
        tagBox.setPrefWidth(COL_TAGS_WIDTH);
        tagBox.setMinWidth(COL_TAGS_WIDTH);
        tagBox.setAlignment(Pos.CENTER_LEFT);
        if (p.getTags() != null && !p.getTags().isEmpty()) {
            for (String tag : p.getTags().split(",")) {
                Label tagLabel = new Label(tag.trim());
                tagLabel.setFont(Font.font(FONT_CODE, 11));
                tagLabel.setTextFill(Color.web(ACCENT_BLUE));
                tagLabel.setStyle("-fx-background-color: " + BG_ELEVATED + "; -fx-background-radius: 4; -fx-padding: 3 8 3 8;");
                tagBox.getChildren().add(tagLabel);
            }
        }

        Label diff = new Label(p.getDifficulty());
        diff.setFont(Font.font(FONT_UI, FontWeight.BOLD, 12));
        String[] dc = getDiffColors(p.getDifficulty());
        diff.setStyle("-fx-background-color: " + dc[1] + "; -fx-text-fill: " + dc[0] + "; -fx-background-radius: 12; -fx-padding: 4 12 4 12;");
        
        HBox diffCol = new HBox(diff);
        diffCol.setPrefWidth(COL_DIFF_WIDTH);
        diffCol.setMinWidth(COL_DIFF_WIDTH);
        diffCol.setAlignment(Pos.CENTER_LEFT);

        Label time = new Label("⏱ " + p.getTimeLimitMs() + "ms");
        time.setFont(Font.font(FONT_CODE, 12));
        time.setTextFill(Color.web(TEXT_MUTED));
        time.setPrefWidth(COL_LIMIT_WIDTH);
        time.setMinWidth(COL_LIMIT_WIDTH);

        row.getChildren().addAll(num, titleCol, tagBox, diffCol, time);

        String hoverStyle = "-fx-background-color: " + BG_ELEVATED + "; -fx-border-color: " + ACCENT_BLUE + "; -fx-border-width: 0 0 1 0; -fx-cursor: hand;";
        row.setOnMouseEntered(e -> {
            row.setStyle(hoverStyle);
            num.setTextFill(Color.web(ACCENT_BLUE));
        });
        row.setOnMouseExited(e -> {
            row.setStyle(baseStyle);
            num.setTextFill(Color.web(TEXT_MUTED));
        });
        row.setOnMouseClicked(e -> showProblemEditor(p, getTemplate("Java")));

        return row;
    }

    // ── Screen 2: Problem Editor ──────────────────────────────
    private void showProblemEditor(Problem p, String codeContent) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox topBar = createTopNavBar("Workspace");

        SplitPane splitPane = new SplitPane();
        splitPane.setStyle(
            "-fx-background-color: " + BG_DARK + ";" + 
            "-fx-box-border: transparent;" +
            "-fx-control-inner-background: " + BG_DARK + ";"
        );

        // Left: problem description
        VBox leftPanel = new VBox(20);
        leftPanel.setPadding(new Insets(28, 28, 28, 28));
        leftPanel.setStyle("-fx-background-color: " + BG_CARD + ";");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label problemTitle = new Label(p.getProblemId() + ". " + p.getTitle());
        problemTitle.setFont(Font.font(FONT_UI, FontWeight.BOLD, 22));
        problemTitle.setTextFill(Color.web(TEXT_PRIMARY));
        
        String[] dc = getDiffColors(p.getDifficulty());
        Label diffBadge = new Label(p.getDifficulty());
        diffBadge.setFont(Font.font(FONT_UI, FontWeight.BOLD, 11));
        diffBadge.setStyle("-fx-background-color: " + dc[1] + "; -fx-text-fill: " + dc[0] + "; -fx-background-radius: 12; -fx-padding: 4 12 4 12;");
        titleRow.getChildren().addAll(problemTitle, diffBadge);

        Label statement = new Label(p.getStatement());
        statement.setFont(Font.font(FONT_UI, 14));
        statement.setTextFill(Color.web(TEXT_PRIMARY));
        statement.setWrapText(true);
        statement.setLineSpacing(6);

        VBox constraints = new VBox(8);
        constraints.setPadding(new Insets(16));
        constraints.setStyle("-fx-background-color: " + BG_SURFACE + "; -fx-background-radius: 8; -fx-border-color: " + BORDER + "; -fx-border-radius: 8;");

        Label constTitle = new Label("Constraints");
        constTitle.setFont(Font.font(FONT_UI, FontWeight.BOLD, 13));
        constTitle.setTextFill(Color.web(TEXT_MUTED));

        Label timeConst = new Label("• Time limit:   " + p.getTimeLimitMs() + " ms");
        timeConst.setFont(Font.font(FONT_CODE, 13));
        timeConst.setTextFill(Color.web(TEXT_PRIMARY));

        Label memConst = new Label("• Memory limit: " + p.getMemoryLimitMb() + " MB");
        memConst.setFont(Font.font(FONT_CODE, 13));
        memConst.setTextFill(Color.web(TEXT_PRIMARY));

        constraints.getChildren().addAll(constTitle, timeConst, memConst);
        leftPanel.getChildren().addAll(titleRow, statement, constraints);

        ScrollPane leftScroll = new ScrollPane(leftPanel);
        leftScroll.setFitToWidth(true);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setStyle("-fx-background: " + BG_CARD + "; -fx-background-color: " + BG_CARD + "; -fx-border-color: transparent;");

        // Right: Editor Panel
        VBox rightPanel = new VBox(0);
        rightPanel.setStyle("-fx-background-color: " + BG_DARK + ";");

        WebView monacoView = createMonacoEditor(codeContent, "java");

        HBox editorBar = new HBox(12);
        editorBar.setPadding(new Insets(10, 16, 10, 16));
        editorBar.setAlignment(Pos.CENTER_LEFT);
        editorBar.setStyle("-fx-background-color: " + BG_SURFACE + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");

        Label langIcon = new Label("{ }");
        langIcon.setFont(Font.font(FONT_CODE, FontWeight.BOLD, 14));
        langIcon.setTextFill(Color.web(ACCENT_BLUE));

        Label langLabel = new Label("Java");
        langLabel.setFont(Font.font(FONT_UI, FontWeight.BOLD, 13));
        langLabel.setTextFill(Color.web(TEXT_PRIMARY));

        Region toolSpacer = new Region();
        HBox.setHgrow(toolSpacer, Priority.ALWAYS);

        CheckBox suggestToggle = new CheckBox("Autocomplete");
        suggestToggle.setSelected(true);
        suggestToggle.setFont(Font.font(FONT_UI, 12));
        suggestToggle.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
        suggestToggle.setTooltip(new Tooltip("Enable/Disable intelligent code suggestions"));
        suggestToggle.setOnAction(e -> {
            boolean enabled = suggestToggle.isSelected();
            monacoView.getEngine().executeScript("toggleSuggestions(" + enabled + ")");
        });

        Button formatBtn = createToolbarButton("✨ Format");
        formatBtn.setTooltip(new Tooltip("Auto-format document"));
        formatBtn.setOnAction(e -> monacoView.getEngine().executeScript("formatCode()"));

        Button resetBtn = createToolbarButton("🔄 Reset");
        resetBtn.setTooltip(new Tooltip("Restore to original boilerplate code"));
        resetBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reset Code");
            alert.setHeaderText("Reset to Boilerplate?");
            alert.setContentText("This will erase all your current code. Continue?");
            
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + BG_CARD + "; -fx-text-fill: white;");
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: " + TEXT_MUTED + ";");
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: " + BG_SURFACE + ";");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String safeTemplate = getTemplate("Java").replace("\\", "\\\\").replace("`", "\\`").replace("$", "\\$");
                monacoView.getEngine().executeScript("setCode(`" + safeTemplate + "`)");
            }
        });

        editorBar.getChildren().addAll(langIcon, langLabel, toolSpacer, suggestToggle, formatBtn, resetBtn);

        HBox submitBar = new HBox(14);
        submitBar.setPadding(new Insets(16, 24, 16, 24));
        submitBar.setAlignment(Pos.CENTER_RIGHT);
        submitBar.setStyle("-fx-background-color: " + BG_SURFACE + "; -fx-border-color: " + BORDER_LIGHT + "; -fx-border-width: 1 0 0 0;");

        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font(FONT_UI, 13));
        statusLabel.setTextFill(Color.web(TEXT_MUTED));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        Button runBtn = createStyledButton("▶ Run Code", BG_ELEVATED, TEXT_PRIMARY, BORDER_LIGHT);
        runBtn.setOnAction(e -> {
            String currentCode = (String) monacoView.getEngine().executeScript("getCode()");
            showRunDialog(currentCode);
        });

        Button submitBtn = createStyledButton("✓ Submit", ACCENT_GREEN, "#0d0d14", ACCENT_GREEN);
        submitBtn.setOnAction(e -> {
            submitBtn.setText("⏳ Judging...");
            submitBtn.setDisable(true);
            runBtn.setDisable(true);
            statusLabel.setText("Running test cases...");
            statusLabel.setTextFill(Color.web(ACCENT_AMBER));

            String code = (String) monacoView.getEngine().executeScript("getCode()");
            if (code == null || code.isEmpty() || code.equals("Loading...")) {
                statusLabel.setText("Error: Editor not fully loaded.");
                submitBtn.setDisable(false);
                runBtn.setDisable(false);
                return;
            }

            String lang = "Java";

            new Thread(() -> {
                Submission sub = new Submission(p.getProblemId(), lang, code);
                int subId = submissionDAO.insertSubmission(sub);
                String verdict = judgeEngine.judge(subId, p.getProblemId(), lang, code, p.getTimeLimitMs());
                Platform.runLater(() -> showVerdict(p, verdict, code, lang));
            }).start();
        });

        submitBar.getChildren().addAll(statusLabel, runBtn, submitBtn);
        rightPanel.getChildren().addAll(editorBar, monacoView, submitBar);

        splitPane.getItems().addAll(leftScroll, rightPanel);
        splitPane.setDividerPositions(0.40); 

        root.setTop(topBar);
        root.setCenter(splitPane);

        mainScene.setRoot(root);
    }

    // ── Screen 3: Verdict ─────────────────────────────────────
    private void showVerdict(Problem p, String verdict, String code, String lang) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        HBox topBar = createTopNavBar("Verdict");

        VBox center = new VBox(28);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(60, 80, 60, 80));

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 60, 50, 60));
        card.setMaxWidth(560);
        card.setStyle(
            "-fx-background-color: " + BG_CARD + ";" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: " + getVerdictBorderColor(verdict) + ";" +
            "-fx-border-radius: 16;" +
            "-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 8);"
        );

        Label icon = new Label(getVerdictIcon(verdict));
        icon.setFont(Font.font(64));

        Label verdictLabel = new Label(getVerdictText(verdict));
        verdictLabel.setFont(Font.font(FONT_UI, FontWeight.BOLD, 36));
        verdictLabel.setTextFill(Color.web(getVerdictColor(verdict)));

        Label desc = new Label(getVerdictDescription(verdict));
        desc.setFont(Font.font(FONT_UI, 15));
        desc.setTextFill(Color.web(TEXT_MUTED));
        desc.setWrapText(true);
        desc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Rectangle divider = new Rectangle(320, 1);
        divider.setFill(Color.web(BORDER_LIGHT));

        Label probName = new Label("Problem: " + p.getTitle());
        probName.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));
        probName.setTextFill(Color.web(ACCENT_BLUE));

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);
        buttons.setPadding(new Insets(16, 0, 0, 0));

        Button tryAgain = createStyledButton("← Edit Code", BG_ELEVATED, TEXT_PRIMARY, BORDER_LIGHT);
        tryAgain.setOnAction(e -> showProblemEditor(p, code));

        Button allProblems = createStyledButton("Problem List", ACCENT_BLUE, "#ffffff", ACCENT_BLUE);
        allProblems.setOnAction(e -> showProblemList());

        buttons.getChildren().addAll(tryAgain, allProblems);
        card.getChildren().addAll(icon, verdictLabel, desc, divider, probName, buttons);

        card.setOpacity(0);
        card.setTranslateY(30);
        FadeTransition ft = new FadeTransition(Duration.millis(400), card);
        ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), card);
        tt.setToY(0);
        new ParallelTransition(ft, tt).play();

        center.getChildren().add(card);
        root.setTop(topBar);
        root.setCenter(center);

        mainScene.setRoot(root);
    }

    // ── Dialog: Run Code ──────────────────────────────────────
    private void showRunDialog(String code) {
        Stage runStage = new Stage();
        runStage.setTitle("Run Code | DStrA Judge");
        runStage.setWidth(900);
        runStage.setHeight(600);
        runStage.initModality(Modality.APPLICATION_MODAL);
        runStage.initOwner(primaryStage);
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");
        
        HBox topBar = new HBox(12);
        topBar.setPadding(new Insets(18, 24, 18, 24));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER + "; -fx-border-width: 0 0 1 0;");
        
        Label title = new Label("▶ Custom Run");
        title.setFont(Font.font(FONT_UI, FontWeight.BOLD, 16));
        title.setTextFill(Color.web(TEXT_PRIMARY));
        topBar.getChildren().add(title);
        root.setTop(topBar);
        
        SplitPane splitPane = new SplitPane();
        splitPane.setStyle("-fx-background-color: " + BG_DARK + "; -fx-box-border: transparent;");
        
        VBox inputSection = new VBox(10);
        inputSection.setPadding(new Insets(20));
        Label inputLabel = new Label("Standard Input");
        inputLabel.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));
        inputLabel.setTextFill(Color.web(TEXT_MUTED));
        TextArea inputArea = new TextArea();
        inputArea.setFont(Font.font(FONT_CODE, 14));
        inputArea.setStyle("-fx-control-inner-background: " + BG_ELEVATED + "; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-background-color: transparent;");
        VBox.setVgrow(inputArea, Priority.ALWAYS);
        inputSection.getChildren().addAll(inputLabel, inputArea);
        
        VBox outputSection = new VBox(10);
        outputSection.setPadding(new Insets(20));
        Label outputLabel = new Label("Standard Output");
        outputLabel.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));
        outputLabel.setTextFill(Color.web(TEXT_MUTED));
        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setFont(Font.font(FONT_CODE, 14));
        outputArea.setStyle("-fx-control-inner-background: " + BG_SURFACE + "; -fx-text-fill: " + TEXT_PRIMARY + "; -fx-background-color: transparent;");
        VBox.setVgrow(outputArea, Priority.ALWAYS);
        outputSection.getChildren().addAll(outputLabel, outputArea);
        
        splitPane.getItems().addAll(inputSection, outputSection);
        splitPane.setDividerPositions(0.5);
        root.setCenter(splitPane);
        
        HBox bottomBar = new HBox(12);
        bottomBar.setPadding(new Insets(16, 24, 16, 24));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.setStyle("-fx-background-color: " + BG_CARD + "; -fx-border-color: " + BORDER + "; -fx-border-width: 1 0 0 0;");
        
        Label statusLabel = new Label("");
        statusLabel.setFont(Font.font(FONT_UI, 13));
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        
        Button runButton = createStyledButton("▶ Run", ACCENT_GREEN, "#0d0d14", ACCENT_GREEN);
        runButton.setOnAction(e -> {
            runButton.setDisable(true);
            statusLabel.setText("⏳ Compiling and running...");
            statusLabel.setTextFill(Color.web(ACCENT_AMBER));
            outputArea.clear();
            
            new Thread(() -> {
                try {
                    String output = runCodeWithInput(code, inputArea.getText());
                    Platform.runLater(() -> {
                        outputArea.setText(output);
                        statusLabel.setText("✓ Done");
                        statusLabel.setTextFill(Color.web(ACCENT_GREEN));
                        runButton.setDisable(false);
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        outputArea.setText("❌ Error:\n" + ex.getMessage());
                        statusLabel.setText("✗ Failed");
                        statusLabel.setTextFill(Color.web(ACCENT_RED));
                        runButton.setDisable(false);
                    });
                }
            }).start();
        });
        
        bottomBar.getChildren().addAll(statusLabel, runButton);
        root.setBottom(bottomBar);
        
        Scene scene = new Scene(root);
        runStage.setScene(scene);
        runStage.show();
    }

    // ── Helpers ───────────────────────────────────────────────
    private Label makeHeaderLabel(String text, double width) {
        Label l = new Label(text.toUpperCase());
        l.setFont(Font.font(FONT_UI, FontWeight.BOLD, 12));
        l.setTextFill(Color.web(TEXT_DIM));
        if (width > 0) {
            l.setMinWidth(width);
            l.setPrefWidth(width);
        }
        return l;
    }

    private HBox makeStatChip(String value, String label, String color) {
        HBox chip = new HBox(10);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(8, 16, 8, 16));
        chip.setStyle("-fx-background-color: " + BG_SURFACE + "; -fx-background-radius: 8;");

        Label dot = new Label("●");
        dot.setFont(Font.font(10));
        dot.setTextFill(Color.web(color));

        Label val = new Label(value);
        val.setFont(Font.font(FONT_UI, FontWeight.BOLD, 16));
        val.setTextFill(Color.web(color));

        Label lbl = new Label(label);
        lbl.setFont(Font.font(FONT_UI, 13));
        lbl.setTextFill(Color.web(TEXT_MUTED));

        chip.getChildren().addAll(dot, val, lbl);
        return chip;
    }

    private String[] getDiffColors(String diff) {
        return switch (diff) {
            case "Easy"   -> new String[]{ ACCENT_GREEN, "#0a2e1f" };
            case "Medium" -> new String[]{ ACCENT_AMBER, "#2e1f0a" };
            case "Hard"   -> new String[]{ ACCENT_RED,   "#2e0a14" };
            default       -> new String[]{ TEXT_MUTED,   BG_SURFACE };
        };
    }

    private void fadeIn(Node node, int delayMs) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setDelay(Duration.millis(delayMs));
        ft.setToValue(1);
        ft.play();
    }

    private Button createStyledButton(String text, String bgColor, String textColor, String borderColor) {
        Button btn = new Button(text);
        btn.setFont(Font.font(FONT_UI, FontWeight.BOLD, 13));
        btn.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-text-fill: " + textColor + ";" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 6;" +
            "-fx-border-color: " + borderColor + ";" +
            "-fx-border-radius: 6;" +
            "-fx-border-width: 1;" +
            "-fx-padding: 8 20 8 20;"
        );
        
        btn.setOnMouseEntered(e -> {
            if (!btn.isDisabled()) {
                btn.setStyle(
                    "-fx-background-color: " + lightenColor(bgColor) + ";" +
                    "-fx-text-fill: " + textColor + ";" +
                    "-fx-cursor: hand;" +
                    "-fx-background-radius: 6;" +
                    "-fx-border-color: " + lightenColor(borderColor) + ";" +
                    "-fx-border-radius: 6;" +
                    "-fx-border-width: 1;" +
                    "-fx-padding: 8 20 8 20;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 6, 0, 0, 2);"
                );
            }
        });
        
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-cursor: hand;" +
                "-fx-background-radius: 6;" +
                "-fx-border-color: " + borderColor + ";" +
                "-fx-border-radius: 6;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 8 20 8 20;"
            );
        });
        
        return btn;
    }

    private Button createToolbarButton(String text) {
        Button btn = new Button(text);
        btn.setFont(Font.font(FONT_UI, 12));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + "; -fx-cursor: hand; -fx-padding: 4 8 4 8;");
        
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + BG_ELEVATED + ";" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-cursor: hand;" +
            "-fx-background-radius: 4;" +
            "-fx-padding: 4 8 4 8;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + TEXT_MUTED + ";" +
            "-fx-cursor: hand;" +
            "-fx-padding: 4 8 4 8;"
        ));
        return btn;
    }

    private String lightenColor(String hex) {
        try {
            hex = hex.replace("#", "");
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            r = Math.min(255, r + 25);
            g = Math.min(255, g + 25);
            b = Math.min(255, b + 25);
            return String.format("#%02x%02x%02x", r, g, b);
        } catch (Exception e) { return hex; }
    }

    private String runCodeWithInput(String code, String input) throws Exception {
        Path tempDir = Files.createTempDirectory("run_");
        Path sourceFile = tempDir.resolve("Main.java");
        Files.writeString(sourceFile, code);
        
        try {
            ProcessBuilder compilePb = new ProcessBuilder("javac", sourceFile.toString())
                    .directory(tempDir.toFile()).redirectErrorStream(true);
            compilePb.environment().keySet().removeIf(k -> k.contains("JAVA_OPTIONS"));
            Process compile = compilePb.start();
            
            String compileOutput = new String(compile.getInputStream().readAllBytes());
            if (compile.waitFor() != 0) return "Compilation Error:\n" + compileOutput;
            
            ProcessBuilder runPb = new ProcessBuilder("java", "-XX:+UseSerialGC", "Main")
                    .directory(tempDir.toFile()).redirectErrorStream(true);
            runPb.environment().keySet().removeIf(k -> k.contains("JAVA_OPTIONS"));
            Process run = runPb.start();
            
            try (OutputStream os = run.getOutputStream()) {
                os.write(input.getBytes());
            }
            
            if (!run.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) {
                run.destroyForcibly();
                return "Time Limit Exceeded (5s timeout)";
            }
            
            String output = new String(run.getInputStream().readAllBytes());
            return output.isEmpty() ? "(No output)" : output;
        } finally {
            Files.walk(tempDir).sorted(java.util.Comparator.reverseOrder())
                 .map(Path::toFile).forEach(File::delete);
        }
    }

    private String getTemplate(String lang) {
        return "import java.util.Scanner;\n\npublic class Main {\n    public static void main(String[] args) {\n        Scanner sc = new Scanner(System.in);\n        // Write your solution here\n\n    }\n}";
    }

    private String getVerdictIcon(String v) {
        return switch (v) {
            case "AC"  -> "🎉";
            case "WA", "RE" -> "✗";
            case "TLE" -> "⏱";
            case "CE"  -> "⚠";
            default    -> "?";
        };
    }

    private String getVerdictText(String v) {
        return switch (v) {
            case "AC"  -> "Accepted";
            case "WA"  -> "Wrong Answer";
            case "TLE" -> "Time Limit Exceeded";
            case "CE"  -> "Compilation Error";
            case "RE"  -> "Runtime Error";
            default    -> "Unknown Error";
        };
    }

    private String getVerdictColor(String v) {
        return switch (v) {
            case "AC"  -> ACCENT_GREEN;
            case "WA", "RE" -> ACCENT_RED;
            case "TLE", "CE" -> ACCENT_AMBER;
            default    -> TEXT_MUTED;
        };
    }

    private String getVerdictBorderColor(String v) {
        return switch (v) {
            case "AC"  -> "#0a3d25";
            case "WA", "RE" -> "#3d0a14";
            case "TLE", "CE" -> "#3d2a0a";
            default    -> BORDER;
        };
    }

    private String getVerdictDescription(String v) {
        return switch (v) {
            case "AC"  -> "Your solution passed all test cases!";
            case "WA"  -> "Your output didn't match the expected output.";
            case "TLE" -> "Your solution exceeded the time limit.";
            case "CE"  -> "Your code failed to compile. Check for syntax errors.";
            case "RE"  -> "Your program crashed during execution.";
            default    -> "An unexpected error occurred.";
        };
    }

    public static void main(String[] args) { launch(args); }
}