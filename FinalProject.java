import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by William on 11/2/2017.
 */
public class FinalProject {
    private static final int WIDTH = 1475;
    private static final int HEIGHT = 1250;

    public static void main(String[] args) {
        //create frame on EDT
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createFrame();
            }
        });
    }

    public static void createFrame() {
        JFrame frame = new ImageFrame(WIDTH, HEIGHT);
        frame.setBackground(new Color(0xFF666666));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class ImageFrame extends JFrame {
    BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    private final JFileChooser chooser;
    ArrayList<JSlider> terrainSliders = new ArrayList<JSlider>();

    MapDisplayPanel dispPanel = new MapDisplayPanel(1000,1000);

    JPanel imageViewer;//holds terrainImage and buttons

    //fonts
    Font header = new Font("Arial",10,48);
    Font detail = new Font("Arial",10,16);

    //terrain options
    JTabbedPane tabbedPane;
    JTextField nameBox;
    JTextField colorBox;
    JSlider heightSlider;
    JSlider waterSlider;


    //generation options stuff
    JSlider genSlider;
    JSlider seedSlider;
    JSlider seaSlider;
    JTextField sizeBox;


    public ImageFrame(int width, int height) {
        this.setTitle("CAP 3027 2017 - Final Project - William Benda");
        this.setSize(width, height);
        chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File("."));

        addImageViewer();
        addMenu();
        addTabs();

    }

    private void addMenu() {
        ///------FILE------
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(detail);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(detail);
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        fileMenu.add(exitItem);

        JMenuItem saveItem = new JMenuItem("Save Current Image");
        saveItem.setFont(detail);
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String outputName = JOptionPane.showInputDialog("Image name:");
                File outputFile = new File(outputName + ".png");
                try {
                    javax.imageio.ImageIO.write(dispPanel.image, "png", outputFile);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(ImageFrame.this,
                            "Error saving file",
                            "oops!",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(saveItem);

        JMenuItem savetItem = new JMenuItem("Save Entire Image");
        savetItem.setFont(detail);
        savetItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String outputName = JOptionPane.showInputDialog("Image name:");
                File outputFile = new File(outputName + ".png");
                try {
                    javax.imageio.ImageIO.write(dispPanel.getImage(), "png", outputFile);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(ImageFrame.this,
                            "Error saving file",
                            "oops!",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileMenu.add(savetItem);

        JMenuBar menu = new JMenuBar();
        menu.add(fileMenu);

        this.setJMenuBar(menu);
    }

    private void addTabs() {
        this.tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Generation", generationPanel());
        tabbedPane.addTab("Terrain", terrainPanel());
        tabbedPane.setFont(detail);
        this.add(tabbedPane, BorderLayout.EAST);
    }

    private void addImageViewer() {
        imageViewer = new JPanel();
       // imagePane = new JScrollPane(new JLabel(imageHolder));
        imageViewer.add(dispPanel, BorderLayout.CENTER);
        imageViewer.add(bottomButtonPanel(), BorderLayout.SOUTH);
        this.add(imageViewer);
        this.validate();
    }

    JPanel terrainPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));

        optionsPanel.add(fontJLabel("Terrain Options",header));

        optionsPanel.add(newTerrainOptions());

        JButton addButton = new JButton("Add Terrain Type");
        addButton.setFont(detail);
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String tName = nameBox.getText();
                int c = (int) Long.parseLong(colorBox.getText().substring(2, colorBox.getText().length()), 16);
                int avgHeight = heightSlider.getValue();
                float waterPer = (float) waterSlider.getValue() / (float) waterSlider.getMaximum();

                dispPanel.generator.terrains.add(new Terrain(c, tName, avgHeight, waterPer));
                JLabel temp = new JLabel(dispPanel.generator.terrains.get(dispPanel.generator.terrains.size() - 1).getName() + " %:");
                temp.setFont(detail);
                optionsPanel.add(temp);
                JSlider slider = new JSlider(0, 100, 50);
                terrainSliders.add(slider);
                optionsPanel.add(slider);

                //update
                tabbedPane.repaint();
            }
        });
        optionsPanel.add(addButton);

        optionsPanel.add(fontJLabel("Terrains",header));

        for (int i = 0; i < dispPanel.generator.terrains.size(); i++) {
            optionsPanel.add(fontJLabel(dispPanel.generator.terrains.get(i).getName() + " %:",detail));
            JSlider slider = new JSlider(0, 100, 50);
            terrainSliders.add(slider);
            optionsPanel.add(slider);
        }

        return optionsPanel;
    }

    JPanel generationPanel() {
        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.PAGE_AXIS));

        optionsPanel.add(fontJLabel("Generation",header));
        optionsPanel.add(fontJLabel("Settings",header));

        optionsPanel.add(fontJLabel("Number of Generations [0-1000]",detail));
        genSlider = new JSlider(0, 1000, 500);
        genSlider.setToolTipText("A lower number of generations typical results in less-dense land masses");
        optionsPanel.add(genSlider);

        optionsPanel.add(fontJLabel("Number of Seeds [0-50]:",detail));
        seedSlider = new JSlider(0, 50, 25);
        seedSlider.setToolTipText("A lower number of seeds typical results in less-diverse terrains");
        optionsPanel.add(seedSlider);

        optionsPanel.add(fontJLabel("Sea Level [0-255]:",detail));
        seaSlider = new JSlider(0, 255, 100);
        seaSlider.setToolTipText("A lower sealevel will result is more land mass");
        optionsPanel.add(seaSlider);

        optionsPanel.add(fontJLabel("Max Image Size:",detail));
        sizeBox = new JTextField("1000",10);
        sizeBox.setMaximumSize(sizeBox.getPreferredSize());
        optionsPanel.add(sizeBox);

        return optionsPanel;

    }

    JPanel bottomButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));

        JButton generateItem = new JButton("Generate new map");
        generateItem.setFont(detail);
        generateItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ArrayList<Float> percents = new ArrayList<Float>();
                        for (int i = 0; i < terrainSliders.size(); i++) {
                            percents.add((float) terrainSliders.get(i).getValue() / terrainSliders.get(i).getMaximum());
                        }
                        dispPanel.generator.makeImage(percents, seedSlider.getValue(), genSlider.getValue(), Integer.parseInt(sizeBox.getText()), seaSlider.getValue());
                        dispPanel.setFinishedImage();
                    }
                }).start();

            }
        });
        buttonPanel.add(generateItem);

        JButton showDItem = new JButton("Show detailed map");
        showDItem.setFont(detail);
        showDItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dispPanel.setFinishedImage();
                    }
                }).start();

            }
        });
        buttonPanel.add(showDItem);

        JButton showItem = new JButton("Show plain map");
        showItem.setFont(detail);
        showItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dispPanel.setTerrainImage();
                    }
                }).start();

            }
        });
        buttonPanel.add(showItem);

        JButton heightItem = new JButton("Show height map");
        heightItem.setFont(detail);
        heightItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                       dispPanel.setHeightImage();
                    }
                }).start();

            }
        });
        buttonPanel.add(heightItem);

        JButton resetItem = new JButton("Reset View");
        resetItem.setFont(detail);
        resetItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        dispPanel.resetView();
                    }
                }).start();

            }
        });
        buttonPanel.add(resetItem);

        return buttonPanel;
    }

    JPanel newTerrainOptions(){
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        panel.add(fontJLabel("Name:",detail));
        nameBox = new JTextField("---",10);
        nameBox.setMaximumSize(nameBox.getPreferredSize());
        panel.add(nameBox);

        panel.add(fontJLabel("Color:",detail));
        colorBox = new JTextField("0xFFFFFFFF",10);
        colorBox.setMaximumSize(colorBox.getPreferredSize());
        panel.add(colorBox);

        panel.add(fontJLabel("Height [0-255]:",detail));
        heightSlider = new JSlider(0, 255, 125);
        panel.add(heightSlider);

        panel.validate();
        return panel;
    }


    JLabel fontJLabel(String s, Font font){
        JLabel l = new JLabel(s);
        l.setFont(font);
        return l;
    }

}