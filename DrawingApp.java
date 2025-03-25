import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;
import javax.imageio.ImageIO;

public class DrawingApp extends JFrame {

    private int prevX, prevY;
    private DrawingPanel panel;
    private Color selectedColor = Color.BLACK;
    private int brushSize = 5; // Default brush size
    private boolean isEraserMode = false; // Flag for eraser mode
    private JLabel colorLabel; // Declare colorLabel as a class member

    public DrawingApp() {
        setTitle("DigiCanvas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent default close operation
        setLocationRelativeTo(null);
        setResizable(false); // Make the frame non-resizable
        setLayout(new CardLayout());

        // Add window listener to handle the close button
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showExitConfirmationDialog();
            }
        });

        // Create the homepage panel
        JPanel homePanel = createHomePanel();

        // Create the drawing panel
        JPanel drawingPanel = createDrawingPanel();

        // Add both panels to the frame
        add(homePanel, "Home");
        add(drawingPanel, "Drawing");

        setVisible(true);
    }

    private void showExitConfirmationDialog() {
        int option = JOptionPane.showOptionDialog(this,
                "Do you want to exit the program?",
                "Exit Confirmation",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Yes", "Save First", "No"},
                "Save First");

        if (option == JOptionPane.YES_OPTION) {
            System.exit(0);
        } else if (option == JOptionPane.NO_OPTION) {
            panel.save();
            System.exit(0);
        }
        // Do nothing if "No" is selected
    }

    private JPanel createHomePanel() {
        JPanel homePanel = new JPanel();
        homePanel.setLayout(new BoxLayout(homePanel, BoxLayout.Y_AXIS));
        homePanel.setBackground(new Color(230, 230, 250)); // Set the background color to a soft violet pastel

        // Title label
        JLabel titleLabel = new JLabel("DigiCanvas");
        titleLabel.setFont(new Font("Brush Script MT", Font.BOLD, 120));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Start Drawing button
        JButton startButton = new JButton("Start Drawing");
        startButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 20));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) getContentPane().getLayout();
                cl.show(getContentPane(), "Drawing");
            }
        });

        // Exit Program button
        JButton exitButton = new JButton("Exit Program");
        exitButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 20));
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showExitConfirmationDialog();
            }
        });

        // Add components to the home panel
        homePanel.add(Box.createVerticalGlue()); // Add vertical glue to center the components
        homePanel.add(titleLabel);
        homePanel.add(Box.createVerticalStrut(20)); // Add vertical spacing
        homePanel.add(startButton);
        homePanel.add(Box.createVerticalStrut(10)); // Add vertical spacing
        homePanel.add(exitButton);
        homePanel.add(Box.createVerticalGlue()); // Add vertical glue to center the components

        return homePanel;
    }

    private JPanel createDrawingPanel() {
        JPanel drawingPanel = new JPanel(new BorderLayout());

        // Clear button
        JButton clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.clear();
            }
        });

        // Undo button
        JButton undoButton = new JButton("Undo");
        undoButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        undoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.undo();
            }
        });

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panel.save();
            }
        });

        // Color selection button
        JButton colorButton = new JButton("Choose Color");
        colorButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = showSwatchOnlyColorChooser();
                if (newColor != null) {
                    selectedColor = newColor;
                    panel.setDrawingColor(newColor);
                    colorLabel.setBackground(newColor); // Update the color label background
                    isEraserMode = false; // Disable eraser mode when a new color is selected
                }
            }
        });

        // Label to show the current color
        colorLabel = new JLabel();
        colorLabel.setOpaque(true);
        colorLabel.setBackground(selectedColor);
        colorLabel.setPreferredSize(new Dimension(50, 25));
        colorLabel.setBorder(new LineBorder(Color.BLACK));

        // Brush size slider with label
        JLabel brushSizeLabel = new JLabel("Brush Size:");
        JSlider brushSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, brushSize);
        brushSizeSlider.setMajorTickSpacing(10);
        brushSizeSlider.setMinorTickSpacing(1);
        brushSizeSlider.setPaintTicks(true);
        brushSizeSlider.setPaintLabels(true);
        brushSizeSlider.addChangeListener(e -> {
            brushSize = brushSizeSlider.getValue();
            panel.setBrushSize(brushSize);
        });

        // Create a panel to hold the brush size label and slider
        JPanel brushSizePanel = new JPanel();
        brushSizePanel.setBackground(new Color(230, 230, 250)); // Set the background color to a soft violet pastel
        brushSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        brushSizePanel.add(brushSizeLabel);
        brushSizePanel.add(brushSizeSlider);

        // Panel for the current color label
        JPanel colorPanel = new JPanel();
        colorPanel.setBackground(new Color(230, 230, 250)); // Set the background color to a soft violet pastel
        colorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        colorPanel.add(new JLabel("Current Color:"));
        colorPanel.add(colorLabel);

        // Erase button
        JButton eraseButton = new JButton("Erase");
        eraseButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        eraseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isEraserMode = true; // Enable eraser mode
            }
        });

        // Draw button
        JButton drawButton = new JButton("Draw");
        drawButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        drawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isEraserMode = false; // Disable eraser mode
            }
        });

        // Grid button
        JButton gridButton = new JButton("Toggle Grid");
        gridButton.setFont(new Font("Tw Cen MT", Font.PLAIN, 16));
        gridButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showGridOptionsDialog();
            }
        });

        // Panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(230, 230, 250)); // Set the background color to a soft violet pastel
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(colorButton);
        buttonPanel.add(eraseButton);
        buttonPanel.add(drawButton);
        buttonPanel.add(gridButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(clearButton);

        // Drawing panel
        panel = new DrawingPanel();
        panel.setBorder(new LineBorder(Color.BLACK, 2)); // Add a border to the canvas

        // Add the color panel, brush size panel, and button panel to the frame
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(colorPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        drawingPanel.add(topPanel, BorderLayout.NORTH);
        drawingPanel.add(brushSizePanel, BorderLayout.SOUTH);
        drawingPanel.add(panel, BorderLayout.CENTER);

        return drawingPanel;
    }

    private void showGridOptionsDialog() {
        int option = JOptionPane.showOptionDialog(this,
                "Choose an option:",
                "Grid Options",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"Grid Sizes", "No Grid", "Cancel"},
                "Grid Sizes");

        if (option == JOptionPane.YES_OPTION) {
            showGridSizeDialog();
        } else if (option == JOptionPane.NO_OPTION) {
            panel.setGridSize(0);
            panel.setShowGrid(false);
        }
        // Do nothing if "Cancel" is selected
    }

    private void showGridSizeDialog() {
        String[] gridSizes = {"20", "30", "40", "50"};
        String selectedGridSize = (String) JOptionPane.showInputDialog(this,
                "Select Grid Size:",
                "Grid Size",
                JOptionPane.PLAIN_MESSAGE,
                null,
                gridSizes,
                gridSizes[0]);

        if (selectedGridSize != null) {
            int gridSize = Integer.parseInt(selectedGridSize);
            panel.setGridSize(gridSize);
            panel.setShowGrid(true);
        }
    }

    private class DrawingPanel extends JPanel {

        private BufferedImage image;
        private Graphics2D g2;
        private Stack<BufferedImage> undoStack = new Stack<>();
        private boolean showGrid = false;
        private int gridSize = 20;

        public DrawingPanel() {
            setDoubleBuffered(false);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    saveStateToUndoStack();
                    prevX = e.getX();
                    prevY = e.getY();
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int x = e.getX();
                    int y = e.getY();

                    if (g2 != null) {
                        g2.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        if (isEraserMode) {
                            g2.setColor(Color.WHITE); // Use white color for erasing
                        } else {
                            g2.setColor(selectedColor); // Use selected color for drawing
                        }
                        g2.drawLine(prevX, prevY, x, y);
                        repaint();
                        prevX = x;
                        prevY = y;
                    }
                }
            });

            clear(); // Initialize the canvas
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                g2 = image.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                clear(); // Clear the canvas to white
            }
            g.drawImage(image, 0, 0, this);

            if (showGrid) {
                drawGrid(g);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(800, 600); // Fixed size for the panel
        }

        public void clear() {
            if (g2 != null) {
                saveStateToUndoStack();
                g2.setColor(Color.WHITE);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(selectedColor); // Setting the drawing color to the current selected color
                repaint();
            }
        }

        public void undo() {
            // Ensure the undo stack is not empty before undoing
            if (!undoStack.isEmpty()) {
                image = undoStack.pop();
                g2 = (Graphics2D) image.getGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(selectedColor); // Retain the current drawing color
                repaint();
            } else {
                clear(); // If the stack is empty, just clear the canvas to white
            }
        }

        public void save() {
            if (image != null) {
                try {
                    // Have the user choose for a file location and name
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save Drawing");
                    int userSelection = fileChooser.showSaveDialog(DrawingApp.this);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();
                        if (!fileToSave.getName().endsWith(".png")) {
                            fileToSave = new File(fileToSave.getAbsolutePath() + ".png");
                        }

                        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D bGr = bufferedImage.createGraphics();
                        bGr.drawImage(image, 0, 0, null);
                        bGr.dispose();
                        ImageIO.write(bufferedImage, "png", fileToSave);
                        JOptionPane.showMessageDialog(DrawingApp.this, "Drawing saved successfully.");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DrawingApp.this, "Error saving drawing: " + ex.getMessage());
                }
            }
        }

        public void setDrawingColor(Color color) {
            if (g2 != null) {
                g2.setColor(color);
            }
        }

        public void setBrushSize(int size) {
            brushSize = size;
        }

        private void saveStateToUndoStack() {
            BufferedImage copy = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = copy.createGraphics();
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, copy.getWidth(), copy.getHeight());
            g2d.drawImage(image, 0, 0, null);
            g2d.dispose();
            undoStack.push(copy);
        }

        public void toggleGrid() {
            showGrid = !showGrid;
            repaint();
        }

        public void setShowGrid(boolean show) {
            showGrid = show;
            repaint();
        }

        public void setGridSize(int size) {
            gridSize = size;
            repaint();
        }

        private void drawGrid(Graphics g) {
            if (gridSize > 0) {
                g.setColor(Color.LIGHT_GRAY);

                for (int i = 0; i < getWidth(); i += gridSize) {
                    g.drawLine(i, 0, i, getHeight());
                }

                for (int i = 0; i < getHeight(); i += gridSize) {
                    g.drawLine(0, i, getWidth(), i);
                }
            }
        }
    }

    // Show a color chooser with only the swatches tab
    private Color showSwatchOnlyColorChooser() {
        JColorChooser colorChooser = new JColorChooser();
        AbstractColorChooserPanel[] panels = colorChooser.getChooserPanels();
        for (AbstractColorChooserPanel panel : panels) {
            if (!panel.getDisplayName().equals("Swatches")) {
                colorChooser.removeChooserPanel(panel);
            }
        }
        return JColorChooser.showDialog(DrawingApp.this, "Choose a color", selectedColor);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DrawingApp());
    }
}