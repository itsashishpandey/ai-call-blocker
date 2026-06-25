import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Generates the 512x512 Play Store icon for Smart Call Blocker.
 * Mirrors the in-app adaptive launcher icon: gradient bg + white shield
 * + inner blue shield + white phone handset + red "block" dot.
 */
public class IconGenerator {

    public static void main(String[] args) throws Exception {
        // 512x512 is what Play Store wants.
        writeIcon(512, "play_store_icon_512.png");
        // Also emit a 1024 source for marketing crops.
        writeIcon(1024, "play_store_icon_1024.png");
        System.out.println("Done.");
    }

    private static void writeIcon(int S, String outPath) throws Exception {
        BufferedImage img = new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // 1. Diagonal gradient background — Trust Shield colours.
        GradientPaint bg = new GradientPaint(
            0, 0, new Color(0x1E3A8A),
            S, S, new Color(0x3B82F6)
        );
        g.setPaint(bg);
        g.fillRect(0, 0, S, S);

        // The vector drawable uses a 108-unit viewport. Scale to our canvas.
        double k = S / 108.0;

        // 2. Outer shield (white)
        // M54,30 L78,40 L78,58 C78,71 69,82 54,86 C39,82 30,71 30,58 L30,40 Z
        Path2D outer = new Path2D.Double();
        outer.moveTo(54 * k, 30 * k);
        outer.lineTo(78 * k, 40 * k);
        outer.lineTo(78 * k, 58 * k);
        outer.curveTo(78 * k, 71 * k, 69 * k, 82 * k, 54 * k, 86 * k);
        outer.curveTo(39 * k, 82 * k, 30 * k, 71 * k, 30 * k, 58 * k);
        outer.lineTo(30 * k, 40 * k);
        outer.closePath();
        g.setColor(Color.WHITE);
        g.fill(outer);

        // 3. Inner shield (BrandPrimary)
        // M54,36 L72,43.5 L72,58 C72,68.5 65,77 54,80.5 C43,77 36,68.5 36,58 L36,43.5 Z
        Path2D inner = new Path2D.Double();
        inner.moveTo(54 * k, 36 * k);
        inner.lineTo(72 * k, 43.5 * k);
        inner.lineTo(72 * k, 58 * k);
        inner.curveTo(72 * k, 68.5 * k, 65 * k, 77 * k, 54 * k, 80.5 * k);
        inner.curveTo(43 * k, 77 * k, 36 * k, 68.5 * k, 36 * k, 58 * k);
        inner.lineTo(36 * k, 43.5 * k);
        inner.closePath();
        g.setColor(new Color(0x1E40AF));
        g.fill(inner);

        // 4. White phone handset
        // M46,52 C46,50.5 46.8,49.5 47.8,48.8 L51,46 ...
        Path2D phone = new Path2D.Double();
        phone.moveTo(46 * k, 52 * k);
        phone.curveTo(46 * k, 50.5 * k, 46.8 * k, 49.5 * k, 47.8 * k, 48.8 * k);
        phone.lineTo(51 * k, 46 * k);
        phone.curveTo(52 * k, 45.2 * k, 53.5 * k, 45.2 * k, 54.5 * k, 46 * k);
        phone.lineTo(57 * k, 48.5 * k);
        phone.curveTo(58 * k, 49.5 * k, 58 * k, 51 * k, 57 * k, 52 * k);
        phone.lineTo(55.5 * k, 53.5 * k);
        phone.curveTo(55.1 * k, 53.9 * k, 55.1 * k, 54.4 * k, 55.4 * k, 54.8 * k);
        phone.curveTo(56.5 * k, 56.8 * k, 58 * k, 58.4 * k, 60 * k, 59.5 * k);
        phone.curveTo(60.4 * k, 59.7 * k, 60.9 * k, 59.7 * k, 61.3 * k, 59.3 * k);
        phone.lineTo(62.8 * k, 57.8 * k);
        phone.curveTo(63.8 * k, 56.8 * k, 65.3 * k, 56.8 * k, 66.3 * k, 57.8 * k);
        phone.lineTo(68.8 * k, 60.3 * k);
        phone.curveTo(69.8 * k, 61.3 * k, 69.8 * k, 62.8 * k, 68.8 * k, 63.8 * k);
        phone.lineTo(65.7 * k, 67 * k);
        phone.curveTo(65 * k, 67.7 * k, 64 * k, 68.2 * k, 62.5 * k, 68.2 * k);
        phone.curveTo(57.5 * k, 68.2 * k, 50.7 * k, 62 * k, 47.7 * k, 57.8 * k);
        phone.curveTo(46.7 * k, 56 * k, 46 * k, 55 * k, 46 * k, 52 * k);
        phone.closePath();
        g.setColor(Color.WHITE);
        g.fill(phone);

        // 5. Red "block" dot in top-right with white horizontal bar
        double dotCx = 66 * k;
        double dotCy = 40 * k;
        double dotR = 6 * k;
        g.setColor(new Color(0xEF4444));
        g.fill(new Ellipse2D.Double(dotCx - dotR, dotCy - dotR, dotR * 2, dotR * 2));

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(
            (float)(2 * k),
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));
        g.draw(new Line2D.Double(62 * k, 40 * k, 70 * k, 40 * k));

        g.dispose();
        File out = new File(outPath);
        ImageIO.write(img, "PNG", out);
        System.out.println("Wrote " + out.getAbsolutePath() + " (" + out.length() + " bytes)");
    }
}
