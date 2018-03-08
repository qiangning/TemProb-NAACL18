package datastruct;

public enum ClinkType {
    CAUSES("c", "causes"),
    CAUSED_BY("cd", "caused_by"),
    UNDEF("undef");
    private final String name;
    private final String fullname;

    ClinkType(String s) {
        name = s;
        fullname = s;
    }

    ClinkType(String name, String fullname) {
        this.name = name;
        this.fullname = fullname;
    }

    public String toString() {
        return this.name;
    }

    public String toStringfull() {
        return this.fullname;
    }

    public ClinkType reverse() {
        switch (this) {
            case CAUSES:
                return CAUSED_BY;
            case CAUSED_BY:
                return CAUSES;
            case UNDEF:
                return UNDEF;
            default:
                System.out.println("Undefined ClinkType.");
                System.exit(-1);
        }
        return UNDEF;
    }

    public static ClinkType reverse(String fullname) {
        return str2TlinkType(fullname).reverse();
    }

    public static ClinkType str2TlinkType(String fullname) {
        switch (fullname) {
            case "causes":
                return CAUSES;
            case "caused_by":
                return CAUSED_BY;
            case "undef":
            case "":
                return UNDEF;
            default:
                System.out.println("Undefined ClinkType.");
                System.exit(-1);
        }
        return UNDEF;
    }

    public static boolean isNull(ClinkType ct){
        return ct==null||ct==UNDEF;
    }
}
