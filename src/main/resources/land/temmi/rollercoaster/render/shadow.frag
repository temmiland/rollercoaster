#ifdef GL_ES
#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif
#endif

void main() {
    vec2 encoded = fract(min(gl_FragCoord.z, 0.99999) * vec2(1.0, 255.0));
    encoded.x -= encoded.y / 255.0;
    gl_FragColor = vec4(encoded, 0.0, 1.0);
}
