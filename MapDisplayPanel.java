import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MapDisplayPanel extends JPanel {
    static private final Color OUTLINE_COLOR = Color.BLACK;

    private final int WIDTH, MAX_X;
    private final int HEIGHT, MAX_Y;

    public BufferedImage image;
    private Graphics2D g2d;

    float oldX;
    float oldY;

    MapGenerator generator;

    public MapDisplayPanel(int width, int height){
        WIDTH = width;
        HEIGHT = height;
        MAX_X = WIDTH - 1;
        MAX_Y = HEIGHT - 1;

        Dimension size = new Dimension(WIDTH,HEIGHT);
        setMinimumSize(size);
        setMaximumSize(size);
        setPreferredSize(size);

        image = new BufferedImage(WIDTH,HEIGHT,BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();

        generator = new MapGenerator();

        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if(e.getWheelRotation() > 0 ){
                    zoom(1/1.1f);
                }else{
                    zoom(1.1f);
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                oldX = e.getX();
                oldY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        this.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setImage(generator.pan(e.getX()-oldX,e.getY()-oldY));
                oldX = e.getX();
                oldY = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });
    }

    //this is normal
    public void paintComponent( Graphics g ) {
        super.paintComponent( g );
        g.drawImage( image, 0, 0, null);
    }

    //set image
    public void setImage( BufferedImage src ) {
        this.image = src;
        g2d.drawImage(src, 0, 0, MAX_X, MAX_Y, 0, 0, (src.getWidth() - 1), (src.getHeight() - 1), OUTLINE_COLOR, null);
        repaint();
    }

    public void setFinishedImage(){
        setImage(generator.getFinishedImage());
    }

    public void setTerrainImage(){
        setImage(generator.getTerrainImage());
    }

    public void setHeightImage(){
        setImage(generator.getHeightMap());
    }

    public void zoom(float f){
        setImage(generator.zoom(f));
    }

    public void resetView(){
        BufferedImage returnImage = generator.resetView();
        setImage(returnImage);
    }

    BufferedImage getImage(){
        return generator.getImage();
    }

}
