package com.example.go;

import javax.swing.table.AbstractTableModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * JTable 模型，用于显示死活题列表及进度信息。
 */
public class ProblemTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "ID", "Size", "Goal", "Level", "Passed", "BestMoves", "BestTime", "LastPlayed"
    };

    private final List<GoLifeAndDeathProblem> problems;
    private final ProgressStore store;

    public ProblemTableModel(List<GoLifeAndDeathProblem> problems, ProgressStore store) {
        this.problems = problems;
        this.store = store;
    }

    @Override
    public int getRowCount() {
        return problems.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        GoLifeAndDeathProblem p = problems.get(rowIndex);
        ProgressStore.Record r = store.getRecord(p.getId());
        switch (columnIndex) {
            case 0:
                return p.getId();
            case 1:
                return p.getSize();
            case 2:
                return p.getGoal();
            case 3:
                return p.getLevel();
            case 4:
                return r != null && r.passed ? "✅" : "";
            case 5:
                return r != null ? r.bestMoves : "";
            case 6:
                return r != null ? r.bestTimeMs : "";
            case 7:
                if (r != null && r.lastPlayed > 0) {
                    return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(r.lastPlayed));
                }
                return "";
            default:
                return null;
        }
    }

    public GoLifeAndDeathProblem getProblemAt(int row) {
        return problems.get(row);
    }
}

