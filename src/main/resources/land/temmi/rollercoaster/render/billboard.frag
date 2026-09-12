#ifdef GL_ES
precision mediump float;
#endif
uniform sampler2D u_texture;
varying vec2 v_uv;
void main() {
    vec4 color = texture2D(u_texture, v_uv);
    if (color.a < 0.5) discard;
    gl_FragColor = vec4(color.rgb, 1.0);
}
