package eu.yozozchomutova;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.*;

public class Main extends JPanel {

    private final JFrame frame;

    private File currentFile;

    public ArrayList<Sprite> sprites = new ArrayList<>();

    private final JButton importBtn;
    private JButton exportAllBtn;
    private JButton exportSelectedBtn;
    private final JButton previousFrameBtn;
    private final JButton nextFrameBtn;
    private final JButton replaceSpriteBtn;
    private final JButton exportAsTspBtn;
    private final JButton exportAsSpriteDataBtn;

    private final JLabel spriteFramePos;

    private final DefaultListModel<String> spritesDataModel = new DefaultListModel<>();
    private final JList<String> allSprites;

    private final float bcgColor = 0;

    private int imagePreviewID = 0;
    private int imageFrameID = 0;

    Main() {
        frame = new JFrame("State of War - Sprites extractor");
        frame.setSize(1500, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);

        setBackground(Color.WHITE);
        setLayout(null);

        //UI
        importBtn = new JButton("Load file");
        importBtn.setBounds(10, 10, 120, 40);
        importBtn.setForeground(Color.WHITE);
        importBtn.setBackground(Color.BLUE);
        add(importBtn);

        importBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                currentFile = getFile(Main.this);

                if (currentFile != null) {
                    imageFrameID = 0;
                    prepareImage(currentFile, new File(currentFile.getParentFile() + "/sprites.info"));
                }
            }
        });

        previousFrameBtn = new JButton("< frame");
        previousFrameBtn.setBounds(140, 10, 100, 40);
        previousFrameBtn.setForeground(Color.WHITE);
        previousFrameBtn.setBackground(Color.GREEN);
        add(previousFrameBtn);

        previousFrameBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (imageFrameID != 0)
                    imageFrameID--;
                else
                    imageFrameID = sprites.get(imagePreviewID).frames.size()-1;

                updateSpriteFramePosLabel();
            }
        });

        nextFrameBtn = new JButton("frame >");
        nextFrameBtn.setBounds(250, 10, 100, 40);
        nextFrameBtn.setForeground(Color.WHITE);
        nextFrameBtn.setBackground(Color.GREEN);
        add(nextFrameBtn);

        nextFrameBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (imageFrameID != sprites.get(imagePreviewID).frames.size()-1)
                    imageFrameID++;
                else
                    imageFrameID = 0;

                updateSpriteFramePosLabel();
            }
        });

        allSprites = new JList<>();
        allSprites.setBounds(10, 60, 230, 900);
        allSprites.setBackground(Color.BLACK);
        allSprites.setForeground(Color.WHITE);
        add(allSprites);

        allSprites.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                imageFrameID = 0;
                imagePreviewID = allSprites.getSelectedIndex();
                updateSpriteFramePosLabel();
            }
        });

        JScrollPane scroll = new JScrollPane (allSprites, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBounds(10, 60, 230, 900);
        add(scroll);

        spriteFramePos = new JLabel("Sprite: 0");
        spriteFramePos.setForeground(Color.WHITE);
        spriteFramePos.setBounds(250, 60, 800, 20);
        spriteFramePos.setVerticalAlignment(SwingConstants.TOP);
        add(spriteFramePos);

        exportAllBtn = new JButton("Export all");
        exportAllBtn.setBounds(360, 10, 120, 40);
        exportAllBtn.setForeground(Color.WHITE);
        exportAllBtn.setBackground(Color.RED);
        add(exportAllBtn);

        exportAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(Main.this);

                File selectedFolder = fileChooser.getSelectedFile();
                if (selectedFolder != null) {
                    MasterExporter.exportPng(Main.this, sprites, selectedFolder.getAbsolutePath());
                }
            }
        });

        exportSelectedBtn = new JButton("Exp. selected");
        exportSelectedBtn.setBounds(490, 10, 120, 40);
        exportSelectedBtn.setForeground(Color.WHITE);
        exportSelectedBtn.setBackground(Color.RED);
        add(exportSelectedBtn);

        exportSelectedBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(Main.this);

                File selectedFolder = fileChooser.getSelectedFile();
                if (selectedFolder != null) {
                    ArrayList<Sprite> selectedSprite = new ArrayList<>();
                    selectedSprite.add(sprites.get(imagePreviewID));

                    MasterExporter.exportPng(Main.this, selectedSprite, selectedFolder.getAbsolutePath());
                }
            }
        });

        replaceSpriteBtn = new JButton("Replace sprite");
        replaceSpriteBtn.setBounds(620, 10, 120, 40);
        replaceSpriteBtn.setForeground(Color.WHITE);
        replaceSpriteBtn.setBackground(Color.magenta);
        add(replaceSpriteBtn);

        replaceSpriteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(Main.this);

                File selectedFolder = fileChooser.getSelectedFile();
                if (selectedFolder != null) {
                    MasterExporter.replaceSprite(Main.this, sprites.get(imagePreviewID), selectedFolder);
                }
            }
        });

        exportAsTspBtn = new JButton("Export TSP");
        exportAsTspBtn.setBounds(750, 10, 120, 40);
        exportAsTspBtn.setForeground(Color.WHITE);
        exportAsTspBtn.setBackground(Color.DARK_GRAY);
        add(exportAsTspBtn);

        exportAsTspBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(Main.this);

                File selectedFolder = fileChooser.getSelectedFile();
                if (selectedFolder != null) {
                    MasterExporter.exportTsp(Main.this, sprites.get(imagePreviewID), selectedFolder);
                }
            }
        });

        exportAsSpriteDataBtn = new JButton("Exp. Sprite data");
        exportAsSpriteDataBtn.setBounds(880, 10, 150, 40);
        exportAsSpriteDataBtn.setForeground(Color.WHITE);
        exportAsSpriteDataBtn.setBackground(Color.DARK_GRAY);
        add(exportAsSpriteDataBtn);

        exportAsSpriteDataBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(Main.this);

                File selectedFolder = fileChooser.getSelectedFile();
                if (selectedFolder != null) {
                    MasterExporter.exportSpriteData(Main.this, spriteFramePos, sprites, selectedFolder);
                }
            }
        });

        //Show frame
        frame.setVisible(true);
        frame.setExtendedState( frame.getExtendedState()|JFrame.MAXIMIZED_BOTH );
        frame.repaint();
        repaint();
    }

    private void updateSpriteFramePosLabel() {
        if (!sprites.isEmpty()) {
            Sprite sprite = sprites.get(imagePreviewID);

            if (!sprite.frames.isEmpty()) {
                Sprite.Frame frame = sprite.frames.get(imageFrameID);
                spriteFramePos.setText("Sprite: " + (imagePreviewID + 1) + "/" + sprites.size() + " \tFrame: " + (imageFrameID + 1) + "/" + sprite.frames.size() + "\t|Width: " + frame.width + " |Height: " + frame.height + " |X: " + frame.originX + " |Y: " + frame.originY + " \t|Name: " + sprite.name);
            }
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    private static File getFile(JPanel panel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("SOW Sprites (.data, .tsp)", "data", "tsp"));

        fileChooser.showOpenDialog(panel);
        return fileChooser.getSelectedFile();
    }

    private void prepareImage(File spritesData, File spritesInfo) {
        Thread loader = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] dataSpritesBytes = new byte[0];
                byte[] spritesInfoBytes = new byte[0];

                //File type
                if (spritesData.getName().endsWith(".tsp")) {
                    //Read bytes
                    try {dataSpritesBytes = Files.readAllBytes(spritesData.toPath());} catch (IOException e) {}

                    sprites.add(new Sprite(spritesData.getName(), dataSpritesBytes.length));

                    //Start
                    readSprite(sprites.size()-1, dataSpritesBytes);
                } else if (spritesData.getName().endsWith(".data")) {
                    //Read bytes
                    try {
                        dataSpritesBytes = Files.readAllBytes(spritesData.toPath());
                        spritesInfoBytes = Files.readAllBytes(spritesInfo.toPath());
                    } catch (IOException e) {}

                    //Sprites.info process
                    generateSprites(spritesInfoBytes);

                    //Start
                    for (int spriteID = 0; spriteID < sprites.size(); spriteID++) {
                        readSprite(spriteID, dataSpritesBytes);
                    }
                }

                //Update UI
                allSprites.setModel(spritesDataModel);
                updateSpriteFramePosLabel();
            }
        });
        loader.start();
    }

    private static final int ZOOM = 4;

    private ImageIcon bcg = new ImageIcon("src/bcg.jpg");

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int spID = imagePreviewID;
        int fmID = imageFrameID;

        //Background
        bcg.paintIcon(this, g, 0, 0);

        //Draw image
        if (!sprites.isEmpty()) {
            Sprite sprite = sprites.get(spID);

            if (sprite.spriteType != Sprite.SpriteType.OTHERS) {
                if (!sprite.frames.isEmpty()) {
                    Sprite.Frame frame = sprite.frames.get(fmID);

                    int x = 0;
                    int y = 0;

                    if (frame.pixels != null) {
                        for (int i = 0; i < frame.pixels.length; i++) {

                            if (frame.pixels[i] == null)
                                g.setColor(new Color(0, 0, 0, 0));
                            else
                                g.setColor(frame.pixels[i]);

                            g.fillRect(x * ZOOM + 250, y * ZOOM + 90, ZOOM, ZOOM);

                            x++;

                            if (x >= frame.width) {
                                x = 0;
                                y++;
                            }
                        }
                    }
                }
            }
        }

        repaint();
    }

    public void generateSprites(byte[] bytes) {
        MasterReader mr = new MasterReader(0, bytes);

        mr.readInt(); // 0x01010101
        int entries = mr.readInt();

        for (int i = 0; i < entries; i++) {
            int temp;
            String name = "";

            while ((temp = mr.readUByte()) != 0x00) {
                name += (char) (temp - 0xa);
            }

            int offset = mr.readInt();
            int length = mr.readInt();

            Sprite sprite = new Sprite(name, offset, length);
            sprites.add(sprite);

            SpritesInfoData.spriteOrdering.add(sprites.size()-1);
        }
    }

    private void readSprite(int spriteId, byte[] dataBytes) {
        Sprite sprite = sprites.get(spriteId);
        MasterReader mr = new MasterReader(sprite.offset, dataBytes);

        spritesDataModel.addElement(sprite.folders + sprite.name + sprite.extension);
        sprite.writingBytes = Arrays.copyOfRange(dataBytes, sprite.offset, sprite.offset + sprite.length);

        if (sprite.spriteType != Sprite.SpriteType.OTHERS) {
            while (sprite.length + sprite.offset > mr.position) {
                Sprite.Frame frame = new Sprite.Frame();

                int size = mr.readInt();

                if (size == 0) {
                    break;
                }

                int outerPosition = mr.position;
                int startPosition = mr.position;

                frame.width = mr.readShort();
                frame.height = mr.readShort();

                frame.originX = mr.readShort();
                frame.originY = mr.readShort();

                frame.unk1 = mr.readShort(); // TODO
                frame.unk2 = mr.readShort(); // TODO
                frame.unk3 = mr.readShort(); // TODO
                frame.unk4 = mr.readShort(); // TODO

                if (frame.width > 0 && frame.height > 0) {
                    frame.pixels = new Color[frame.width * frame.height];

                    for (int y = 0; y < frame.height; y++) {
                        int lineOffset = mr.readInt();
                        int forPosition = mr.position;
                        mr.position = startPosition + lineOffset * 2;

                        int x = 0;
                        int numCommand = mr.readShort();
                        boolean skipMode = mr.readShort() == 0x00;

                        for (int i = 0; i < numCommand; i++) {
                            if (skipMode) {
                                x += mr.readShort();
                            } else {
                                short readPixels = mr.readShort();

                                for (int j = 0; j < readPixels; j++) {
                                    short color16 = mr.readShort();
                                    int color32 = RGB_565toARGB_8888(color16);
                                    frame.pixels[x + y * frame.width] = ARGB_8888toColor(color32);

                                    x++;
                                }
                            }

                            skipMode = !skipMode;
                        }
                        mr.position = forPosition;
                    }
                }

                mr.position = outerPosition + size;

                //Add frame to sprite
                frame.fileName = sprite.folders + sprite.name + "_" + sprite.frames.size();
                sprite.frames.add(frame);
            }
        }
    }

    public static int RGB_565toARGB_8888(int rgb565) {
        return (((rgb565 >> 8) & 0xf8) << 24) | (((rgb565 >> 3) & 0xfc) << 16) | ((rgb565 & 0x1f) << 11) | 0xff;
    }

    public static short ARGB_888toRGB_565(int argb888) {
        return (short) (((((argb888 >> 24) & 0b11111000) << 8) | (((argb888 >> 16) & 0b11111100) << 3) | ((argb888 >> 11) & 0b00011111)) & 0xFFFF);
    }

    public static Color ARGB_8888toColor(int value) {
        float a = ((value & 0xff000000) >>> 24) / 255f;
        float r = ((value & 0x00ff0000) >>> 16) / 255f;
        float g = ((value & 0x0000ff00) >>> 8) / 255f;
        float b = ((value & 0x000000ff)) / 255f;

        return new Color(a, r, g, b);
    }

    public static int ColorToARGB_8888(Color value) {
        return ((value.getRed() << 24) & 0xff000000) | ((value.getGreen() << 16) & 0x00ff0000) | ((value.getBlue() << 8) & 0x0000ff00) | (value.getAlpha() & 0x000000ff);
    }
}
