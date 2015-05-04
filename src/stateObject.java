import java.awt.image.BufferedImage;


public class stateObject {

//	boolean zoomInBool = false;
//	boolean zoomOutBool = false;
//	boolean rotateBool = false;
//	boolean cropBool = true;
//	boolean selectBool = true;
//	boolean dragMode = false;
	
	double currentAngle;
	
	BufferedImage image;
	BufferedImage originalImage;
	
	public stateObject() {
		// TODO Auto-generated constructor stub
	}
	
	public stateObject(BufferedImage curImage,BufferedImage origImage,double angle){
		image = curImage;
		originalImage = origImage;
		currentAngle = angle;
	}

	public double getCurrentAngle() {
		return currentAngle;
	}

	public void setCurrentAngle(double currentAngle) {
		this.currentAngle = currentAngle;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getOriginalImage() {
		return originalImage;
	}

	public void setOriginalImage(BufferedImage originalImage) {
		this.originalImage = originalImage;
	}
}
