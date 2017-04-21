package webreduce.visualize;

import webreduce.data.Dataset;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by ahmedov on 03/06/16.
 */
public class tableVisualizer {

    private JTabbedPane tabbedPane = new JTabbedPane();
    private JFrame f = new JFrame();
    int i=0;


    public tableVisualizer(){

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);

    }

    private DefaultTableModel model = new DefaultTableModel() {
        private static final long serialVersionUID = 1L;

        @Override
        public Class getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };

    public void drawTable(String[][] rows, String[] cols, Dataset ds){

        JTable table = new TableBackroundPaint0(rows,cols);

        table.setBorder(BorderFactory.createLineBorder(Color.RED,1));

        JScrollPane scrollPane = new JScrollPane(table);

        JPanel pContainer = new JPanel();
        pContainer.setLayout(new BorderLayout());
        pContainer.add(new JLabel(String.valueOf(ds.getTitle())), BorderLayout.NORTH);
        pContainer.add(scrollPane, BorderLayout.CENTER);
        final JLabel url = new JLabel(ds.getUrl());
        url.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        url.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            URI uri = new URI(url.getText());
                            desktop.browse(uri);
                        } catch (IOException ex) {
                            // do nothing
                        } catch (URISyntaxException ex) {
                            //do nothing
                        }
                    } else {
                        //do nothing
                    }
                }
            }
            public void mouseEntered(MouseEvent e){
                url.setForeground(Color.BLUE);
            }
            public void mouseExited(MouseEvent e){
                url.setForeground(Color.BLACK);
            }
        });
        pContainer.add(url, BorderLayout.SOUTH);
        tabbedPane.addTab("Tab "+i++, pContainer);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        f.setBounds(50,50,400,300);
        f.add(tabbedPane, BorderLayout.CENTER);
        f.pack();
        f.setVisible(true);
    }
}


class TableBackroundPaint0 extends JTable {

    private static final long serialVersionUID = 1L;

    TableBackroundPaint0(Object[][] data, Object[] head) {
        super(data, head);
        setOpaque(false);
        ((JComponent) getDefaultRenderer(Object.class)).setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        Color background = new Color(168, 210, 241);
        Color controlColor = new Color(230, 240, 230);
        int width = getWidth();
        int height = getHeight();
        Graphics2D g2 = (Graphics2D) g;
        Paint oldPaint = g2.getPaint();
        g2.setPaint(new GradientPaint(0, 0, background, width, 0, controlColor));
        g2.fillRect(0, 0, width, height);
        g2.setPaint(oldPaint);
        for (int row : getSelectedRows()) {
            Rectangle start = getCellRect(row, 0, true);
            Rectangle end = getCellRect(row, getColumnCount() - 1, true);
            g2.setPaint(new GradientPaint(start.x, 0, controlColor, (int) ((end.x + end.width - start.x) * 1.25), 0, Color.yellow));
            g2.fillRect(start.x, start.y, end.x + end.width - start.x, start.height);
        }
        super.paintComponent(g);
    }
}