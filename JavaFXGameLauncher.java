import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 纯 JavaFX 游戏启动器，避免 Swing+JavaFX 混合导致的崩溃
 */
public class JavaFXGameLauncher extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("多游戏平台 - Java 11 + JavaFX 17");
        
        // 创建主要布局
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f5f5dc;");

        // 标题
        Label titleLabel = new Label("🎮 欢迎来到游戏中心");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #8b4513;");

        // 游戏按钮网格
        GridPane gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setHgap(20);
        gameGrid.setVgap(20);
        gameGrid.setPadding(new Insets(20));

        // 中国象棋
        Button chineseChessBtn = createGameButton("🏮 中国象棋", "传统的中国象棋游戏");
        chineseChessBtn.setOnAction(e -> {
            showMessage("中国象棋游戏启动中...");
            startChineseChess();
        });

        // 围棋
        Button goBtn = createGameButton("⚫⚪ 围棋", "古老的策略棋盘游戏");
        goBtn.setOnAction(e -> {
            showMessage("围棋游戏启动中...");
            startGo();
        });

        // 飞行棋
        Button flightBtn = createGameButton("✈️ 飞行棋", "有趣的家庭友好型桌面游戏");
        flightBtn.setOnAction(e -> {
            showMessage("飞行棋游戏启动中...");
            startFlightChess();
        });

        // 坦克大战
        Button tankBtn = createGameButton("🚗 坦克大战", "经典的坦克对战游戏");
        tankBtn.setOnAction(e -> {
            showMessage("坦克大战游戏启动中...");
            startTankBattle();
        });

        // 街头霸王 - 独立启动
        Button streetFighterBtn = createGameButton("👊 街头霸王", "激烈的格斗游戏");
        streetFighterBtn.setOnAction(e -> {
            showMessage("街头霸王游戏启动中...");
            startStreetFighter();
        });
        
        // 街头霸王增强版
        Button streetFighterNewBtn = createGameButton("🥊 街头霸王增强版", "全新的街头霸王体验");
        streetFighterNewBtn.setOnAction(e -> {
            showMessage("街头霸王增强版启动中...");
            startStreetFighterNew();
        });

        // 其他游戏占位符
        Button otherBtn = createGameButton("🎯 更多游戏", "敬请期待...");
        otherBtn.setOnAction(e -> showMessage("更多精彩游戏正在开发中，敬请期待！"));

        // 布局游戏按钮
        gameGrid.add(chineseChessBtn, 0, 0);
        gameGrid.add(goBtn, 1, 0);
        gameGrid.add(flightBtn, 2, 0);
        gameGrid.add(tankBtn, 0, 1);
        gameGrid.add(streetFighterBtn, 1, 1);
        gameGrid.add(streetFighterNewBtn, 2, 1);
        
        gameGrid.add(otherBtn, 0, 2);

        // 退出按钮
        Button exitBtn = new Button("退出游戏中心");
        exitBtn.setStyle("-fx-background-color: #dc143c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20;");
        exitBtn.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(titleLabel, gameGrid, exitBtn);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createGameButton(String title, String description) {
        Button btn = new Button(title + "\n" + description);
        btn.setPrefSize(200, 100);
        btn.setStyle(
            "-fx-background-color: #fffaf0; " +
            "-fx-border-color: #8b4513; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 12px; " +
            "-fx-text-alignment: center;"
        );
        
        // 鼠标悬停效果
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #f0f8ff; " +
            "-fx-border-color: #1e90ff; " +
            "-fx-border-width: 3; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 12px; " +
            "-fx-text-alignment: center;"
        ));
        
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: #fffaf0; " +
            "-fx-border-color: #8b4513; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5; " +
            "-fx-font-size: 12px; " +
            "-fx-text-alignment: center;"
        ));
        
        return btn;
    }

    private void showMessage(String message) {
        System.out.println("🎮 " + message);
    }

    private void startChineseChess() {
        // 在新线程中启动，避免阻塞 JavaFX 线程
        new Thread(() -> {
            try {
                Class<?> gameFrameClass = Class.forName("com.example.chinesechess.ui.GameFrame");
                Object frame = gameFrameClass.getDeclaredConstructor().newInstance();
                gameFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("启动中国象棋失败: " + e.getMessage());
            }
        }).start();
    }

    private void startGo() {
        new Thread(() -> {
            try {
                Class<?> gameFrameClass = Class.forName("com.example.go.GoFrame");
                Object frame = gameFrameClass.getDeclaredConstructor().newInstance();
                gameFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("启动围棋失败: " + e.getMessage());
            }
        }).start();
    }

    private void startFlightChess() {
        new Thread(() -> {
            try {
                Class<?> gameFrameClass = Class.forName("com.example.flightchess.FlightChessFrame");
                Object frame = gameFrameClass.getDeclaredConstructor().newInstance();
                gameFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("启动飞行棋失败: " + e.getMessage());
            }
        }).start();
    }

    private void startTankBattle() {
        new Thread(() -> {
            try {
                Class<?> gameClass = Class.forName("com.tankbattle.TankBattleGame");
                gameClass.getMethod("main", String[].class).invoke(null, (Object) new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("启动坦克大战失败: " + e.getMessage());
            }
        }).start();
    }

    private void startStreetFighter() {
        // 街头霸王使用独立进程启动，避免 JavaFX 线程冲突
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "bash", "-c", 
                    "cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighter && mvn javafx:run"
                );
                pb.inheritIO();
                Process process = pb.start();
                showMessage("街头霸王已在独立进程中启动");
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("启动街头霸王失败: " + e.getMessage());
            }
        }).start();
    }
    
    private void startStreetFighterNew() {
        // 街头霸王增强版使用独立进程启动
        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "bash", "-c", 
                    "cd /Users/wangxiaozhe/workspace/chinese-chess-game/StreetFighterNew && mvn javafx:run"
                );
                pb.inheritIO();
                Process process = pb.start();
                showMessage("街头霸王增强版已在独立进程中启动");
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("启动街头霸王增强版失败: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
