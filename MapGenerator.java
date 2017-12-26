import com.sun.prism.Graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by William on 11/5/2017.
 */
public class MapGenerator extends JPanel {

    //--------COLORS FOR EACH TERRAIN----------
    final int OCEAN = 0xFF0E416D;
    final int ICY = 0xFFC9FCF8;
    final int PLAINS = 0xFF6D9E45;
    final int DESERT = 0xFFEFEEC4;
    final int FOREST = 0xFF344F34;
    final int MOUNTAIN = 0xFF72654E;

    ArrayList<Terrain> terrains = new ArrayList<Terrain>();      //list of possible terrains

    int[][] heights;                                             //stores height for terrainImage
    int size;                                                    //input size
    Random rand = new Random();                                  //to get random numbers

    BufferedImage terrainImage;                                  //terrainImage
    BufferedImage heightImage;
    BufferedImage heightMask;
    int type;

    float scaleFactor = 1;
    float xTrans = 0;
    float yTrans = 0;


    MapGenerator() {
        terrains.add(new Terrain(ICY,"Icy",250,0.2f));
        terrains.add(new Terrain(PLAINS,"Plains",170,0.5f));
        terrains.add(new Terrain(DESERT,"Desert",200,0.0f));
        terrains.add(new Terrain(FOREST,"Forest",180,0.5f));
        terrains.add(new Terrain(MOUNTAIN,"Mountain",230,0.2f));
    }

    //----------------GENERATION STUFF-----------------------------------------------

    //takes in chances for each terrain type, number of seeds, and how many generations
    void makeImage(ArrayList<Float> percents, int seedNum, int gens, int size, int sealevel) {
        this.size = size;
        this.scaleFactor = size/1000f;
        xTrans = 0;
        yTrans = 0;

        System.out.println("______________________");
        System.out.println(LocalDateTime.now() + ": Making terrain...");
        makeTerrain(percents, seedNum, gens);
        System.out.println(LocalDateTime.now() + ": Terrain made!");
        //----------------------REMOVE ANOMALIES, TERRAIN-SPECIFIC STUFF------------------------------------

        //Making height map
        System.out.println(LocalDateTime.now() + ": Adding elevation...");
        for(int i = 0; i<5; i++) {
            makeHeightMap();
        }
        System.out.println(LocalDateTime.now() + ": Elevation added!");

        System.out.println(LocalDateTime.now() + ": Adding some water...");
        addGoodLakes(sealevel);
        System.out.println(LocalDateTime.now() + ": Water added!");
        System.out.println(LocalDateTime.now() + ": Removing ugly lakes...");
        removeBadLakes(sealevel);
        System.out.println(LocalDateTime.now() + ": Lakes removed");

        heightImage = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        heightMask = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        for(int i = 0; i<size;i++){
            for(int j = 0; j<size;j++){
                heightMask.setRGB(i,j,getARGB(100,heights[i][j],heights[i][j],heights[i][j]));
                heightImage.setRGB(i,j,getARGB(255,heights[i][j],heights[i][j],heights[i][j]));
            }
        }
    }

