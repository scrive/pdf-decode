import com.google.gson.annotations.Expose;

class Word {
    @Expose String w = "";
    @Expose LTRB b;

    Word(LTRB r, String c) {
        b = r;
        w = c;
    }

    void add(LTRB q, String c) {
        b.add(q);
        w += c;
    }
}
