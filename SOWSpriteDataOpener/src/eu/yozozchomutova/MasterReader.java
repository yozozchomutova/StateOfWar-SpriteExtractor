package eu.yozozchomutova;

import java.util.ArrayList;

public class MasterReader {

    public int position = 0;
    public byte[] bytes;

    public MasterReader(int position, byte[] bytes) {
        this.position = position;
        this.bytes = bytes;
    }

    //READ OPERATIONS
    public short readShort() {
        short first = this.readUByte();
        short second = this.readUByte();
        return (short) (first | (second << 8));
    }

    public int readUShort() {
        return this.readShort() & 0xffff;
    }

    public int readInt() {
        int first = this.readUShort();
        int second = this.readUShort();
        return first | (second << 16);
    }

    public short readUByte() {
        return (short) (this.readByte() & 0xff);
    }

    private byte readByte(int position) {
        if (position < 0 || position >= bytes.length) {
            throw new IndexOutOfBoundsException();
        }

        return this.bytes[position];
    }

    public byte readByte() {
        return this.readByte(this.position++);
    }

    //WRITE OPERATIONS
    public static void writeInt(ArrayList<Byte> byteList, int value, Order order) {
        if (order == Order.LITTLE_ENDIAN) {
            byteList.add((byte) (value));
            byteList.add((byte) (value >>> 8));
            byteList.add((byte) (value >>> 16));
            byteList.add((byte) (value >>> 24));
        } else  {
            byteList.add((byte) (value >>> 24));
            byteList.add((byte) (value >>> 16));
            byteList.add((byte) (value >>> 8));
            byteList.add((byte) (value));
        }
    }

    public static void writeIntAt(ArrayList<Byte> byteList, int value, int index, Order order) {
        if (order == Order.LITTLE_ENDIAN) {
            byteList.add(index, (byte) (value >>> 24));
            byteList.add(index, (byte) (value >>> 16));
            byteList.add(index, (byte) (value >>> 8));
            byteList.add(index, (byte) (value));
        } else  {
            byteList.add(index, (byte) (value));
            byteList.add(index, (byte) (value >>> 8));
            byteList.add(index, (byte) (value >>> 16));
            byteList.add(index, (byte) (value >>> 24));
        }
    }

    public static void writeShort(ArrayList<Byte> byteList, short value, Order order) {
        if (order == Order.LITTLE_ENDIAN) {
            byteList.add((byte) (value));
            byteList.add((byte) (value >>> 8));
        } else {
            byteList.add((byte) (value >>> 8));
            byteList.add((byte) (value));
        }
    }

    public enum Order {
        LITTLE_ENDIAN, BIG_ENDIAN
    }
}