    //takes initial parameters, and makes initial terrain/heightmap by randomly growing
    void makeTerrain(ArrayList<Float> percents, int seedNum, int gens){

        //---------VARIABLES------------------
        terrainImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        heights = new int[size][size];
        int genScale = (int)(size/1000f);
        ArrayList<Point2D.Float> seeds = new ArrayList<Point2D.Float>(); //hold current seeds
        ArrayList<Point2D.Float> nextGen = new ArrayList<Point2D.Float>(); //hold next gen's seeds
        int[] xMoves = {0, 1, 0, -1}; //random x moves
        int[] yMoves = {1, 0, -1, 0}; //random y moves


        //--------------------INITIALIZATION-------------------
        //set to water initially
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                terrainImage.setRGB(i, j, OCEAN);
            }
        }

        //add seeds
        for (int i = 0; i < seedNum; i++) {
            double r = rand.nextDouble();
            //if the random number is below the %chance to appear, add a seed
            for (int j = 0; j < percents.size(); j++) {
                if (r < percents.get(j)) {
                    int x = rand.nextInt(size);
                    int y = rand.nextInt(size);
                    seeds.add(new Point2D.Float(x, y));
                    terrainImage.setRGB(x, y, terrains.get(j).getColor());
                    heights[x][y] = terrains.get(j).getAvgAlt();
                }
            }
        }


        //----------------------FIRST PASS OF GENERATION-------------------------------
        //for every gen, expand seeds
        for (int i = 0; i < gens*genScale; i++) {
            //for all current seeds
            for (int j = 0; j < seeds.size(); j++) {
                int x = (int) seeds.get(j).getX();//get x loc
                int y = (int) seeds.get(j).getY();//get y loc
                int color = terrainImage.getRGB(x, y);//get current color of seed

                //for all moves
                for (int k = 0; k < xMoves.length; k++) {
                    int tX = x + xMoves[k]; //get candidate x spot
                    int tY = y + yMoves[k]; //get candidate y spot

                    //if in range, and candidate spot is not drawing over existing terrain
                    if (tX < size && tX >= 0 && tY < size && tY >= 0 && terrainImage.getRGB(tX, tY) == OCEAN) {
                        //randomness, will be decided later by user
                        if (rand.nextFloat() < 0.51f) {
                            terrainImage.setRGB(tX, tY, color); //color terrainImage
                            heights[tX][tY]=heights[x][y];
                            nextGen.add(new Point2D.Float(tX, tY)); //add spot to next set of seeds
                        }
                    }
                }
            }

            //remove all seeds (already been used)
            seeds.clear();
            for (int k = 0; k < nextGen.size(); k++) {
                seeds.add(nextGen.get(k)); //set to next generations seeds
            }
            nextGen.clear(); //clear for new use
        }
    }

    //takes heights array and smooths it
    void makeHeightMap(){
        for(int i = 0; i < size;i++){
            for(int j = 0; j < size;j++) {
                int avg = averageOfNeighbors(i,j,3);
                heights[i][j]=avg;
            }
        }
    }

    //if below sea level, make water
    void addGoodLakes(int sealevel){
        for(int i = 0; i < size;i++){
            for(int j = 0; j < size;j++) {
                if(heights[i][j]<sealevel){
                    terrainImage.setRGB(i,j,OCEAN);
                }
            }
        }
    }

    //if above or at sea level, make nearest lant
    void removeBadLakes(int sealevel){
        for(int i = 0; i < size;i++){
            for(int j = 0; j < size;j++) {
                if(heights[i][j]>=sealevel && terrainImage.getRGB(i,j)==OCEAN){
                    terrainImage.setRGB(i,j,nearestTerrain(i,j));
                }
            }
        }
    }

    //returns average values of neighbors around x,y, in a 2*degree+1 square
    int averageOfNeighbors(int x,int y, int degree){
        int avg = 0;
        int count = 0;

        for(int i = -degree; i <= degree; i++){
            for(int j = -degree;j<=degree;j++) {
                if (x + i < size && x + i >= 0 && y + j < size && y + j >= 0) {
                    avg += heights[x + i][y + j];
                    count++;
                }
            }
        }

        avg = avg / count;
        return avg;
    }

    //finds nearest non-Ocean terrain from x,y
    int nearestTerrain(int x, int y){
        int cand = 0xFF000000;
        int dist= 10000000;

        int i = 0;
        while(x+i+1<size && terrainImage.getRGB(x+i,y)==OCEAN){
            if(terrainImage.getRGB(x+i+1,y)!=OCEAN && i<dist){
                cand = terrainImage.getRGB(x+i+1,y);
                dist = i;
            }
            i++;
        }
        i=0;
        while(y+i+1<size && terrainImage.getRGB(x,y+i)==OCEAN){
            if(terrainImage.getRGB(x,y+i+1)!=OCEAN && i<dist){
                cand = terrainImage.getRGB(x,y+i+1);
                dist = i;
            }
            i++;
        }
        i=0;
        while(x-i-1>=0 && terrainImage.getRGB(x-i,y)==OCEAN){
            if(terrainImage.getRGB(x-i-1,y)!=OCEAN && i<dist){
                cand = terrainImage.getRGB(x-i-1,y);
                dist = i;
            }
            i++;
        }
        i=0;
        while(y-i-1>=0 && terrainImage.getRGB(x,y-i)==OCEAN){
            if(terrainImage.getRGB(x,y-i-1)!=OCEAN && i<dist){
                cand = terrainImage.getRGB(x,y-i-1);
                dist = i;
            }
            i++;
        }

        return cand;
    }

    //--------------DISPLAY STUFF------------------------------------------------------


    //returns terrainImage of height map colors
    BufferedImage getHeightMap(){
        type = 1;

        BufferedImage returnImage = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)returnImage.getGraphics();

        float dX = ((float)size/2) + xTrans;
        float dY = ((float)size/2) + yTrans;
        g2d.translate(size/2,size/2);
        g2d.scale(scaleFactor,scaleFactor);
        g2d.translate(-dX,-dY);
        g2d.drawImage(heightImage,0,0,null);

        return returnImage;
    }

    //returns plain terrain terrainImage
    BufferedImage getTerrainImage(){
        type = 2;
        BufferedImage returnImage = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)returnImage.getGraphics();

        float dX = ((float)size/2) + xTrans;
        float dY = ((float)size/2) + yTrans;
        g2d.translate(size/2,size/2);
        g2d.scale(scaleFactor,scaleFactor);
        g2d.translate(-dX,-dY);

        g2d.drawImage(terrainImage,0,0,null);

        return returnImage;
    }

    //layers heightmap on terrain
    BufferedImage getFinishedImage(){
        type = 3;
        //copy normal terrainImage
        BufferedImage regCopy = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D)regCopy.createGraphics();

        float dX = ((float)size/2) + xTrans;
        float dY = ((float)size/2) + yTrans;
        g2d.translate(size/2,size/2);
        g2d.scale(scaleFactor,scaleFactor);
        g2d.translate(-dX,-dY);

        g2d.drawImage(terrainImage,0,0,null);

        g2d.drawImage(heightMask,0,0,null);
        return regCopy;
    }

    BufferedImage zoom(float coef){
        scaleFactor*=coef;

        switch(type){
            case 3:
                return getFinishedImage();
            case 2:
                return getTerrainImage();
            case 1:
                return getHeightMap();
        }

        return getFinishedImage();
    }

    BufferedImage pan(float dx, float dy){

        xTrans -= dx/scaleFactor;
        yTrans -= dy/scaleFactor;

        switch(type){
            case 3:
                return getFinishedImage();
            case 2:
                return getTerrainImage();
            case 1:
                return getHeightMap();
        }

        return getFinishedImage();
    }

    BufferedImage getImage(){
        switch(type){
            case 3:
                BufferedImage regCopy = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = (Graphics2D)regCopy.createGraphics();
                g2d.drawImage(terrainImage,0,0,null);

                BufferedImage heightImage = new BufferedImage(size,size,BufferedImage.TYPE_INT_ARGB);
                for(int i = 0; i<size;i++){
                    for(int j = 0; j<size;j++){
                        heightImage.setRGB(i,j,getARGB(100,heights[i][j],heights[i][j],heights[i][j]));
                    }
                }

                g2d.drawImage(heightImage,0,0,null);
                return regCopy;
            case 2:
                return getTerrainImage();
            case 1:
                return getHeightMap();
        }
        return null;
    }

    BufferedImage resetView(){
        scaleFactor = 1;
        xTrans = 0;
        yTrans = 0;

        switch(type){
            case 3:
                return getFinishedImage();
            case 2:
                return getTerrainImage();
            case 1:
                return getHeightMap();
        }

        return getFinishedImage();
    }

    //Methods to implement efficient ARGB synthesis/extraction

    public int getARGB(int A, int R, int G, int B) {
        return (A << 24) | (R << 16) | (G << 8) | B;
    }
}
