import java.io.IOException;
import java.util.*;

import com.google.gson.annotations.Expose;
import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.*;
import com.itextpdf.text.pdf.parser.Vector;

class Page {
    private static final String WHITE_SPACE = "[ \t\n\u000B\f\r\u00A0\uFEFF\u200B]";

    private final Rectangle2D size;

    private final TreeMap<Double, TreeMap<Double, Word>> lines = new TreeMap<>();

    private final ArrayList<PageCharacter> chars = new ArrayList<>();

    @Expose final int number;
    @Expose final double width;
    @Expose final double height;
    @Expose final int rotation;
    @Expose final ArrayList<ArrayList<Word>> words = new ArrayList<>();

    private class PageCharacter {
        final LTRB rect;
        final String text;
        final double spacewidth;

        PageCharacter(double sw, Rectangle2D r, String t) {
            rect = new LTRB(size, r);
            text = t;
            spacewidth = sw;
        }
    }

    private class PageCharacterComparator implements Comparator<PageCharacter> {
        @Override
        public int compare(PageCharacter pc1, PageCharacter pc2) {
            if (pc1.rect.t < pc2.rect.t) return -1;
            if (pc2.rect.t < pc1.rect.t) return 1;
            if (pc1.rect.l < pc2.rect.l) return -1;
            if (pc2.rect.l < pc1.rect.l) return 1;
            return 0;
        }
    }

    private class PageTextRenderListener implements RenderListener {
        PageTextRenderListener(Page text) { }
        public void beginTextBlock() { }
        public void endTextBlock() { }
        public void renderImage(ImageRenderInfo renderInfo) { }

        private boolean isHorizontal(LineSegment base) {
            Vector end = base.getEndPoint().subtract(base.getStartPoint());

            if (rotation == 90) {
                return Math.abs(end.get(Vector.I1)) < 0.0001;
            }

            return Math.abs(end.get(Vector.I2)) < 0.0001;
        }

        public void renderText(TextRenderInfo renderInfo) {
            for (TextRenderInfo tri : renderInfo.getCharacterRenderInfos()) {
                String text = tri.getText().replaceAll(WHITE_SPACE, "");
                if (!text.equals("")) {
                    final LineSegment base = tri.getBaseline();
                    final Rectangle2D r0 = base.getBoundingRectange();
                    final Rectangle2D r1 = tri.getAscentLine().getBoundingRectange();
                    final Rectangle2D r2 = tri.getDescentLine().getBoundingRectange();
                    final Rectangle2D r = rotate(r1.createUnion(r2).createUnion(r0));
                    final double sw = Util.round(tri.getSingleSpaceWidth() / size.getWidth());
                    if (isHorizontal(base)) {
                        chars.add(new PageCharacter(sw, r, text));
                    }
                }
            }
        }
    }

    Page(PdfReader reader, int n) throws IOException {
        Rectangle r = reader.getCropBox(n);
        rotation = reader.getPageRotation(n);
        size = rotate(new Rectangle2D.Float(r.getLeft(), r.getTop(), r.getWidth(), r.getHeight()));

        number = n;
        width = size.getWidth();
        height = size.getHeight();

        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
        parser.processContent(n, new PageTextRenderListener(this));
        processPageCharacters();
        outputWords();
    }

    private Rectangle2D rotate(Rectangle2D r) {
        if (rotation == 90) {
            return new Rectangle2D.Double(r.getY(), -r.getX(), r.getHeight(), r.getWidth());
        }

        return r;
    }

    private static boolean isSpace(PageCharacter pc1, PageCharacter pc2) {
        final LTRB r1 = pc1.rect;
        final LTRB r2 = pc2.rect;
        final double sw = pc2.spacewidth;

        double avg = (r2.width() + r1.width()) / 2;
        double w = Math.min(Options.LETTER_SPACING_RATIO * sw, avg * Options.LETTER_SPACING_RATIO);

        double right = r1.r;
        double left = r2.l;
        double space = Math.max(left - right, 0);

        if (Options.OUTPUT_CHARS) {
            return true;
        }

        return space >= w;
    }

    private void processPageCharacters() {
        Collections.sort(chars, new PageCharacterComparator());

        double wordno = -1d;
        PageCharacter prev = null;

        for (PageCharacter pc : chars) {
            if (wordno < 0) {
                wordno = pc.rect.l;
            }

            if (prev != null && prev.rect.t != pc.rect.t) {
                wordno = pc.rect.l;
                prev = null;
            }

            if (prev != null && isSpace(prev, pc)) {
                wordno = pc.rect.l;
            }

            processPageCharacter(wordno, pc);
            prev = pc;
        }
    }

    private void processPageCharacter(double wordno, PageCharacter pc) {
        TreeMap<Double, Word> line = lines.get(pc.rect.t);

        if (line == null) {
            line = new TreeMap<>();
            lines.put(pc.rect.t, line);
        }

        Word w = line.get(wordno);

        if (w == null) {
            line.put(wordno, new Word(pc.rect, pc.text));
        } else {
            w.add(pc.rect, pc.text);
        }
    }

    private void outputWords() {
        for (Map.Entry<Double, TreeMap<Double, Word>> line : lines.entrySet()) {
            ArrayList<Word> arr = new ArrayList<>();
            words.add(arr);
            for (Map.Entry<Double, Word> word : line.getValue().entrySet()) {
                arr.add(word.getValue());
            }
        }
    }
}
