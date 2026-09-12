package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

/** Uploads one lighting environment to a world or billboard shader. */
final class LightingUniforms {
    private final float[] positions = new float[LightingEnvironment.MAX_POINT_LIGHTS * 3];
    private final float[] directions = new float[LightingEnvironment.MAX_POINT_LIGHTS * 3];
    private final float[] colors = new float[LightingEnvironment.MAX_POINT_LIGHTS * 4];
    private final float[] parameters = new float[LightingEnvironment.MAX_POINT_LIGHTS * 4];

    void apply(ShaderProgram program, LightingEnvironment environment) {
        Color ambient = environment.getAmbientColor();
        Color sun = environment.getSunColor();
        Vector3 direction = environment.getSunDirection();
        program.setUniformf("u_ambientLight", ambient.r, ambient.g, ambient.b, environment.getAmbientIntensity());
        program.setUniformf("u_sunDirection", direction);
        program.setUniformf("u_sunLight", sun.r, sun.g, sun.b, environment.getSunIntensity());

        int count = Math.min(environment.getPointLights().size, LightingEnvironment.MAX_POINT_LIGHTS);
        int colorIndex = 0;
        int positionIndex = 0;
        int directionIndex = 0;
        int parameterIndex = 0;
        for (int i = 0; i < count; i++) {
            PointLightSource light = environment.getPointLights().get(i);
            if (!light.enabled || light.intensity <= 0f) continue;
            positions[positionIndex++] = light.position.x;
            positions[positionIndex++] = light.position.y;
            positions[positionIndex++] = light.position.z;
            directions[directionIndex++] = light.direction.x;
            directions[directionIndex++] = light.direction.y;
            directions[directionIndex++] = light.direction.z;
            colors[colorIndex++] = light.color.r;
            colors[colorIndex++] = light.color.g;
            colors[colorIndex++] = light.color.b;
            colors[colorIndex++] = light.intensity;
            parameters[parameterIndex++] = light.range;
            parameters[parameterIndex++] = light.isSpot() ? 1f : 0f;
            parameters[parameterIndex++] = light.getInnerConeCos();
            parameters[parameterIndex++] = light.getOuterConeCos();
        }
        int active = colorIndex / 4;
        program.setUniformi("u_pointCount", active);
        if (active > 0) {
            program.setUniform3fv("u_pointPosition", positions, 0, active * 3);
            program.setUniform3fv("u_pointDirection", directions, 0, active * 3);
            program.setUniform4fv("u_pointLight", colors, 0, active * 4);
            program.setUniform4fv("u_pointParams", parameters, 0, active * 4);
        }
    }
}
