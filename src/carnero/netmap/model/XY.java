package carnero.netmap.model;

public class XY {

    public int x;
    public int y;

    public XY() {
        // empty
    }

    public XY(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof XY)) {
            return false;
        }

        final XY xy = (XY) object;

        return (xy.x == x && xy.y == y);
    }

    @Override
    public int hashCode() {
        int code = (x << 16) + y;

        return code;
    }

    @Override
    public String toString() {
        return "Sector [" + x + ":" + y + "]";
    }
}
