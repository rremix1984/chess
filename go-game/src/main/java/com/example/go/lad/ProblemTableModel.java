package com.example.go.lad;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Table model displaying problems and progress.
 */
public class ProblemTableModel extends AbstractTableModel {
    private final String[] cols = {"题号","路数","目标","完成","最佳步数","最佳用时"};
    private final List<GoLifeAndDeathProblem> data = new ArrayList<>();
    private final Map<String, ProblemProgress> progress = new HashMap<>();

    public void setData(List<GoLifeAndDeathProblem> list, ProgressStore store) {
        data.clear();
        data.addAll(list);
        progress.clear();
        for (GoLifeAndDeathProblem p : list) {
            progress.put(p.id, store.get(p.id));
        }
        fireTableDataChanged();
    }

    public String getIdAt(int row) { return data.get(row).id; }

    public String getNextId(String id) {
        for (int i=0; i<data.size(); i++) {
            if (data.get(i).id.equals(id)) {
                return (i+1 < data.size()) ? data.get(i+1).id : null;
            }
        }
        return null;
    }

    @Override public int getRowCount() { return data.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        GoLifeAndDeathProblem p = data.get(r);
        ProblemProgress pr = progress.getOrDefault(p.id, new ProblemProgress(p.id));
        switch (c) {
            case 0: return p.id;
            case 1: return p.size;
            case 2: return p.goal;
            case 3: return pr.passed ? "✅" : "";
            case 4: return pr.bestMoves > 0 ? pr.bestMoves : "";
            case 5: return pr.bestTimeMs > 0 ? (pr.bestTimeMs/1000 + "s") : "";
            default: return "";
        }
    }
}
