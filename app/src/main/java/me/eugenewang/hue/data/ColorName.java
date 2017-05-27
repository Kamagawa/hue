package me.eugenewang.hue.data;

/**
 * Created by eugen on 5/27/2017.
 */
public class ColorName {
    String color;
    int r;
    int g;
    int b;

    public ColorName(String color, int r, int g, int b) {
        this.color = color;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int computeMSE(int pixR, int pixG, int pixB) {
        return (int) (((pixR - r) * (pixR - r) + (pixG - g) * (pixG - g) + (pixB - b)
                * (pixB - b)) / 3);
    }

    public String getName() {
        return color;
    }

}
