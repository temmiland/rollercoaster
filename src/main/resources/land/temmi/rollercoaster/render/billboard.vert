attribute vec3 a_position;
attribute vec2 a_texCoord0;
uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform vec2 u_targetSize;
uniform vec4 u_uvTransform;
uniform vec4 u_depthPlane;
varying vec2 v_uv;
varying vec3 v_worldPosition;
varying vec3 v_normal;
#ifdef directionalShadow
uniform mat4 u_shadowMatrix;
varying vec4 v_shadowPosition;
#endif

void main() {
    // Columns 0 and 1 carry the quad's own axes, already scaled: a sprite standing on a turned
    // walking plane is rotated there rather than following the camera's axes blindly.
    vec3 right = u_worldTrans[0].xyz;
    vec3 up = u_worldTrans[1].xyz;
    vec3 center = u_worldTrans[3].xyz;
    vec3 worldPosition = center + right * a_position.x + up * (a_position.y + 0.5);
    v_worldPosition = worldPosition;
    v_normal = normalize(cross(up, right));
#ifdef directionalShadow
    v_shadowPosition = u_shadowMatrix * vec4(worldPosition, 1.0);
#endif
    vec4 clip = u_projViewTrans * vec4(worldPosition, 1.0);
    vec4 centerClip = u_projViewTrans * vec4(center, 1.0);
    vec2 centerPixels = (centerClip.xy / centerClip.w * 0.5 + 0.5) * u_targetSize;
    centerPixels = floor(centerPixels + 0.5);
    vec2 centerNdc = centerPixels / u_targetSize * 2.0 - 1.0;
    clip.xy += (centerNdc - centerClip.xy / centerClip.w) * clip.w;
    // Keep the snapped image intact, but test depth against the standing plane through the feet.
    if (abs(u_depthPlane.z) > 0.00001) {
        clip.z = -dot(u_depthPlane.xyw, clip.xyw) / u_depthPlane.z;
    }
    gl_Position = clip;
    v_uv = a_texCoord0 * u_uvTransform.zw + u_uvTransform.xy;
}
