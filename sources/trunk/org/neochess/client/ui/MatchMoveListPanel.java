
package org.neochess.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.neochess.client.ui.MatchFrame.MatchFrameListener;
import org.neochess.engine.Board.Move;
import org.neochess.general.Disposable;
import org.neochess.util.UserInterfaceUtils;

public class MatchMoveListPanel extends JPanel implements Disposable, MatchFrameListener
{
    private MatchFrame matchFrame;
    private JTable moveListTable = new JTable();
    private JScrollBar moveListScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
    private DefaultTableModel moveListTableModel = new DefaultTableModel() { public boolean isCellEditable(int row, int column) { return false; }};

    public MatchMoveListPanel(MatchFrame matchFrame)
    {
        this.matchFrame = matchFrame;
        this.matchFrame.addMatchFrameListener(this);
        moveListTable.setModel(moveListTableModel);
        moveListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        moveListTable.setColumnSelectionAllowed(false);
        moveListTable.setRowSelectionAllowed(false);
        moveListTable.setDragEnabled(false);
        moveListTable.getTableHeader().setReorderingAllowed(false);
        moveListTable.setDefaultRenderer(Object.class, new AttributiveCellRenderer());
        moveListTable.getSelectionModel().setSelectionMode(moveListTable.getSelectionModel().SINGLE_SELECTION);
        moveListTable.setRowHeight(13);
        moveListTableModel.addColumn("#");
        moveListTableModel.addColumn("White");
        moveListTableModel.addColumn("Black");
        moveListTable.getColumnModel().getColumn(0).setPreferredWidth(15);
        moveListTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        moveListTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        moveListTable.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = moveListTable.rowAtPoint(e.getPoint());
                int column = moveListTable.columnAtPoint(e.getPoint());
                if (column > 0)
                {    
                    int ply = (row * 2) + column;
                    MatchMoveListPanel.this.matchFrame.setDisplayPly(ply);
                }
            }   
        });
        JScrollPane scrollPane = new JScrollPane(moveListTable);
        scrollPane.setWheelScrollingEnabled(true);
        moveListScrollBar.addAdjustmentListener(new AdjustmentListener() 
        {
            public void adjustmentValueChanged(AdjustmentEvent e)
            {
                MatchMoveListPanel.this.matchFrame.setDisplayPly(moveListScrollBar.getValue());
            }
        });
        updateScrollBarValues();
        setLayout(new BorderLayout(5, 5));
        add(scrollPane, BorderLayout.CENTER);
        add(moveListScrollBar, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }

    @Override
    public void dispose()
    {
        moveListTableModel = null;
        while (moveListTable.getMouseListeners().length > 0)
            moveListTable.removeMouseListener(moveListTable.getMouseListeners()[0]);
        moveListTable = null;
        while (moveListScrollBar.getAdjustmentListeners().length > 0)
            moveListScrollBar.removeAdjustmentListener(moveListScrollBar.getAdjustmentListeners()[0]);
        moveListScrollBar = null;
        this.matchFrame.removeMatchFrameListener(this);
        this.matchFrame = null;
        removeAll();
    }

    public void clearMoveList()
    {
        while (moveListTableModel.getRowCount() > 0)
            moveListTableModel.removeRow(0);
        updateScrollBarValues();
    }

    public void addMove(MatchFrame match, Move move)
    {
        String moveString = move.getSANString(match.getBoard(match.getPly()-1));
        if ((match.getPly()-1) % 2 == 0)
        {
            moveListTableModel.addRow(new String[] { String.valueOf(moveListTableModel.getRowCount() + 1), moveString, "" });
        } 
        else
        {
            moveListTableModel.setValueAt(moveString, moveListTableModel.getRowCount() - 1, 2);
        }
        moveListTable.scrollRectToVisible(moveListTable.getCellRect(moveListTableModel.getRowCount() - 1, 0, true));
        updateScrollBarValues();
    }

    public void removeMove(MatchFrame match)
    {
        if (match.getPly() % 2 == 0)
        {
            moveListTableModel.removeRow(moveListTableModel.getRowCount() - 1);
        } 
        else
        {
            moveListTableModel.setValueAt("", moveListTableModel.getRowCount() - 1, 2);
        }
        moveListTable.scrollRectToVisible(moveListTable.getCellRect(moveListTableModel.getRowCount() - 1, 0, true));
        updateScrollBarValues();
    }

    public void onMatchMove(MatchFrame match, Move move)
    {
        addMove(match, move);   
    }
    
    public void onMatchTakeback(MatchFrame match, Move move)
    {
        removeMove(match);
    }
    
    public void onMatchFinished(MatchFrame match){}
    public void onMatchPositionChanged(MatchFrame match){}
    public void onMatchStarted(MatchFrame match){}
    public void onMatchStateChanged(MatchFrame match, byte state){}
    public void onMatchTurnStarted (MatchFrame match, byte side){}
    public void onMatchTurnEnded (MatchFrame match, byte side){}
    public void onMatchBoardFlipped (MatchFrame match, boolean flipped) {}
    
    public void onMatchDisplayPlyChanged (MatchFrame match, int ply)
    {
        update();
    }
    
    public void update ()
    {
        moveListTable.repaint();
        updateScrollBarValues();
    }
    
    private void updateScrollBarValues()
    {
        moveListScrollBar.setMinimum(0);
        moveListScrollBar.setMaximum(matchFrame.getPly()+1);
        moveListScrollBar.setVisibleAmount(1);
        moveListScrollBar.setValue(Math.max(matchFrame.getDisplayPly(),0));
        repaint();
    }

    public class AttributiveCellRenderer implements TableCellRenderer
    {
        public AttributiveCellRenderer()
        {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            JLabel caption = new JLabel();
            caption.setHorizontalAlignment(JLabel.CENTER);
            caption.setFont(new java.awt.Font("Arial", 0, 11));
            caption.setText((String) value);
            caption.setOpaque(true);
            if (column == 0)
            {
                caption.setBackground(UserInterfaceUtils.getColor("TableHeader.background"));
            } 
            else
            {
                int ply = (row * 2) + column;
                if (ply == MatchMoveListPanel.this.matchFrame.getDisplayPly() || isSelected)
                {
                    caption.setBackground(UserInterfaceUtils.getColor("Table.selectionBackground"));
                    caption.setForeground(UserInterfaceUtils.getColor("Table.selectionForeground"));
                    caption.setBorder(javax.swing.BorderFactory.createLineBorder(UserInterfaceUtils.getColor("TableHeader.background")));
                } 
                else
                {
                    caption.setBackground(UserInterfaceUtils.getColor("Table.background"));
                    caption.setForeground(UserInterfaceUtils.getColor("Table.foreground"));
                }
            }
            return caption;
        }
    }
}
