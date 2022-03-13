package ru.crystals.pos.customerdisplay.ksdp01;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.net.URL;

public class CDEmulatorView extends JFrame {

    private static final int WIDTH = 430;

    private JPanel viewPanel = new JPanel() {
        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            if (image != null) {
                g2d.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
            } else {
                super.paint(g2d);
            }
        }
    };

    private Image image = null;

    private volatile int draggedAtX, draggedAtY;

    public CDEmulatorView() {
        this.setTitle("Customer display emulator");
        TemplateProcessing.setCdImageListener(new CDImageListener() {
            private WhiteToGreenSwapFilter filter;

            @Override
            public void draw(BufferedImage image) {
                filter = new WhiteToGreenSwapFilter();
                CDEmulatorView.this.image = createImage(new FilteredImageSource(image.getSource(), filter));
                SwingUtilities.invokeLater(() -> viewPanel.repaint());
            }
        });
        this.setPreferredSize(new Dimension(WIDTH, 211));
        try {
            URL imageUrl = CDEmulatorView.class.getResource("/vikivision.png");
            if (imageUrl != null) {
                BufferedImage background = ImageIO.read(imageUrl);
                this.setContentPane(new JLabel(new ImageIcon(background)));
            }
            this.setUndecorated(true);
            this.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
        } catch (Exception e) {
            e.printStackTrace();
        }
        viewPanel.setBackground(Color.BLACK);
        this.add(viewPanel);
        viewPanel.setBounds(88, 112, 256, 64);
        this.pack();
        this.setLocation(1260, 0);
        this.setVisible(true);


        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                draggedAtX = e.getX();
                draggedAtY = e.getY();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                CDEmulatorView.this.setLocation(e.getX() - draggedAtX + getLocation().x,
                        e.getY() - draggedAtY + getLocation().y);
            }
        });

        for (Window w : Window.getWindows()) {
            if (w.isShowing()) {
                int x = new Double(w.getLocation().getX() + (w.getWidth() - WIDTH) / 2).intValue();
                int y = new Double(w.getLocation().getY() + w.getHeight() + 20).intValue();
                this.setLocation(x, y);
                break;
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CDEmulatorView().setVisible(true));
    }

    class WhiteToGreenSwapFilter extends java.awt.image.RGBImageFilter {

        public WhiteToGreenSwapFilter() {
            canFilterIndexColorModel = true;
        }

        public int filterRGB(int x, int y, int rgb) {
            if (rgb == Color.WHITE.getRGB()) {
                return new Color(110, 255, 15).getRGB();
            }
            return rgb;
        }
    }

}
