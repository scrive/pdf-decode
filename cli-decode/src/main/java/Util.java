class Util {
    private static double rounding = 1 / Options.THRESHOLD;
    private static double a4ratio = 1.4142;

    static double round(double value) {
        return (double)Math.round(value * rounding) / rounding;
    }
    static double norm950width(double value) { return Math.floor(950 * value); }
    static double norm950height(double value) { return Math.floor(950 * a4ratio * value); }
}
