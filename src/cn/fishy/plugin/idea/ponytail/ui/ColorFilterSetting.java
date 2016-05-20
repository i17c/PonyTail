package cn.fishy.plugin.idea.ponytail.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import cn.fishy.plugin.idea.ponytail.Console;
import cn.fishy.plugin.idea.ponytail.colors.ColorFilter;
import cn.fishy.plugin.idea.ponytail.persistence.ColorFilterHolder;

public class ColorFilterSetting extends JDialog {

    private static final long serialVersionUID = -6350095993951244600L;
    private final List<ColorFilter> clonedData;
    private final TableModel tableModel;

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton addButton;
    private JButton removeButton;
    private JTable TABLE_filters;
    private Console console;
    private Project project;


    public ColorFilterSetting() {
        setContentPane(contentPane);
        setModal(true);
        setTitle("PonyTail Color Filter Settings");
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onAdd();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRemove();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        clonedData = ColorFilterHolder.getInstance().clone();
        tableModel = new TableModel();

        TABLE_filters.setModel(tableModel);
        TABLE_filters.getColumnModel().getColumn(COLUMN_BG).setCellRenderer(new ColorCellRenderer());
        TABLE_filters.getColumnModel().getColumn(COLUMN_FG).setCellRenderer(new ColorCellRenderer());

        TABLE_filters.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                int column = e.getColumn();
                if (column == COLUMN_NAME || column == COLUMN_REGEXP || column == COLUMN_ENABLED) {
                    int row = e.getFirstRow();
                    switch (column) {
                        case COLUMN_NAME:
                            String name = String.valueOf(tableModel.getVectorValue(row, column));
                            clonedData.get(row).setName(name);
                            break;
                        case COLUMN_REGEXP:
                            String reValue = String.valueOf(tableModel.getVectorValue(row, column));
                            clonedData.get(row).setRegularExpression(reValue);
                            break;
                        case COLUMN_ENABLED:
                            Object enabled = tableModel.getVectorValue(row, column);
                            clonedData.get(row).setEnabled((Boolean) enabled);
                    }
                }
            }
        });

        TABLE_filters.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();

                    switch (column) {
                        case COLUMN_BG:
                            ColorFilter lBg = clonedData.get(row);
                            Color lNewBgColor = JColorChooser.showDialog(null, "Select color", lBg.getBg());
                            lBg.setBg(lNewBgColor);
                            refreshTable();
                            break;
                        case COLUMN_FG:
                            ColorFilter lFg = clonedData.get(row);
                            Color lNewFgColor = JColorChooser.showDialog(null, "Select color", lFg.getFg());
                            lFg.setFg(lNewFgColor);
                            refreshTable();
                    }
                }
            }
        });
    }

    private static final int COLUMN_NAME = 0;
    private static final int COLUMN_REGEXP = 1;
    private static final int COLUMN_ENABLED = 2;
    private static final int COLUMN_BG = 3;
    private static final int COLUMN_FG = 4;
    private static final String[] columns = {"Name", "RegExp", "Enabled", "Background", "Foreground"};

    private class TableModel extends DefaultTableModel {

        public TableModel() {
            super(clonedData.size(), columns.length);
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == COLUMN_ENABLED) {
                return Boolean.class;
            }
            return super.getColumnClass(columnIndex);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            switch (column) {
                case COLUMN_BG:
                case COLUMN_FG:
                    return false;
            }
            return super.isCellEditable(row, column);
        }

        public Object getVectorValue(int row, int column) {
            Vector rowVector = (Vector) dataVector.elementAt(row);
            return rowVector.elementAt(column);
        }

        @Override
        public Object getValueAt(int row, int column) {
            ColorFilter lEntry = clonedData.get(row);
            if (lEntry != null) {
                switch (column) {
                    case COLUMN_NAME:
                        return lEntry.getName();
                    case COLUMN_REGEXP:
                        return lEntry.getRegularExpression();
                    case COLUMN_ENABLED:
                        return lEntry.isEnabled();
                }
            }
            return "";
        }
    }

    private class ColorCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component lCell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            Object lValue = null;
            ColorFilter lEntry = clonedData.get(row);
            if (lEntry != null) {
                switch (column) {
                    case COLUMN_BG:
                        lValue = lEntry.getBg();
                        break;
                    case COLUMN_FG:
                        lValue = lEntry.getFg();
                }
            }
            if (lValue instanceof Color) {
                lCell.setBackground((Color) lValue);
            }
            return lCell;
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(clonedData.size());
        tableModel.fireTableDataChanged();
    }

    private void onAdd() {
        clonedData.add(new ColorFilter("New Filter", true, ""));
        refreshTable();
    }

    private void onRemove() {
        int[] lSelectedRow = TABLE_filters.getSelectedRows();
        for (int lRow : lSelectedRow) {
            if (lRow >= 0) {
                clonedData.set(lRow, null);
            }
        }

        Iterator<ColorFilter> litr = clonedData.iterator();
        while (litr.hasNext()) {
            if (litr.next() == null) {
                litr.remove();
            }
        }

        refreshTable();
    }

    private void onOK() {
        ColorFilterHolder.getInstance().setFilters(clonedData);
        if (console != null) {
            console.refresh(null);
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static ColorFilterSetting getInstance() {
        try {
            return ServiceManager.getService(ColorFilterSetting.class);
        } catch (Exception e) {
            return new ColorFilterSetting();
        }
    }

    public void setConsole(Console console) {
        this.console = console;
    }

    public Console getConsole() {
        return console;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public static void main(String[] args) {
        ColorFilterSetting dialog = new ColorFilterSetting();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public static void pop(Console console, Project project) {
        ColorFilterSetting colorFilterSetting = ColorFilterSetting.getInstance();
        if (colorFilterSetting == null) {
            colorFilterSetting = new ColorFilterSetting();
        }
        colorFilterSetting.pack();
        Rectangle
                screenBounds =
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration()
                        .getBounds();
        colorFilterSetting.setLocation(screenBounds.x + (screenBounds.width - colorFilterSetting.getWidth()) / 2,
                                       screenBounds.y + (screenBounds.height - colorFilterSetting.getHeight()) / 2);
        colorFilterSetting.setConsole(console);
        colorFilterSetting.setProject(project);
        colorFilterSetting.setVisible(true);
    }
}
