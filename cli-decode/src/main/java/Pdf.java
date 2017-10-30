import com.google.gson.annotations.Expose;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

class Pdf {
    @Expose final String version = "0.0.4";
    @Expose final String source = "text";
    @Expose final int length;
    @Expose final Page[] pages;
    @Expose long time = 0;

    Pdf(String path) throws IOException, DocumentException {
        this(new FileInputStream(path));
    }

    private Pdf(FileInputStream inputStream) throws IOException, DocumentException {
        this(new PdfReader(inputStream));
    }

    private Pdf(PdfReader reader) throws IOException, DocumentException {
        long start = System.currentTimeMillis();
        System.setProperty("java.awt.headless", "true");
        com.itextpdf.text.pdf.PdfReader.unethicalreading = true;
        PdfReader flat = createFlattened(reader);
        length = flat.getNumberOfPages();
        pages = new Page[length];
        for (int i = 0; i < length; i++) {
            pages[i] = new Page(flat, i+1);
        }
        time = System.currentTimeMillis() - start;
    }

    private static PdfReader createFlattened(PdfReader reader) throws IOException, DocumentException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, buf);
        stamper.setFormFlattening(true);
        stamper.setFreeTextFlattening(true);
        stamper.close();
        return new PdfReader(buf.toByteArray());
    }
}
