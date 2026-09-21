package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.Vector3;

/** World plane used for sprite depth, independent of the camera-facing image plane. */
final class BillboardDepthAttribute extends Attribute {
    static final long TYPE = register("billboardDepthPlane");
    final Vector3 normal = new Vector3();
    float offset;

    BillboardDepthAttribute() { super(TYPE); }

    @Override public Attribute copy() {
        BillboardDepthAttribute copy = new BillboardDepthAttribute();
        copy.normal.set(normal);
        copy.offset = offset;
        return copy;
    }

    @Override public int compareTo(Attribute other) {
        if (type != other.type) return type < other.type ? -1 : 1;
        BillboardDepthAttribute depth = (BillboardDepthAttribute) other;
        int result = Float.compare(normal.x, depth.normal.x);
        if (result == 0) result = Float.compare(normal.y, depth.normal.y);
        if (result == 0) result = Float.compare(normal.z, depth.normal.z);
        return result == 0 ? Float.compare(offset, depth.offset) : result;
    }
}
