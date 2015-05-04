import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class RotatePanel extends JPanel {
	private BufferedImage image;
	private double currentAngle;

	public void setCurrentAngle(double currentAngle) {
		this.currentAngle = currentAngle;
		repaint();
	}
	public void setImage(BufferedImage image) {
		this.image = image;
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 0);
		try {
			mt.waitForID(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		setCurrentAngle(0);
	}
	
	public BufferedImage getImage(){
		return image;
	}

	public RotatePanel(BufferedImage image) {
		this.image = image;
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 0);
		try {
			mt.waitForID(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public RotatePanel() {
		// TODO Auto-generated constructor stub
	}

	public void rotateRight() {
		// rotate 5 degrees at a time
		currentAngle += 5.0;
		if (currentAngle >= 360.0) {
			currentAngle = 0;
		}
		repaint();
	}
	public void rotateLeft() {
		// rotate 5 degrees at a time
		currentAngle -= 5.0;
		if (currentAngle >= 360.0) {
			currentAngle = 0;
		}
		if (currentAngle < 0.0) {
			currentAngle += 360;
		}
		repaint();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		if(ImageProcessing.selectBool)
		{
			int x = (getWidth() - image.getWidth(this)) / 2;
			int y = (getHeight() - image.getHeight(this)) / 2;
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.drawImage(image, x,y,this);
			final float dash1[] = {10.0f};
			final BasicStroke dashed =
			        new BasicStroke(1.0f,
			                        BasicStroke.CAP_BUTT,
			                        BasicStroke.JOIN_MITER,
			                        10.0f, dash1, 0.0f);
			g2d.setStroke(dashed);
			g2d.drawRect(Math.min(ImageProcessing.startX,ImageProcessing.endX), Math.min(ImageProcessing.startY,ImageProcessing.endY), Math.abs(ImageProcessing.endX-ImageProcessing.startX), Math.abs(ImageProcessing.endY-ImageProcessing.startY));
		}
		else
		{
			AffineTransform origXform = g2d.getTransform();
			AffineTransform newXform = (AffineTransform) (origXform.clone());
			// center of rotation is center of the panel
			int xRot = this.getWidth() / 2;
			int yRot = this.getHeight() / 2;
			newXform.rotate(Math.toRadians(currentAngle), xRot, yRot);
			g2d.setTransform(newXform);
			// draw image centered in panel
			int x = (getWidth() - image.getWidth(this)) / 2;
			int y = (getHeight() - image.getHeight(this)) / 2;
			g2d.setComposite(AlphaComposite.Src);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.drawImage(image, x, y, this);
			g2d.setTransform(origXform);
		}
	}

	public Dimension getPreferredSize() {
		return new Dimension(image.getWidth(this), image.getHeight(this));
		
		//return new Dimension(image.getWidth(this)+ImageProcessing.toolBox.getWidth()+20, image.getHeight(this)+ImageProcessing.menuBar.getHeight()+45);
	}

//	public static void main(String[] args) {
//		JFrame f = new JFrame();
//		Container cp = f.getContentPane();
//		cp.setLayout(new BorderLayout());
//		BufferedImage testImage = Toolkit.getDefaultToolkit().getImage("c:/gumby.gif");
//		final RotatePanel rotatePanel = new RotatePanel(testImage);
//		JButton b = new JButton("Rotate");
//		b.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent ae) {
//				rotatePanel.rotateRight();
//			}
//		});
//		cp.add(rotatePanel, BorderLayout.CENTER);
//		cp.add(b, BorderLayout.SOUTH);
//		f.pack();
//		f.setVisible(true);
//	}
}