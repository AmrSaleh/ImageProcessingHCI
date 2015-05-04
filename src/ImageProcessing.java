import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ImageProcessing extends JPanel {
	/**
	 * 
	 */
	stateObject curStateObject;
	Stack<stateObject> undoStack = new Stack<stateObject>();
	Stack<stateObject> redoStack = new Stack<stateObject>();

	boolean mousePressed;
	boolean zoomInBool = false;
	boolean zoomOutBool = false;
	boolean rotateBool = false;
	boolean cropBool = false;
	static boolean selectBool = false;
	boolean dragMode = false;
	boolean validMousePosition = false;
	Point cropStartPoint = new Point();
	JViewport cropStartViewPort;

	// int zoomRatio = 1;
	static int startX = 0;
	static int startY = 0;
	static int endX = 0;
	static int endY = 0;
	double currentAngle;
	// int resizedWidth;
	// int resizedHeight;

	String fileChooserPath = null;
	String imageName = null;

	File file;

	BufferedImage image;
	BufferedImage originalImage;
	BufferedImage resetImage;

	JFrame frame;

	JPanel motherPanel;
	static JPanel toolBox;

	JButton zoomIn;
	JButton zoomOut;
	JButton rotateLeft;
	JButton rotateRight;
	JButton rot90Left;
	JButton rot90Right;
	JButton crop;
	JButton undoBtn;
	JButton redoBtn;
	JButton resetBtn;

	static JMenuBar menuBar;

	JMenu fileMenu;

	JMenuItem openAction;
	JMenuItem saveMenuItem;
	JMenuItem saveAsMenuItem;

	JScrollPane pane;

	JViewport viewPort;

	Point point;

	// Image image = Toolkit.getDefaultToolkit().getImage("default.jpg");
	RotatePanel rotatePanel;

	private final String[] extensions = { "png", "jpg", "bmp" };
	private final String description = "png, jpg, bmp";

	public static void main(String[] args) {
		if (args.length == 0) {
			new ImageProcessing(null);
		} else {
			new ImageProcessing(args[0]);
		}

	}

	public ImageProcessing(String fileName) {

		try {
			image = ImageIO.read(new File("default.jpg"));
			originalImage = image;
			currentAngle = 0;
			resetImage = image;
			undoStack.clear();
			redoStack.clear();
			curStateObject = new stateObject(image, originalImage, currentAngle);
			undoStack.push(curStateObject);
			// resizedWidth = image.getWidth();
			// resizedHeight= image.getHeight();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			System.out.println("failed loading default image");
			e2.printStackTrace();
		}
		rotatePanel = new RotatePanel(image);
		imageName = fileName;
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnsupportedLookAndFeelException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				file = null;

				if (imageName != null) {
					file = new File(imageName);
				}

				toolBox = new JPanel(new GridLayout(10, 4));

				zoomIn = new JButton("Zoom in");
				zoomIn.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						if (rotateBool) {
							originalImage = image;
							currentAngle = 0;
						}
						resetBooleans();
						zoomInBool = true;
						float zoomLevel = 1.1f;
						int newImageWidth = (int) (image.getWidth() * zoomLevel);
						int newImageHeight = (int) (image.getHeight() * zoomLevel);
						BufferedImage resizedImage = new BufferedImage(newImageWidth, newImageHeight, image.getType());
						Graphics2D g = resizedImage.createGraphics();
						g.setComposite(AlphaComposite.Src);
						g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g.drawImage(originalImage, 0, 0, newImageWidth, newImageHeight, null);
						g.dispose();
						// int w = (int) (image.getWidth()*1.1);
						// int h = (int) (image.getHeight()*1.1);
						//
						// BufferedImage resizedImage = new BufferedImage(w,
						// h,image.getType());
						// AffineTransform at = new AffineTransform();
						// at.scale(1.1, 1.1);
						// AffineTransformOp scaleOp =
						// new AffineTransformOp(at,
						// AffineTransformOp.TYPE_BILINEAR);
						// resizedImage = scaleOp.filter(image, resizedImage);

						image = resizedImage;
						rotatePanel.setImage(image);
						rotatePanel.revalidate();
						rotatePanel.repaint();

						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
						resetSelection();
					}
				});
				zoomOut = new JButton("Zoom out");
				zoomOut.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						if (rotateBool) {
							originalImage = image;
							currentAngle = 0;
						}
						resetBooleans();
						zoomOutBool = true;

						double zoomLevel = 0.9;
						int newImageWidth = (int) (image.getWidth() * zoomLevel);
						int newImageHeight = (int) (image.getHeight() * zoomLevel);
						if (newImageWidth <= 50 || newImageHeight <= 50) {
							return;
						}
						BufferedImage resizedImage = new BufferedImage(newImageWidth, newImageHeight, image.getType());
						Graphics2D g = resizedImage.createGraphics();
						g.setComposite(AlphaComposite.Src);
						g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
						g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g.drawImage(originalImage, 0, 0, newImageWidth, newImageHeight, null);
						g.dispose();

						// int w = (int) (image.getWidth()*0.9);
						// int h = (int) (image.getHeight()*0.9);
						//
						// BufferedImage resizedImage = new BufferedImage(w,
						// h,image.getType());
						// AffineTransform at = new AffineTransform();
						// at.scale(0.9, 0.9);
						// AffineTransformOp scaleOp =
						// new AffineTransformOp(at,
						// AffineTransformOp.TYPE_BILINEAR);
						// resizedImage = scaleOp.filter(image, resizedImage);
						//

						image = resizedImage;
						rotatePanel.setImage(image);
						rotatePanel.revalidate();
						rotatePanel.repaint();

						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
						resetSelection();
					}
				});
				rotateRight = new JButton("RotateR");
				rotateLeft = new JButton("RotateL");
				rot90Right = new JButton("Rot90R");
				rot90Left = new JButton("Rot90L");
				// rotate.addActionListener(new ActionListener() {
				//
				// public void actionPerformed(ActionEvent e) {
				// // TODO Auto-generated method stub
				// resetBooleans();
				// rotateBool = true;
				//
				// rotatePanel.rotate();
				//
				// }
				// });

				rotateRight.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						mousePressed = true;
						new Thread() {
							public void run() {
								while (mousePressed) {
									if (zoomInBool || zoomOutBool) {
										originalImage = image;
										currentAngle = 0;
									}
									resetBooleans();
									rotateBool = true;

									// rotatePanel.rotateRight();

									currentAngle += 5.0;
									if (currentAngle >= 360.0) {
										currentAngle = 0;
									}

									BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
									Graphics2D tg = target.createGraphics();

									AffineTransform at = new AffineTransform();
									at.rotate(Math.toRadians(currentAngle), image.getWidth() / 2, image.getHeight() / 2);
									tg.setComposite(AlphaComposite.Src);
									tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
									tg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
									tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
									tg.drawImage(originalImage, at, null);

									image = target;
									// originalImage = image;
									rotatePanel.setImage(image);
									rotatePanel.revalidate();
									rotatePanel.repaint();
									resetSelection();
									try {
										sleep(200);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}

						}.start();

					}

					public void mouseReleased(MouseEvent e) {
						mousePressed = false;
						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
					}

				});
				rotateLeft.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						mousePressed = true;
						new Thread() {
							public void run() {
								while (mousePressed) {
									if (zoomInBool || zoomOutBool) {
										originalImage = image;
										currentAngle = 0;
									}
									resetBooleans();
									rotateBool = true;

									// rotatePanel.rotateLeft();

									currentAngle -= 5.0;
									if (currentAngle >= 360.0) {
										currentAngle = 0;
									}
									if (currentAngle < 0.0) {
										currentAngle += 360;
									}

									BufferedImage target = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
									Graphics2D tg = target.createGraphics();

									AffineTransform at = new AffineTransform();
									at.rotate(Math.toRadians(currentAngle), image.getWidth() / 2, image.getHeight() / 2);
									tg.setComposite(AlphaComposite.Src);
									tg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
									tg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
									tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
									tg.drawImage(originalImage, at, null);

									image = target;
									// originalImage = image;
									rotatePanel.setImage(image);
									rotatePanel.revalidate();
									rotatePanel.repaint();
									resetSelection();
									try {
										sleep(200);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}

							}

						}.start();
					}

					public void mouseReleased(MouseEvent e) {
						mousePressed = false;
						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
					}

				});
				rot90Right.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						if (rotateBool) {
							originalImage = image;
							currentAngle = 0;
						}
						resetBooleans();
						// rotateBool = true;

						// rotatePanel.rotateLeft();

						BufferedImage target = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
						// Graphics2D tg = target.createGraphics();

						Graphics2D g2 = target.createGraphics();
						g2.translate(image.getHeight(), 0);
						g2.rotate(Math.PI / 2);
						g2.drawImage(image, 0, 0, null);
						g2.dispose();

						image = target;
						originalImage = target;
						rotatePanel.setImage(target);
						rotatePanel.revalidate();
						rotatePanel.repaint();

						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
						resetSelection();
					}
				});
				rot90Left.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						if (rotateBool) {
							originalImage = image;
							currentAngle = 0;
						}
						resetBooleans();
						// rotateBool = true;

						// rotatePanel.rotateLeft();

						BufferedImage target = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
						// Graphics2D tg = target.createGraphics();

						Graphics2D g2 = target.createGraphics();
						g2.translate(0, image.getWidth());
						g2.rotate(Math.toRadians(360 - 90));
						g2.drawImage(image, 0, 0, null);
						g2.dispose();

						image = target;
						originalImage = target;
						rotatePanel.setImage(target);
						rotatePanel.revalidate();
						rotatePanel.repaint();

						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
						resetSelection();
					}
				});
				crop = new JButton("Crop");
				crop.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						if (rotateBool) {
							originalImage = image;
							currentAngle = 0;
						}
						if(!cropBool && !selectBool){
							resetBooleans();
							cropBool = true;
							resetSelection();
							return;
						}
						resetBooleans();
						cropBool = true;
						System.out.println(startX + " " + startY);
						System.out.println(endX + " " + endY);
						//

						if (cropStartPoint == null || cropStartViewPort == null) {
							System.out.println("return");
							return;
						}

						JViewport oldPort = pane.getViewport();
						Point oldPoint = oldPort.getViewPosition();

						rotatePanel.scrollRectToVisible(new Rectangle(cropStartPoint, cropStartViewPort.getSize()));

						int myStartX = startX;
						int myStartY = startY;
						int myEndX = endX;
						int myEndY = endY;

						int imageStartX = rotatePanel.getX() + (rotatePanel.getWidth() / 2) - image.getWidth() / 2;
						int imageStartY = rotatePanel.getY() + (rotatePanel.getHeight() / 2) - image.getHeight() / 2;
						System.out.println("startx " + imageStartX);
						System.out.println("rotatePanelx " + rotatePanel.getX());

						int temp;
						if (myStartX > myEndX) {
							temp = myStartX;
							myStartX = myEndX;
							myEndX = temp;
						}
						if (myStartY > myEndY) {
							temp = myStartY;
							myStartY = myEndY;
							myEndY = temp;
						}
						if (myStartX > imageStartX + image.getWidth() || myStartY > imageStartY + image.getHeight() || myEndX < imageStartX || myEndY < imageStartY) {
							rotatePanel.scrollRectToVisible(new Rectangle(oldPoint, oldPort.getSize()));
							return;
						}

						myStartX -= imageStartX;
						myStartY -= imageStartY;
						if (myStartX < 0) {
							myStartX = 0;
						}

						if (myStartY < 0) {
							myStartY = 0;
						}

						myEndX -= imageStartX;
						myEndY -= imageStartY;
						if (myEndX < 0 || myEndY < 0) {
							rotatePanel.scrollRectToVisible(new Rectangle(oldPoint, oldPort.getSize()));
							return;
						}
						if (myEndX > image.getWidth()) {
							myEndX = image.getWidth();

						}

						if (myEndY > image.getHeight()) {
							myEndY = image.getHeight();
						}

						if ((myEndX - myStartX) == 0 || (myEndY - myStartY) == 0) {
							rotatePanel.scrollRectToVisible(new Rectangle(oldPoint, oldPort.getSize()));
							return;
						}
						BufferedImage croppedImage = image.getSubimage(myStartX, myStartY, myEndX - myStartX, myEndY - myStartY);
						image = croppedImage;
						originalImage = croppedImage;
						rotatePanel.setImage(croppedImage);
						rotatePanel.revalidate();
						rotatePanel.repaint();

						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
						resetSelection();
					}
				});
				undoBtn = new JButton("Undo");
				redoBtn = new JButton("Redo");
				resetBtn = new JButton("Reset");

				undoBtn.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						resetBooleans();
						resetSelection();
						undo();
						
					}
				});
				redoBtn.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						resetBooleans();
						resetSelection();
						redo();
					}
				});
				resetBtn.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						image = resetImage;
						originalImage = resetImage;
						currentAngle = 0;

						rotatePanel.setImage(image);
						rotatePanel.revalidate();
						rotatePanel.repaint();

						redoStack.clear();
						stateObject curState = new stateObject(image, originalImage, currentAngle);
						undoStack.push(curState);
						resetSelection();
					}
				});
				Image zoomInIcon = Toolkit.getDefaultToolkit().getImage("icons/ZoomIn.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;
				zoomIn.setIcon(new ImageIcon(zoomInIcon));
				Image zoomOutIcon = Toolkit.getDefaultToolkit().getImage("icons/ZoomOut.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				zoomOut.setIcon(new ImageIcon(zoomOutIcon));
				Image rotateRightIcon = Toolkit.getDefaultToolkit().getImage("icons/rotateRight.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				rotateRight.setIcon(new ImageIcon(rotateRightIcon));
				Image rotateLeftIcon = Toolkit.getDefaultToolkit().getImage("icons/rotateLeft.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				rotateLeft.setIcon(new ImageIcon(rotateLeftIcon));
				Image rot90RightIcon = Toolkit.getDefaultToolkit().getImage("icons/Rotate90right.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				rot90Right.setIcon(new ImageIcon(rot90RightIcon));
				Image rot90LeftIcon = Toolkit.getDefaultToolkit().getImage("icons/Rotate90left.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				rot90Left.setIcon(new ImageIcon(rot90LeftIcon));
				Image cropIcon = Toolkit.getDefaultToolkit().getImage("icons/cut.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				crop.setIcon(new ImageIcon(cropIcon));
				Image undoBtnIcon = Toolkit.getDefaultToolkit().getImage("icons/Undo.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				undoBtn.setIcon(new ImageIcon(undoBtnIcon));
				Image redoBtnIcon = Toolkit.getDefaultToolkit().getImage("icons/Redo.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				redoBtn.setIcon(new ImageIcon(redoBtnIcon));
				Image resetBtnIcon = Toolkit.getDefaultToolkit().getImage("icons/refresh.png").getScaledInstance( 30, 30,  java.awt.Image.SCALE_SMOOTH ) ;;
				resetBtn.setIcon(new ImageIcon(resetBtnIcon));
				
				
				toolBox.add(zoomIn);
				toolBox.add(zoomOut);
				toolBox.add(rotateRight);
				toolBox.add(rotateLeft);
				toolBox.add(rot90Right);
				toolBox.add(rot90Left);
				toolBox.add(crop);
				toolBox.add(undoBtn);
				toolBox.add(redoBtn);
				toolBox.add(resetBtn);

				menuBar = new JMenuBar();

				fileMenu = new JMenu("File");

				menuBar.add(fileMenu);

				openAction = new JMenuItem("Open");
				saveMenuItem = new JMenuItem("Save");
				saveAsMenuItem = new JMenuItem("Save as");
				

				fileMenu.add(openAction);
				fileMenu.add(saveMenuItem);
//				fileMenu.add(saveAsMenuItem);

				openAction.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						resetSelection();
						JFileChooser fileChooser;
						if (fileChooserPath == null) {
							fileChooser = new JFileChooser();
						} else {
							fileChooser = new JFileChooser(fileChooserPath);
						}
						FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
						fileChooser.setFileFilter(filter);
						int returnVal = fileChooser.showOpenDialog(null);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							file = fileChooser.getSelectedFile();
							fileChooserPath = fileChooser.getSelectedFile().getPath();
							try {
								image = ImageIO.read(file);
								originalImage = image;
								currentAngle = 0;
								resetImage = image;
								undoStack.clear();
								redoStack.clear();
								curStateObject = new stateObject(image, originalImage, currentAngle);
								undoStack.push(curStateObject);
								// resizedWidth = image.getWidth();
								// resizedHeight= image.getHeight();
							} catch (Exception d) {
								System.out.println("Error in reading image");
								// e.printStackTrace();
							}
							try {

								rotatePanel.setImage(image);

								Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
								screenSize.setSize(screenSize.width, screenSize.height - 700);
								frame.setMaximumSize(screenSize);

								frame.setSize(rotatePanel.getPreferredSize().width + toolBox.getWidth() + 20, rotatePanel.getPreferredSize().height + menuBar.getHeight() + 45);

							} catch (Exception d) {
								System.out.println("Error in creating label");
								// label = new JLabel("");

								// e.printStackTrace();
							}
							// label.revalidate();
							// label.repaint();
							rotatePanel.revalidate();
							rotatePanel.repaint();
						}
					}
				});
				
				saveMenuItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						JFileChooser fileChooser;
						if (fileChooserPath == null) {
							fileChooser = new JFileChooser();
						} else {
							fileChooser = new JFileChooser(fileChooserPath);
						}
						FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
						fileChooser.setFileFilter(filter);

						int returnVal = fileChooser.showSaveDialog(frame.getContentPane());
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							System.out.println("You chose to save this file: " + fileChooser.getSelectedFile().getName());

							java.io.File file = fileChooser.getSelectedFile();
							String file_name = file.toString();

							if(!file_name.endsWith(".png") && !file_name.endsWith(".jpg") && !file_name.endsWith(".bmp")){
								file_name=file_name+".png";
							}
							
						//save here
							try {				    
							    File outputfile = new File(file_name);
							    ImageIO.write(image, "png", outputfile);
							} catch (IOException e1) {
							   System.out.println("error in saving");
							}

						}
					}
				});
				
				
				frame = new JFrame("Image Processing");
				pane = new JScrollPane(rotatePanel);
				pane.setAutoscrolls(true);

				motherPanel = new JPanel(new BorderLayout());
				motherPanel.setPreferredSize(new Dimension(800, 600));
				motherPanel.add(toolBox, BorderLayout.WEST);
				motherPanel.add(pane);

				frame.setMinimumSize(new Dimension(400, 300));
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setJMenuBar(menuBar);
				frame.add(motherPanel);
				frame.setVisible(true);

				cropStartViewPort = pane.getViewport();
				cropStartPoint = cropStartViewPort.getViewPosition();
				try {
					image = ImageIO.read(file);
					originalImage = image;
					currentAngle = 0;
					resetImage = image;
					undoStack.clear();
					redoStack.clear();
					curStateObject = new stateObject(image, originalImage, currentAngle);
					undoStack.push(curStateObject);
					// resizedWidth = image.getWidth();
					// resizedHeight= image.getHeight();
				} catch (Exception d) {
					System.out.println("Error no image in arguments, loading default image");
					// e.printStackTrace();
				}
				try {
					rotatePanel.setImage(image);

					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					screenSize.setSize(screenSize.width, screenSize.height - 700);
					frame.setMaximumSize(screenSize);

					frame.setSize(rotatePanel.getPreferredSize().width + toolBox.getWidth() + 20, rotatePanel.getPreferredSize().height + menuBar.getHeight() + 45);

				} catch (Exception d) {
					System.out.println("Error in creating label");

					// e.printStackTrace();
				}
				rotatePanel.addMouseListener(new MouseListener() {

					public void mouseReleased(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					public void mousePressed(MouseEvent e) {
						if (zoomInBool || zoomOutBool || cropBool) {
							selectBool = true;
						}
						startX = e.getX();
						startY = e.getY();

						// }

						// System.out.println("mouse pressed");

					}

					public void mouseExited(MouseEvent e) {
						// TODO Auto-generated method stub
						validMousePosition = false;
					}

					public void mouseEntered(MouseEvent e) {
						// TODO Auto-generated method stub
						validMousePosition = true;
					}

					public void mouseClicked(MouseEvent e) {
						// TODO Auto-generated method stub
						startX = e.getX();
						startY = e.getY();
						endX = e.getX();
						endY = e.getY();
						selectBool = false;
						rotatePanel.revalidate();
						rotatePanel.repaint();
					}
				});
				rotatePanel.addMouseMotionListener(new MouseMotionListener() {

					public void mouseMoved(MouseEvent e) {
						// TODO Auto-generated method stub

					}

					public void mouseDragged(MouseEvent e) {
						// TODO Auto-generated method stub
						// if ((zoomInBool || zoomOutBool || cropBool) &&
						// validMousePosition) {

						endX = e.getX();
						endY = e.getY();
						viewPort = pane.getViewport();
						point = viewPort.getViewPosition();
						if (dragMode) {
							point.translate(startX - endX, startY - endY);
						} else {

							endX = e.getX();
							endY = e.getY();
							int x = 0;
							int y = 0;

							if (endX - startX > 0) {
								x = 5;
							} else {
								x = -5;
							}
							if (endY - startY > 0) {
								y = 5;
							} else {
								y = -5;
							}
							if (!validMousePosition) {

								point.translate(x, y);
							}
						}
						rotatePanel.scrollRectToVisible(new Rectangle(point, viewPort.getSize()));
						if ((zoomInBool || zoomOutBool || cropBool) && validMousePosition) {
							rotatePanel.revalidate();
							rotatePanel.repaint();
						}
						// rotatePanel.setLocation(e.getLocationOnScreen().x -
						// startX, e.getLocationOnScreen().y - startY);
						// System.out.println(endX + " " + endY);

						// }
						// System.out.println("mouse dragged");
					}
				});

			}

		});

	}

	private void resetBooleans() {
		zoomInBool = false;
		zoomOutBool = false;
		rotateBool = false;
		cropBool = false;
		selectBool = false;
	}

	private void undo() {
		if (undoStack.isEmpty()) {
			return;
		}
		stateObject curState = undoStack.pop();
		image = curState.getImage();
		originalImage = curState.getOriginalImage();
		currentAngle = curState.getCurrentAngle();

		rotatePanel.setImage(image);
		rotatePanel.revalidate();
		rotatePanel.repaint();

		redoStack.push(curState);
	}

	private void redo() {
		if (redoStack.isEmpty()) {
			return;
		}
		stateObject curState = redoStack.pop();
		image = curState.getImage();
		originalImage = curState.getOriginalImage();
		currentAngle = curState.getCurrentAngle();

		rotatePanel.setImage(image);
		rotatePanel.revalidate();
		rotatePanel.repaint();

		undoStack.push(curState);
	}
	private void resetSelection() {
		// TODO Auto-generated method stub
		startX=0;
		startY=0;
		endX=0;
		endY=0;
	}

}
