package land.temmi.rollercoaster.render;

import com.badlogic.gdx.graphics.g3d.utils.TextureBinder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

final class ShadowUniforms {
    void apply(ShaderProgram program, DirectionalShadowMap shadowMap, TextureBinder textureBinder) {
        int unit = textureBinder.bind(shadowMap.getTexture());
        program.setUniformi("u_shadowMap", unit);
        program.setUniformMatrix("u_shadowMatrix", shadowMap.getShadowMatrix());
        program.setUniformf("u_shadowTexelSize", shadowMap.getTexelSize());
        program.setUniformf("u_shadowBias", shadowMap.getDepthBias());
        program.setUniformf("u_shadowStrength", shadowMap.getStrength());
        program.setUniformi("u_shadowEnabled", shadowMap.isReady() ? 1 : 0);
    }
}
