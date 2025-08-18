package com.example.go.lad;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main window for life-and-death training mode.
 */
public class LifeAndDeathFrame extends JFrame {

    private JTable problemTable;
    private ProblemTableModel problemTableModel;
    private GoBoardPanel boardPanel;
    private JLabel lblProblemId, lblGoal, lblToPlay, lblStatus;
    private JTextArea hintArea;
    private JButton btnUnlockHint, btnSubmit, btnNext, btnReset, btnBack;

    private transient ProblemRepository problemRepo = new JsonProblemRepository();
    private transient ProgressStore progressStore = new FileProgressStore();
    private transient LifeAndDeathService ladService = new RuleBasedLifeAndDeathService();

    private GoLifeAndDeathProblem current;
    private long startTimeMs;

    public LifeAndDeathFrame() {
        setTitle("围棋 · 死活训练");
        setSize(1100, 720);
        setLocationRelativeTo(null);

        buildUI();
        loadProblems();
    }

    private void buildUI() {
        problemTableModel = new ProblemTableModel();
        problemTable = new JTable(problemTableModel);
        problemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        problemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = problemTable.getSelectedRow();
                if (row >= 0) onSelectProblem(problemTableModel.getIdAt(row));
            }
        });

        boardPanel = new GoBoardPanel();
        boardPanel.setMoveListener(pt -> {
            if (current != null) {
                boardPanel.place(pt);
                lblStatus.setText("已落子：" + pt);
            }
        });

        JPanel right = buildSidebar();

        JSplitPane leftMiddle = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(problemTable), boardPanel);
        leftMiddle.setResizeWeight(0.28);

        JSplitPane root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftMiddle, right);
        root.setResizeWeight(0.8);

        getContentPane().add(root, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        lblProblemId = new JLabel("-");
        lblGoal = new JLabel("-");
        lblToPlay = new JLabel("-");
        lblStatus = new JLabel("请选择关卡…");

        hintArea = new JTextArea(6, 18);
        hintArea.setEditable(false);
        hintArea.setLineWrap(true);
        hintArea.setWrapStyleWord(true);

        btnUnlockHint = new JButton("解锁提示");
        btnUnlockHint.addActionListener(e -> onUnlockHint());

        btnSubmit = new JButton("提交");
        btnSubmit.addActionListener(e -> onSubmit());

        btnNext = new JButton("下一题");
        btnNext.setEnabled(false);
        btnNext.addActionListener(e -> onNextProblem());

        btnReset = new JButton("重来");
        btnReset.addActionListener(e -> onReset());

        btnBack = new JButton("返回大厅");
        btnBack.addActionListener(e -> dispose());

        JPanel info = new JPanel(new GridLayout(0,1,4,4));
        info.add(new JLabel("题号：")); info.add(lblProblemId);
        info.add(new JLabel("目标：")); info.add(lblGoal);
        info.add(new JLabel("手番：")); info.add(lblToPlay);

        JPanel btns = new JPanel(new GridLayout(0,1,6,6));
        btns.add(btnUnlockHint);
        btns.add(btnSubmit);
        btns.add(btnNext);
        btns.add(btnReset);
        btns.add(btnBack);

        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        p.add(info, BorderLayout.NORTH);
        p.add(new JScrollPane(hintArea), BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        p.add(lblStatus, BorderLayout.PAGE_END);
        return p;
    }

    private void loadProblems() {
        List<GoLifeAndDeathProblem> list = problemRepo.list(new ProblemFilter());
        problemTableModel.setData(list, progressStore);
        if (!list.isEmpty()) {
            problemTable.setRowSelectionInterval(0,0);
            onSelectProblem(list.get(0).id);
        }
    }

    private void onSelectProblem(String id) {
        this.current = problemRepo.get(id);
        ladService.load(current, boardPanel.getGame());
        boardPanel.showPosition(current);
        startTimeMs = System.currentTimeMillis();

        lblProblemId.setText(current.id);
        lblGoal.setText(current.goal);
        lblToPlay.setText(current.toPlay.name());
        hintArea.setText("");
        btnNext.setEnabled(false);
        lblStatus.setText("已载入：" + current.id);
    }

    private void onUnlockHint() {
        String next = ladService.nextHint(current, hintArea.getText());
        if (next == null) {
            JOptionPane.showMessageDialog(this, "没有更多提示了。");
        } else {
            hintArea.append((hintArea.getText().isEmpty()?"":"\n") + "• " + next);
        }
    }

    private void onSubmit() {
        JudgeResult jr = ladService.judge(boardPanel.getGame(), current);
        if (jr.status == JudgeStatus.PASS) {
            btnNext.setEnabled(true);
            long t = System.currentTimeMillis() - startTimeMs;
            progressStore.markPassed(current.id, jr.movesUsed, t, countHints());
            lblStatus.setText("正解 ✅ " + jr.reason);
            JOptionPane.showMessageDialog(this, "正解 ✅\n" + jr.reason, "通过", JOptionPane.INFORMATION_MESSAGE);
        } else if (jr.status == JudgeStatus.FAIL) {
            lblStatus.setText("未解 ❌ " + jr.reason);
            JOptionPane.showMessageDialog(this, "未解 ❌\n" + jr.reason, "再试一次", JOptionPane.WARNING_MESSAGE);
        } else {
            lblStatus.setText("暂不确定，继续尝试或解锁提示。");
        }
    }

    private int countHints() {
        String s = hintArea.getText();
        return (int) s.lines().filter(l -> l.trim().startsWith("•")).count();
    }

    private void onNextProblem() {
        String nextId = problemTableModel.getNextId(current.id);
        if (nextId != null) {
            onSelectProblem(nextId);
        } else {
            JOptionPane.showMessageDialog(this, "已是最后一题。");
        }
    }

    private void onReset() {
        if (current != null) {
            ladService.load(current, boardPanel.getGame());
            boardPanel.showPosition(current);
            startTimeMs = System.currentTimeMillis();
            lblStatus.setText("已重置。");
        }
    }
}
