attribute vec3 a_position;
uniform mat4 u_worldTrans;
uniform mat4 u_shadowTrans;

void main() {
    gl_Position = u_shadowTrans * u_worldTrans * vec4(a_position, 1.0);
}
