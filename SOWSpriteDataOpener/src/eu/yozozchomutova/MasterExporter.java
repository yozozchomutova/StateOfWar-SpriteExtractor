package eu.yozozchomutova;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class MasterExporter {

    public static void exportPng(JPanel jPanel, ArrayList<Sprite> sprites, String pathFolder) {
        generateFolders(jPanel, sprites, pathFolder);

        //Generate pngs
        for (int sID = 0; sID < sprites.size(); sID++) {
            //for (int sID = 0; sID < 1; sID++) {
            Sprite sprite = sprites.get(sID);

            for (int fID = 0; fID < sprite.frames.size(); fID++) {
                //for (int fID = 0; fID < 1; fID++) {
                Sprite.Frame frame = sprite.frames.get(fID);
                String path = pathFolder + "\\" + sprite.folders + sprite.name + "_" + fID + ".png";

                ArrayList<Byte> colorBytesList = new ArrayList<>();

                if (frame.pixels == null)
                    continue;

                for (int i = 0; i < frame.pixels.length; i++) {
                    Color pixel = frame.pixels[i];

                    if (pixel == null)
                        pixel = new Color(0, 0, 0, 0);

                    colorBytesList.add((byte) (pixel.getRed() & 0xFF));
                    colorBytesList.add((byte) (pixel.getGreen() & 0xFF));
                    colorBytesList.add((byte) (pixel.getBlue() & 0xFF));
                    colorBytesList.add((byte) (pixel.getAlpha() & 0xFF));
                }

                //List to array
                byte[] aByteArray = new byte[colorBytesList.size()];
                for (int i = 0; i < colorBytesList.size(); i++) {
                    aByteArray[i] = colorBytesList.get(i);
                }

                DataBuffer buffer = new DataBufferByte(aByteArray, aByteArray.length);

                //3 bytes per pixel: red, green, blue
                //WritableRaster raster = Raster.createInterleavedRaster(buffer, frame.width, frame.height, 4 * frame.width, 4, new int[] {0, 1, 2, 3}, (Point)null);

                WritableRaster raster = Raster.createInterleavedRaster(buffer, frame.width, frame.height, 4 * frame.width, 4, new int[] {0, 1, 2, 3}, (Point)null);
                ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), true, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
                BufferedImage image = new BufferedImage(cm, raster, false, null);

                try {
                    File file = new File(path);

                    if(!file.exists())
                        file.createNewFile();

                    ImageIO.write(image, "png", file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        JOptionPane.showMessageDialog(jPanel, ".png files were successfully exported", "Export success!", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void replaceSprite(JPanel panel, Sprite sprite, File folder) {
        try {
            //Load all needed files
            for (int fID = 0; fID < sprite.frames.size(); fID++) {
                Sprite.Frame frame = sprite.frames.get(fID);

                //Check if file is valid & suitable for replacement
                File neededFile = new File(folder.getPath() + "\\" + frame.fileName + ".png");

                if (!neededFile.exists()) {
                    JOptionPane.showMessageDialog(panel, "Sorry, but we're missing file: " + (folder.getPath() + "\\" + frame.fileName + ".png"), "Missing file", JOptionPane.ERROR_MESSAGE);
                    return;
                } else {
                    BufferedImage img = ImageIO.read(neededFile);

                    int pngWidth = img.getWidth();
                    int pngHeight = img.getHeight();

                    if (pngWidth != frame.width || pngHeight != frame.height) {
                        JOptionPane.showMessageDialog(panel, "Sorry, but entered image is not suitable for replacement. Correct the size of the image. (Image: " + fID + ")", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            for (int fID = 0; fID < sprite.frames.size(); fID++) {
                Sprite.Frame frame = sprite.frames.get(fID);

                //Change pixels
                File neededFile = new File(folder.getPath() + "\\" + frame.fileName + ".png");
                BufferedImage img = ImageIO.read(neededFile);

                int pngWidth = img.getWidth();
                int pngHeight = img.getHeight();

                for (int x = 0; x < pngWidth; x++) {
                    for (int y = 0; y < pngHeight; y++) {
                        Color color = intToColor(img.getRGB(x, y));

                        if (color.getRed() == 0 && color.getGreen() == 0 && color.getBlue() == 0) {
                            frame.pixels[x + y * pngWidth] = null;
                        } else {
                            frame.pixels[x + y * pngWidth] = intToColor(img.getRGB(x, y));
                        }
                    }
                }
            }

            JOptionPane.showMessageDialog(panel, "Replacement was successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void exportTsp(JPanel panel, Sprite sprite, File destinationFolder) {
        //Convert frames
        sprite.generateNewBytes();

        //Finish
        try {
            File file = new File(destinationFolder.toPath() + "\\" + sprite.name + ".tsp");

            if (!file.exists())
                file.createNewFile();

            Files.write(file.toPath(), sprite.writingBytes);
            JOptionPane.showMessageDialog(panel, ".tsp export went successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportSpriteData(JPanel panel, JLabel progressUpdater, ArrayList<Sprite> sprites, File destinationFolder) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //Finish
                try {
                    progressUpdater.setText("Export initiated.");

                    ArrayList<Byte> spritesDataByteList = new ArrayList<>();
                    ArrayList<Byte> spritesInfoByteList = new ArrayList<>();

                    ArrayList<Sprite> writingSprites = new ArrayList<>();

                    //Replace sprites
                    int spritedOrderingSize = SpritesInfoData.spriteOrdering.size();
                    int offset = 0;

                    for (int sID = 0; sID < spritedOrderingSize; sID++) {
                        progressUpdater.setText("[1/3] Generating sprites.DATA: " + (sID+1) + "/" + spritedOrderingSize);

                        Sprite sprite = sprites.get(SpritesInfoData.spriteOrdering.get(sID));
                        writingSprites.add(sprite);

                        if (sprite.spriteType == Sprite.SpriteType.DATA_SPRITE) {
                            sprite.generateNewBytes();
                        }

                        sprite.length = sprite.writingBytes.length;
                        for (int bID = 0; bID < sprite.length; bID++) {
                            spritesDataByteList.add(sprite.writingBytes[bID]);
                        }

                        sprite.offset = offset;
                        offset += sprite.length;
                    }

                    //Generate new sprites.info file
                    MasterReader.writeInt(spritesInfoByteList, 16843009, MasterReader.Order.LITTLE_ENDIAN); // Signature
                    MasterReader.writeInt(spritesInfoByteList, writingSprites.size(), MasterReader.Order.LITTLE_ENDIAN); // Entries

                    for (int i = 0; i < writingSprites.size(); i++) {
                        Sprite sprite = writingSprites.get(i);
                        String location = sprite.folders + sprite.name + sprite.extension;

                        for (int ch = 0; ch < location.length(); ch++) {
                            spritesInfoByteList.add((byte) (location.charAt(ch) + 0xa));
                        }

                        spritesInfoByteList.add((byte) 0x00);

                        MasterReader.writeInt(spritesInfoByteList, sprite.offset, MasterReader.Order.LITTLE_ENDIAN);
                        MasterReader.writeInt(spritesInfoByteList, sprite.length, MasterReader.Order.LITTLE_ENDIAN);

                        progressUpdater.setText("[2/3] Generating sprites.info: " + i + "/" + writingSprites.size());
                    }

                    progressUpdater.setText("[3/3] Writing to files...");

                    //Convert list to array
                    byte[] spritesInfoBytes = new byte[spritesInfoByteList.size()];
                    for (int i = 0; i < spritesInfoBytes.length; i++) {
                        spritesInfoBytes[i] = spritesInfoByteList.get(i);
                    }

                    byte[] spritesDataBytes = new byte[spritesDataByteList.size()];
                    for (int i = 0; i < spritesDataBytes.length; i++) {
                        spritesDataBytes[i] = spritesDataByteList.get(i);
                    }

                    //Write
                    File dataSpritesFile = new File(destinationFolder.toPath() + "\\sprites.data");
                    File infoSpritesFile = new File(destinationFolder.toPath() + "\\sprites.info");

                    if (!dataSpritesFile.exists())
                        dataSpritesFile.createNewFile();

                    if (!infoSpritesFile.exists())
                        infoSpritesFile.createNewFile();

                    Files.write(dataSpritesFile.toPath(), spritesDataBytes);
                    Files.write(infoSpritesFile.toPath(), spritesInfoBytes);
                    JOptionPane.showMessageDialog(panel, "sprites.data & sprites.info export went successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }

    private static void generateFolders(JPanel jPanel, ArrayList<Sprite> sprites, String pathFolder) {
        //Generate folders
        for (int i = 0; i < sprites.size(); i++) {
            File newFolder = new File(pathFolder + "\\" + sprites.get(i).folders);

            if (!newFolder.exists()) {
                boolean success = newFolder.mkdirs();

                if (!success) {
                    JOptionPane.showMessageDialog(jPanel, "Unable to create folder", "Export failed!", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private static Color intToColor(int color) {
        int blue = color & 0xff;
        int green = (color & 0xff00) >> 8;
        int red = (color & 0xff0000) >> 16;

        return new Color(red, green, blue);
    }
}
