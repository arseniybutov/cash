package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JPanel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.commons.lang.StringUtils;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;

import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview.fonts.PrinterFonts;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview.images.PrinterImages;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableBarCode;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableFontLine;

public class PrinterPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final long printInterval = 5;
	private int ribbonChars = 46;
	private int ribbonWidth = 0;
	private Font normal;
	private Font small;
	private Font fiscal;
	private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	private final int dpi = 250;
	private List<PrinterItem> items = new ArrayList<PrinterItem>();
	private Rectangle lastAddedBounds;
	private Image fiscalLogo;
	private BufferedImage logo;
	private int lastDocsCount;

	public PrinterPanel() {
		if (registerFont()) {
			normal = new Font("Andale Mono", Font.PLAIN, 12);
			small = new Font("Andale Mono", Font.PLAIN, 10);
			fiscal = new Font("Andale Mono", Font.BOLD, 10);
		} else {
			normal = new Font(Font.MONOSPACED, Font.PLAIN, 12);
			small = new Font(Font.MONOSPACED, Font.PLAIN, 10);
			fiscal = new Font(Font.MONOSPACED, Font.BOLD, 10);
		}

		fiscalLogo = PrinterImages.FISCAL_SIGN.getImage();
		logo = PrinterImages.LOGO.getImage();

		appendStartItem();
	}

	public int getLastDocsCount() {
		return lastDocsCount;
	}

	public void setLastDocsCount(int lastDocsCount) {
		this.lastDocsCount = lastDocsCount;
	}

	private boolean registerFont() {
		try {

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Font monoFont = PrinterFonts.getAndaleMono();
			boolean registered = ge.registerFont(monoFont);

			if (registered) {
				System.out.println("monoFont [{" + monoFont.toString() + "}] was registered");
			} else {
				System.out.println("monoFont [" + monoFont.toString() + "] WAS NOT registered");
			}
			return registered;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getRibbonChars() {
		return ribbonChars;
	}

	public void setRibbonChars(int ribbonChars) {
		this.ribbonChars = ribbonChars;
		ribbonWidth = (int) getFontMetrics(normal).getStringBounds(StringUtils.repeat("0", ribbonChars), null).getWidth();
	}

	private int getRibbonWidth() {
		if (ribbonWidth == 0) {
			ribbonWidth = (int) getFontMetrics(normal).getStringBounds(StringUtils.repeat("0", ribbonChars), null).getWidth() + 10;
		}
		return ribbonWidth;
	}

	public int getRibbonChars(ru.crystals.pos.fiscalprinter.Font font) {
		double width = getRibbonWidth() - 10;
		double metric = getFontMetrics(font == ru.crystals.pos.fiscalprinter.Font.SMALL ? small : normal).getStringBounds("0", null).getWidth();
		return (int) (width / metric);
	}

	private void appendStartItem() {
		PrinterItem img = getEmptyImage();
		if (img != null) {
			img.setItemType(PrinterItemType.EMPTY);
			items.add(img);
		}
		try {
			Thread.sleep(printInterval);
		} catch (InterruptedException e) {
		}
	}

	public void appendText(SerializableFontLine line) {
		PrinterItem img = getTextImage(line);
		if (img != null) {
			img.setItemType(PrinterItemType.TEXT);
			items.add(img);
			repaint();
		}
		try {
			Thread.sleep(printInterval);
		} catch (InterruptedException e) {
		}
	}

	public void appendFiscal(long shiftNumber, long docNumber, long kpk) {
		PrinterItem img = getFiscalImage(shiftNumber, docNumber, kpk);
		if (img != null) {
			img.setItemType(PrinterItemType.FISCAL);
			items.add(img);
			repaint();
		}
		try {
			Thread.sleep(printInterval);
		} catch (InterruptedException e) {
		}
	}

	public void appendLogo() {
		PrinterItem img = getLogoImage();
		if (img != null) {
			img.setItemType(PrinterItemType.LOGO);
			items.add(img);
			repaint();
		}
		try {
			Thread.sleep(printInterval);
		} catch (InterruptedException e) {
		}
	}

	public void appendBarcode(SerializableBarCode barCode) {
		PrinterItem img = null;
		if(barCode.getType()== BarCodeType.QR){
			img = getQRcodeImage(barCode.getValue());
		}else {
			img = getBarcodeImage(barCode.getValue(), 20d);
		}
		if (img != null) {
			img.setItemType(PrinterItemType.BARCODE);
			items.add(img);
			repaint();
		}
		try {
			Thread.sleep(printInterval);
		} catch (InterruptedException e) {
		}
	}

	private PrinterItem getQRcodeImage(String value) {
		PrinterItem image = null;
		try{
			BitMatrix matrix = new MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, 0, 0);
			BufferedImage buffer = MatrixToImageWriter.toBufferedImage(matrix);

			int width = getRibbonWidth();
			int desc = getGraphics().getFontMetrics(normal).getDescent();
			int scale = 4;

			image = new PrinterItem(width, buffer.getHeight()*scale+desc,AffineTransformOp.TYPE_BICUBIC);
			image.createGraphics();
			image.getGraphics().setColor(Color.white);
			image.getGraphics().fillRect(0, 0, image.getWidth(), image.getHeight());
			image.getGraphics().drawImage(buffer,
					((image.getWidth() - scale * buffer.getWidth()) / 2),
					desc,
					((image.getWidth() - scale * buffer.getWidth()) / 2) + scale * buffer.getWidth(),
					desc + scale * buffer.getHeight(),
					0,
					0,
					buffer.getWidth(),
					buffer.getHeight(),
					null);
			image.getGraphics().dispose();
			image.setItemType(PrinterItemType.BARCODE);
		}catch (Exception e){}
		return image;
	}

	public void appendCutter() {
		PrinterItem img = getCutterImage();
		if (img != null)
			img.setItemType(PrinterItemType.CUTTER);
		items.add(img);
		removeOldDocs();
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int top = 0;
		Dimension dim = getSize();
		dim.width = getRibbonWidth();

		List<PrinterItem> tmpItems = new ArrayList<PrinterItem>(items);
		for (PrinterItem img : tmpItems) {
			if (img.getItemType().equals(PrinterItemType.EMPTY)) {
				int height = getParent().getHeight();
				try {
					img = getScaledImage(img, getRibbonWidth(), height == 0 ? 1 : height);
				} catch (IOException e) {
				}
			}
			g.drawImage(img, 0, top, null);
			lastAddedBounds = new Rectangle(0, top, img.getWidth(), img.getHeight());
			top += img.getHeight();
		}

		dim.height = top;
		setPreferredSize(dim);
	}

	private PrinterItem getEmptyImage() {
		int width = getRibbonWidth();
		int height = 1;
		return new PrinterItem(width, height, AffineTransformOp.TYPE_BICUBIC);
	}

	private PrinterItem getLogoImage() {
		if (logo != null) {
			int width = getRibbonWidth();
			int height = logo.getHeight();
			int left = (width - logo.getWidth()) / 2;

			PrinterItem img = new PrinterItem(width, height, AffineTransformOp.TYPE_BICUBIC);
			Graphics2D g2d = img.createGraphics();
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, width, height);

			g2d.drawImage(logo, left, 0, null);

			return img;
		}
		return null;
	}

	private PrinterItem getCutterImage() {
		int width = getRibbonWidth();
		int height = 12;

		PrinterItem img = new PrinterItem(width, height, AffineTransformOp.TYPE_BICUBIC);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, width, height);

		g2d.setColor(Color.white);
		Polygon p = new Polygon();
		int n = 0;
		for (int i = 0; i < width; i++) {
			if (i % 3 == 0) {
				p.addPoint(i, n);
				n = n == 0 ? 3 : 0;
			}
		}
		if (p.ypoints[p.npoints - 1] != 0)
			p.addPoint(p.xpoints[p.npoints - 1] + 3, 0);
		g2d.fillPolygon(p);

		p = new Polygon();
		n = 7;
		p.addPoint(0, 12);
		for (int i = 0; i < width; i++) {
			if (i % 3 == 0) {
				p.addPoint(i, n);
				n = n == 7 ? 10 : 7;
			}
		}
		p.addPoint(p.xpoints[p.npoints - 1] + 3, 12);
		g2d.fillPolygon(p);

		return img;
	}

	private PrinterItem getFiscalImage(long shiftNumber, long docNumber, long kpk) {
		Graphics g = getGraphics();
		int width = getRibbonWidth();
		int height = g.getFontMetrics(fiscal).getHeight();
		int totalHeight = height * 2;
		int asc = g.getFontMetrics(fiscal).getAscent();
		int desc = g.getFontMetrics(fiscal).getDescent();

		PrinterItem img = new PrinterItem(width, totalHeight, AffineTransformOp.TYPE_BICUBIC);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, width, totalHeight);

		g2d.setFont(fiscal);
		g2d.setColor(Color.black);

		String s = datetimeFormat.format(Calendar.getInstance().getTime()) + " ДОК N:";
		s += StringUtils.leftPad(String.valueOf(shiftNumber), 4, '0') + ".";
		s += StringUtils.leftPad(String.valueOf(docNumber), 7, '0');

		if (s.length() > ribbonChars)
			s = s.substring(0, ribbonChars);
		g2d.drawString(s, desc, asc);
		int left = (int) (getFontMetrics(fiscal).getStringBounds(s, null).getWidth() + desc);
		g2d.drawImage(fiscalLogo, left, 0, null);

		s = StringUtils.leftPad(String.valueOf(kpk), 8, '0') + " #";
		s += StringUtils.leftPad(String.valueOf(kpk), 6, '0');
		if (s.length() > ribbonChars)
			s = s.substring(0, ribbonChars);
		g2d.drawString(s, desc, height + asc);

		return img;
	}

	private PrinterItem getTextImage(SerializableFontLine line) {
		if (line != null && line.getContent() != null) {
			Graphics g = getGraphics();
			Font font = line.getFont() == ru.crystals.pos.fiscalprinter.Font.SMALL ? small : normal;
			FontMetrics fm = ((font != null && g != null) ? g.getFontMetrics(font) : null);
			int width = getRibbonWidth();
			int height = fm != null ? fm.getHeight() : 1;
			int asc = fm != null ? fm.getAscent() : 0;
			int desc = fm != null ? fm.getDescent() : 0;

			PrinterItem img = new PrinterItem(width, height, AffineTransformOp.TYPE_BICUBIC);
			Graphics2D g2d = img.createGraphics();
			g2d.setColor(Color.white);
			g2d.fillRect(0, 0, width, height);

			String str = line.getContent();
			int strlength = getRibbonChars(line.getFont());
			if (str.length() > strlength)
				str = str.substring(0, strlength);

			g2d.setFont(font);
			g2d.setColor(Color.black);
			g2d.drawString(str, desc, asc);

			if (line.getFont() == ru.crystals.pos.fiscalprinter.Font.DOUBLEHEIGHT) {
				try {
					img = getScaledImage(img, img.getWidth(), img.getHeight() * 2);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return img;
		}
		return null;
	}

	private PrinterItem getScaledImage(PrinterItem image, int width, int height) throws IOException {
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		double scaleX = (double) width / imageWidth;
		double scaleY = (double) height / imageHeight;
		AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
		AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

		BufferedImage img = bilinearScaleOp.filter(image, new BufferedImage(width, height, image.getType()));
		PrinterItem item = new PrinterItem(img.getWidth(), img.getHeight(), img.getType());
		item.setData(img.getData());
		return item;
	}

	private PrinterItem getBarcodeImage(String barCodeStr, double moduleHeight) {
		Graphics g = getGraphics();
		int width = getRibbonWidth();
		int desc = g.getFontMetrics(normal).getDescent();
		AbstractBarcodeBean bean = new Code39Bean();
		bean.doQuietZone(false);
		bean.setFontSize(1);
		bean.setMsgPosition(HumanReadablePlacement.HRP_BOTTOM);
		bean.setBarHeight(UnitConv.in2mm(moduleHeight / dpi));
		bean.setModuleWidth(UnitConv.in2mm(0.8f / dpi));

		BitmapCanvasProvider canvas = new BitmapCanvasProvider(dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
		bean.generateBarcode(canvas, barCodeStr);
		try {
			canvas.finish();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedImage bitmap = canvas.getBufferedImage();
		PrinterItem img = new PrinterItem(width, bitmap.getHeight() + desc, AffineTransformOp.TYPE_BICUBIC);
		img.getGraphics().setColor(Color.white);
		img.getGraphics().fillRect(0, 0, img.getWidth(), img.getHeight());
		img.getGraphics().drawImage(bitmap, ((img.getWidth() - bitmap.getWidth()) / 2), desc, null);

		return img;
	}

	public Rectangle getLastBounds() {
		return lastAddedBounds;
	}

	private void removeOldDocs() {
		boolean remove = false;
		int couter = 0;
		int newSize = 0;
		for (int i = items.size() - 1; i >= 0; i--) {
			PrinterItem img = items.get(i);
			if (img.getItemType() == PrinterItemType.CUTTER) {
				remove = couter >= lastDocsCount;
				couter++;
				if (remove)
					break;
			}
			newSize++;
		}
		if (remove) {
			while (items.size() - 1 > newSize) {
				items.remove(0);
			}
		}
	}

}
