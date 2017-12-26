public class Terrain {
    int color;
    String name;
    int avgAlt;
    float waterPer;

    Terrain(int color, String name, int avgAlt, float waterPer) {
        this.color = color;
        this.name = name;
        this.avgAlt = avgAlt;
        this.waterPer = waterPer;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public int getAvgAlt() {
        return avgAlt;
    }
}

