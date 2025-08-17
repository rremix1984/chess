package com.example.go;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Random;

/**
 * 简单的死活训练窗口，左侧为题目列表，右侧为棋盘。
 * 点击“开始训练”或双击题目即可在棋盘上加载该题目。
 * 该实现为基础版本，满足任务单的最小功能，后续可扩展。
 */
public class GoLifeAndDeathFrame extends JFrame {
    private final ProblemRepository repository;
    private final ProgressStore progressStore;
    private final ProblemTableModel tableModel;
    private final JTable table;
    private final GoBoardPanel boardPanel;
    private final JLabel goalLabel;
    private final Random random = new Random();

    public GoLifeAndDeathFrame() {
        this.repository = new ProblemRepository();
        this.progressStore = new ProgressStore();
        this.tableModel = new ProblemTableModel(repository.getProblems(), progressStore);
        this.table = new JTable(tableModel);
        this.boardPanel = new GoBoardPanel();
        this.goalLabel = new JLabel("目标:");

        setTitle("围棋死活训练");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initLayout();
    }

    private void initLayout() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setLeftComponent(new JScrollPane(table));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(boardPanel, BorderLayout.CENTER);
        rightPanel.add(goalLabel, BorderLayout.SOUTH);
        splitPane.setRightComponent(rightPanel);

        add(splitPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        JButton startButton = new JButton("开始训练");
        JButton randomButton = new JButton("随机一题");
        JButton closeButton = new JButton("返回大厅");
        bottom.add(startButton);
        bottom.add(randomButton);
        bottom.add(closeButton);
        add(bottom, BorderLayout.SOUTH);

        // 事件
        startButton.addActionListener(e -> startSelected());
        randomButton.addActionListener(e -> startRandom());
        closeButton.addActionListener(e -> dispose());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    previewSelected();
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    startSelected();
                }
            }
        });
    }

    private void previewSelected() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            GoLifeAndDeathProblem p = tableModel.getProblemAt(row);
            boardPanel.getGame().loadPosition(p.getBoard(), p.getStartingPlayer());
            goalLabel.setText("目标: " + p.getGoal());
            boardPanel.repaint();
        }
    }

    private void startSelected() {
        previewSelected();
    }

    private void startRandom() {
        List<GoLifeAndDeathProblem> problems = repository.getProblems();
        if (problems.isEmpty()) return;
        int index = random.nextInt(problems.size());
        table.setRowSelectionInterval(index, index);
        previewSelected();
    }
}

