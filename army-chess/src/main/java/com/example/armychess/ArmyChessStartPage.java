package com.example.armychess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 军旗游戏起始页面类
 */
public class ArmyChessStartPage extends JFrame {

    public ArmyChessStartPage() {
        setTitle("军旗游戏");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("欢迎来到军旗游戏", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 10, 10));

        JButton playerVsPlayerButton = new JButton("玩家对玩家");
        playerVsPlayerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startPlayerVsPlayerGame();
            }
        });
        buttonPanel.add(playerVsPlayerButton);

        JButton playerVsAIButton = new JButton("玩家对AI");
        playerVsAIButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startPlayerVsAIGame();
            }
        });
        buttonPanel.add(playerVsAIButton);

        JButton aiVsAIButton = new JButton("AI对AI");
        aiVsAIButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startAIVsAIGame();
            }
        });
        buttonPanel.add(aiVsAIButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private void startPlayerVsPlayerGame() {
        ArmyChessGame game = new ArmyChessGame();
        game.start();
    }

    private void startPlayerVsAIGame() {
        ArmyChessGame game = new ArmyChessGame();
        ArmyChessAI ai = new ArmyChessAI();
        game.start();
        ai.makeMove();
    }

    private void startAIVsAIGame() {
        ArmyChessAI ai1 = new ArmyChessAI();
        ArmyChessAI ai2 = new ArmyChessAI();
        ai1.makeMove();
        ai2.makeMove();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ArmyChessStartPage startPage = new ArmyChessStartPage();
            startPage.setVisible(true);
        });
    }
}