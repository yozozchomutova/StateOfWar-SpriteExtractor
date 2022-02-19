package eu.yozozchomutova;

import java.awt.*;
import java.util.ArrayList;

public class Sprite {

    public String extension;
    public String name;
    public String folders;

    public SpriteType spriteType;

    public int offset;
    public int length;

    public byte[] writingBytes;

    public Sprite(String fullName, int length) {
        init(fullName, 0, length, SpriteType.TSP);
    }

    public Sprite(String fullName, int offset, int length) {
        init(fullName, offset, length, SpriteType.DATA_SPRITE);
    }

    private void init(String fullName, int offset, int length, SpriteType spriteType) {
        this.offset = offset;
        this.length = length;
        this.spriteType = spriteType == SpriteType.DATA_SPRITE ? (fullName.endsWith(".ps6") ? spriteType : SpriteType.OTHERS) : spriteType;

        //Find '/'. Every file should have one.
        int columnPos = 0;
        boolean columnFound = false;

        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == '\\') {
                columnPos = i;
                columnFound = true;
            }
        }

        this.extension = fullName.substring(fullName.length()-4);
        this.name = fullName.substring(columnPos + (columnFound ? 1 : 0), fullName.length()-4);

        if (columnFound)
            this.folders = fullName.substring(0, columnPos) + "\\";
        else
            this.folders = fullName.substring(0, columnPos);
    }

    public void generateNewBytes() {
        ArrayList<Byte> bytes = new ArrayList<>();

        for (int fID = 0; fID < frames.size(); fID++) {
            Sprite.Frame frame = frames.get(fID);

            byte[] frameBytes = frame.compress();

            for (byte frameByte : frameBytes) {
                bytes.add(frameByte);
            }
        }

        MasterReader.writeInt(bytes, 0, MasterReader.Order.LITTLE_ENDIAN);

        //List -> array
        writingBytes = new byte[bytes.size()];
        for (int i = 0; i < writingBytes.length; i++) {
            writingBytes[i] = bytes.get(i);
        }
    }

    public ArrayList<Frame> frames = new ArrayList<>();

    public static class Frame {
        public String fileName;
        public Color[] pixels;

        public short width;
        public short height;

        public short originX;
        public short originY;

        public short unk1;
        public short unk2;
        public short unk3;
        public short unk4;

        public byte[] compress() {
            ArrayList<Byte> byteList = new ArrayList<>();

            //Header compress
            //Width & Height
            MasterReader.writeShort(byteList, width, MasterReader.Order.LITTLE_ENDIAN);
            MasterReader.writeShort(byteList, height, MasterReader.Order.LITTLE_ENDIAN);

            //Origins
            MasterReader.writeShort(byteList, originX, MasterReader.Order.LITTLE_ENDIAN);
            MasterReader.writeShort(byteList, originY, MasterReader.Order.LITTLE_ENDIAN);

            //Unknown values
            MasterReader.writeShort(byteList, unk1, MasterReader.Order.LITTLE_ENDIAN);
            MasterReader.writeShort(byteList, unk2, MasterReader.Order.LITTLE_ENDIAN);
            MasterReader.writeShort(byteList, unk3, MasterReader.Order.LITTLE_ENDIAN);
            MasterReader.writeShort(byteList, unk4, MasterReader.Order.LITTLE_ENDIAN);

            int reservedBytesByHeightLineOffsets = 16 + 4 * height;
            int lastLineOffset = 0;

            //Pixels
            for (int y = 0; y < height; y++) {
                //Pixels
                ArrayList<Short> compressedPixels = RLEPixelCompress(y);
                for (int i = 0; i < compressedPixels.size(); i++) {
                    MasterReader.writeShort(byteList, compressedPixels.get(i), MasterReader.Order.LITTLE_ENDIAN);
                }

                //Write line offset
                int lineOffset = (reservedBytesByHeightLineOffsets) / 2 + lastLineOffset; //+4 - lineOffset data, 4-byte header is excluded!
                MasterReader.writeIntAt(byteList, lineOffset, 16 + (y * 4), MasterReader.Order.LITTLE_ENDIAN);
                lastLineOffset += compressedPixels.size();
            }

            //Write Data length on start
            int dataLength = byteList.size();
            MasterReader.writeIntAt(byteList, dataLength, 0, MasterReader.Order.LITTLE_ENDIAN);

            //List to array
            byte[] bytes = new byte[byteList.size()];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = byteList.get(i);
            }
            return bytes;
        }

        private ArrayList<Short> RLEPixelCompress(int y) {
            ArrayList<Short> pixelData = new ArrayList<>();

            short numCommands = 0;
            boolean skipMode = false;

            for (int x = 0; x < width;) {
                Color mainPixel = pixels[x + width * y];

                short pixelLength = 0;

                if (x == 0) {
                    //Color handling
                    if (mainPixel == null) { //Pixel is transparent => Skip pixels
                        skipMode = true;
                    } else { //Non transparent -> Draw pixels
                        skipMode = false;
                    }

                    pixelData.add((short) (skipMode ? 0x00 : 0x01));
                } else {
                    skipMode = !skipMode;
                }

                //Skip mode
                if (skipMode) {
                    numCommands++;

                    for (int i2 = x; i2 < width; i2++) {
                        Color neighbourPixel = pixels[i2 + width * y];

                        if (neighbourPixel == null) {
                            pixelLength++;
                        } else {
                            break;
                        }
                    }

                    pixelData.add(pixelLength);
                    x += pixelLength;
                } else {
                    numCommands++;

                    pixelData.add((short) 0x00);
                    int reservedIndex = pixelData.size()-1;

                    for (int i2 = x; i2 < width; i2++) {
                        Color neighbourPixel = pixels[i2 + width * y];

                        if (neighbourPixel != null) {
                            pixelLength++;
                            short rgb565pixel = Main.ARGB_888toRGB_565(Main.ColorToARGB_8888(neighbourPixel));
                            pixelData.add(rgb565pixel);
                        } else {
                            break;
                        }
                    }

                    pixelData.set(reservedIndex, pixelLength);
                    x += pixelLength;
                }
            }

            //Don't forget on headers
            pixelData.add(0, numCommands);

            return pixelData;
        }
    }

    public enum SpriteType {
        DATA_SPRITE,
        TSP,
        OTHERS
    }
}
