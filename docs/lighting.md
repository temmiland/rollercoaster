# Lighting and shadows

`LightingEnvironment` shares ambient, directional sun and up to eight point/spot lights
between the geometry and billboard shaders. `DayNightCycle` updates sun and ambient
colours/intensities on a 24-hour clock. Night ambient remains bright enough to navigate.

## Sun shadows

Create `DirectionalShadowMap(lighting, resolution)` on the GL thread, then pass it to
`WorldShaderProvider(lighting, shadows)`. Before beginning the main framebuffer/ModelBatch:

```java
shadows.render(focusPosition, worldScene.getInstances());
lowRes.begin();
batch.begin(camera);
batch.render(worldScene.getVisibleInstances(camera, visible));
batch.end();
lowRes.end();
```

Submit all opaque casters in range, including objects outside the view camera frustum.
Terrain and model geometry cast shadows; geometry and billboards receive them. This is
one directional shadow map, not cascades. `setWorldSize` controls its square world-space
coverage; `setLightDistance` controls placement/depth range. Outside that volume, sun
lighting remains unshadowed. Dispose the map separately from the main ModelBatch.

The depth pass packs depth in two colour channels, with dithering disabled, nearest
texture filtering and depth writes enabled before clearing. A texel-aligned light camera,
3x3 PCF and slope-dependent comparison bias reduce movement shimmer and self-shadowing.
`setDepthBias` uses normalized depth; excessive values detach shadows from their casters.
Only direct sunlight is shadowed, so ambient and local lights still illuminate the shade.

Point/spot lights currently do not cast shadows or respect wall occlusion. Billboards
receive sun shadows but are not supported as casters. There is no bloom or indirect
illumination. Device GL validation is still needed; desktop tests exercise GL 2.1.

## Materials and lamps

The geometry shader reads glTF base-colour and emissive factors/textures, including
emissive intensity, as well as libGDX diffuse/emissive attributes. Texture UV0 with
offset/scale is supported. Emission is added after lighting and is independent of shadows.
A glowing window therefore needs an emissive material; to illuminate nearby ground,
place a separate point/spot light outside the window. Emission alone is not a light source.
Diffuse lighting is multiplied by the material colour before final framebuffer clamping.

Local lights use a smooth finite-range falloff and spot cone attenuation. Tune position,
range and intensity together. Keep lamps outside solid geometry. The example puts warm
point lights at street fixtures and outward/downward spot lights at the house windows.
Its `ExampleLighting` switches all local sources and emissive materials off during the
day, fades them in at dusk and preserves the manual L override across time changes.

## Verification

In the sibling example repository run `./gradlew :lightingSmokeTest --offline`.
It measures model and terrain shadow contrast, clearing after moving a caster, light
range, emission in darkness and daytime/nighttime switching. It also saves day and
night screenshots in the Java temporary directory. The regular `:renderSmokeTest`
continues to verify depth ordering, textures, map loading and terrain behavior.
