# Billboard depth

`BillboardRenderer` draws a camera-facing image but computes occlusion against a standing plane
through the actor's feet. Using the image plane itself for depth makes the sprite lean into walls;
at a 45-degree camera pitch it can even coincide with an ascending ramp.

The standing plane spans the sprite's right axis and `depthUp`, which defaults to world Y.
Characters remain upright on ordinary ramps. Call `setDepthUp` with the walking plane's normal
for a character standing on a wall or ceiling, and restore world Y on returning to ordinary terrain.
`setBasis` continues to control the visible image orientation independently.

`BillboardDepthAttribute` carries the world plane in the material. The shader transforms its
equation by the inverse transpose of the current projection-view matrix, including the camera's
pixel snap. For a clip-space plane `(a, b, c, d)`, the vertex shader assigns
`clip.z = -(a * clip.x + b * clip.y + d * clip.w) / c`. Image coordinates, UV interpolation and
size remain those of the camera-facing quad. A plane seen edge-on falls back to the image depth.
This uses ordinary depth testing and writing, without a fragment-depth extension or draw ordering.

Snap the image anchor to integer pixel boundaries, since it anchors the quad's edge. Snapping it
to pixel centres can put the bottom opaque row on the ground's depth and cause order-dependent
clipping. `setBottomPadding` must match the transparent rows below the feet in the source image.

The example game's `renderSmokeTest` compares rendered character silhouettes against unobstructed
references at house walls, throughout ascending and descending ramps, and on the bridge deck.
It also checks full and partial occlusion by foreground walls, with both submission orders.
