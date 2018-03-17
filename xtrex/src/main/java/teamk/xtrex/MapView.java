package teamk.xtrex;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * @author Conor Spilsbury
 * @author Adam Griffiths
 * 
 * The view part of the map MVC, deals with displaying the map, cursor and button presses
 */
public class MapView extends Screen {

    private byte mapData[] = null;		
    private MapController mapController;
    private GPSparser gps;
    private BufferedImage cursorImg = null;
    
    /* When we construct we read the cursor image to save multiple reads (it needs to be drawn
    * each time the map is refreshed and multiple reads would be ineffcent);
    */
    public MapView(MapController mapController) {
        this.mapController = mapController;
        this.gps = GPSparser.getInstance();
        
        try {
            this.cursorImg = ImageIO.read(new File("cursor.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the data to the passed byte array for the map and draws it if the screen is active.
     * 
     * @param mapData the byte array of image data for the map
     */
    public void setMapData(byte mapData[]) {
        
        this.mapData = mapData;
        
        /* We check if the currently active screen is an instance of mapview and if so we need
        * to repaint the screen
        */
        if (XTrexDisplay.getInstance().getCurrentScreen() instanceof MapView)
            this.repaint();
        
    }
    
    @Override
    public void paint(Graphics g) {
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Converting the byte array for the map into a buffered image object
        ByteArrayInputStream bais = new ByteArrayInputStream(this.mapData);
        BufferedImage image = null;
        try {
            image = ImageIO.read(bais);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /* If the image was read successfuly we rotate it to the current orientation, position
        * it and draw the cursor
        */
        if (image != null) {
            /* Rotation has to be 360 - angle since the bearing is clockwise but rotation is done
            * anti-clockwise
            */
            double rotation = Math.toRadians(360 - (double) gps.TrueTrackAngle());
            double locationX = image.getWidth() / 2;
            double locationY = image.getHeight() / 2;
            AffineTransform tx = AffineTransform.getRotateInstance(rotation, locationX, locationY);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            
            /* We draw the image with an offset (since it is bigger than it needs to be so it 
            * can be rotated) and then draw the cursor image over it in the center of the frame
            */
            g2d.drawImage(op.filter(image, null), -99, -61, null);
            g2d.drawImage(cursorImg, 156, 194, null);
            
        }

    }
    
    @Override
    public void onMinusButtonPressed() {	
        mapController.decreaseZoom();	
    }
    
    @Override
    public void onPlusButtonPressed() {
        mapController.increaseZoom();
    }
    
    @Override 
    public void onSelectButtonPressed() {
        // The select button is disabled for the map screen so we simply return
        return;
    }
}