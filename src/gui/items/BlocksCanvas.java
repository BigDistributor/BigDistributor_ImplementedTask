package gui.items;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

public class BlocksCanvas extends JComponent {
	final static float dash1[] = { 10.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1,
			0.0f);
	private static final long serialVersionUID = 7920533803582858096L;
	private int extra;
	private Image image;
	private long[] sizes;
	private long blockSize;
	private List<BlockView> blocks;
	private int[] numBlock;

	public BlocksCanvas(Image image, long[] sizes, int[] numBlocks, long blockSize, List<BlockView> blocks, int extra) {
		this.image = image;
		this.extra = extra;
		this.blocks = blocks;
		this.sizes = sizes;
		this.blockSize = blockSize;
		this.numBlock = numBlocks;
	}

	public void paint(Graphics g) {

		// draw image
		g.drawImage(image, extra, extra, this);
		for (BlockView block : blocks) {
			// fill rect used status
			System.out.println("Block: " + block.getMainArrea().toString());
			g.setColor(Colors.Color(block.getStatus()));
			g.fillRect((int) block.getMainArrea().getX(), (int) block.getMainArrea().getY(),
					(int) block.getMainArrea().getWidth(), (int) block.getMainArrea().getHeight());

			// draw extra dotted rect
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setPaint(Color.gray);
			g2.setStroke(dashed);

			g2.drawRect((int) block.getArea().getX(), (int) block.getArea().getY(), (int) block.getArea().getWidth(),
					(int) block.getArea().getHeight());

			// draw main rect
			Graphics2D g3 = (Graphics2D) g;
			g3.setPaint(Color.black);
			g3.setStroke(new BasicStroke(1));
			g3.drawRect((int) block.getMainArrea().getX(), (int) block.getMainArrea().getY(),
					(int) block.getMainArrea().getWidth(), (int) block.getMainArrea().getHeight());

		}

	}

	public void update(ArrayList<BlockView> blocks, int blockSize, int extra) {
		this.blocks = blocks;
		this.blockSize = blockSize;
		this.extra = extra;
		this.repaint();
		
	}
}