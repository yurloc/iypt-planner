package org.iypt.domain;

/**
 *
 * @author jlocker
 */
//public final class Country {
//
//    private final String code;
//    private final String fullName;
//
//    public Country(String code, String fullName) {
//        this.code = code;
//        this.fullName = fullName;
//    }
//
//    @Override
//    public String toString() {
//        return String.format("[%s] %s", code, fullName);
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 97 * hash + (this.code != null ? this.code.hashCode() : 0);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Country other = (Country) obj;
//        if ((this.code == null) ? (other.code != null) : !this.code.equals(other.code)) {
//            return false;
//        }
//        return true;
//    }
//
//    /**
//     * Get the value of code
//     *
//     * @return the value of code
//     */
//    public String getCode() {
//        return code;
//    }
//
//    /**
//     * Get the value of fullName
//     *
//     * @return the value of fullName
//     */
//    public String getFullName() {
//        return fullName;
//    }
//    
//    public static final Country AUT = new Country("AUT", "Austria");
//    public static final Country BEL = new Country("BEL", "Belgium");
//    public static final Country BGR = new Country("BGR", null);
//    public static final Country CZE = new Country("CZE", "Czech Republic");
//    public static final Country GER = new Country("GER", "Germany");
//    public static final Country POL = new Country("POL", "Poland");
//    public static final Country SVK = new Country("SVK", "Slovakia");
//}

public enum Country {
    AUT, BEL, BGR, CZE, GER, POL, SVK, ABC, DEF, GHI, JKL, MNO, PQR, STU, VWX, YZZ,
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z
}