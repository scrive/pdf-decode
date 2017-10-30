import com.google.gson.annotations.Expose;
import com.itextpdf.awt.geom.Rectangle2D;

class LTRB {
    @Expose double l;
    @Expose double t;
    @Expose double r;
    @Expose double b;

    LTRB(Rectangle2D s, Rectangle2D q) {
        double w = q.getWidth() / s.getWidth();
        double h = q.getHeight() / s.getHeight();
        double x = q.getX() < 0 ? s.getWidth() + q.getX() : q.getX();
        double y = q.getY() < 0 ? (s.getHeight() + q.getY() - q.getHeight()) : q.getY();
        l = Util.round(x / s.getWidth());
        t = Util.round(((s.getHeight() - y) / s.getHeight()) - h);
        r = Util.round(l + w);
        b = Util.round(t + h);
    }

    void add(LTRB q) {
        r = q.r;
        b = q.b;
    }

    double width() {
        return r - l;
    }

    double height() {
        return b - t;
    }
}
