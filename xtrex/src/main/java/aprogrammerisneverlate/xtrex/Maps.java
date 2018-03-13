package aprogrammerisneverlate.xtrex;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Deals with the map screen and google maps API calls
 * 
 * @author Adam Griffiths
 * 
 * @version Sprint 2
 */
public class Maps {
	
	/**
	 * The view part of the map MVC, deals with displaying the map, cursor and button presses
	 */
	private class MapView extends Screen {
	
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
	
	public class MapController {
		
		private MapModel mapModel;
		private MapView mapView;
		
		private MapController() {
			
			this.mapModel = new MapModel();
			this.mapView = new MapView(this);
			
		}
		
		public MapView getScreen() {
			return this.mapView;
		}
		
		public void updateMap() {
			this.mapView.setMapData(this.mapModel.getMapData());
		}
		
		public void increaseZoom() {
			this.mapModel.setZoom(this.mapModel.getZoom() + 1);
			this.updateMap();
		}
		
		public void decreaseZoom() {
			this.mapModel.setZoom(this.mapModel.getZoom() - 1);
			this.updateMap();
		}
		
		
	}
	
	private class MapModel {
		
		private final static String API_BASE = "https://maps.googleapis.com/maps/api/staticmap";
		private final static String API_KEY = "AIzaSyDv8abU01v40-krRiApS1w-zr5Kkcxb0zI";
		private final static String DEFAULT_LAT = "50.77730";
		private final static String DEFAULT_LONG = "-3.52626";
		private final static String IMG_SIZE = "540x540";
		
		
		private GPSparser gps;
		private int zoom = 18;
		
		private MapModel() {
			this.gps = GPSparser.getInstance();
		}
		
		public int getZoom() {
			return this.zoom;
		}
		
		public void setZoom(int zoom) {
			
			if (zoom > 0 && zoom < 22)
				this.zoom = zoom;
			
		}
		
		public byte[] getMapData() {
				
			String latStr  = Double.toString(gps.Latitude());
			String longStr = Double.toString(gps.Longitude());
 				
			
			final String method = "GET";
		    final String url
		      = ( MapModel.API_BASE
		        + "?center=" + latStr + "," + longStr
		        + "&zoom=" + zoom
		        + "&size=" + MapModel.IMG_SIZE
		        + "&key=" + MapModel.API_KEY );
		    
		    final byte[] body = {};
		    final String[][] headers = {};
		   
		    return HttpConnect.httpConnect( method, url, headers, body );
			
		}
		
	}
	
	private static Maps mapsInstance = null;
	
	private MapController mapController = null;
	
	private Maps() {
		
		this.mapController = new MapController();
		
	}
	
	public static MapController getController() {
		
		if (Maps.mapsInstance == null)
			Maps.mapsInstance = new Maps();
		
		return Maps.mapsInstance.mapController;
		
	}
	
}