# Surface rooms

A surface room is a room folded out of several walking planes that share one fixed X/Y/Z tile
grid. It lets an actor walk on a wall or a ceiling without a second coordinate system, a physics
step, or a separate renderer.

## Pieces

- `GravityState` — the four planes (ground, west wall, east wall, ceiling). A state is three
  integer axes: the surface normal, the world step of `MoveIntent.UP`, and the world step of
  `MoveIntent.RIGHT`, derived as `forward × normal` so every state is a proper rotation of the
  ground. Walls are the only states that walk along Y; ground and ceiling never change altitude.
- `SurfacePlatform` — an ordinary `LoadedMap` placed as one plane. Its own tile map, terrain
  heights and collision stay authoritative; only the placement rotates. Local X follows the plane's
  right axis and local Z its backward axis, so a map authored for the ground reads the same way on
  a wall and is never mirrored.
- `SurfaceRoom` — the platforms plus the seams between them. It implements
  `GridActor.MovementSpace`, so an actor keeps its usual speed, input cadence and walk animation.
- `SurfaceRoomScene` — builds each plane's geometry with the normal `ChunkMesher` and places it.
- `SurfaceCamera` — the framing, see below.

## Seams

Walking inside a plane is the plane's own business. Crossing to another one is the only place
gravity changes, and it happens where the planes touch or across an authored seam where they do
not.

Seams have to be authored in both directions, and that is not an oversight: the step that leaves a
plane points along the normal the actor is about to gain, and no input maps to a plane's own
normal. So leaving the ground westwards is a `LEFT`, while coming back down the wall is a `DOWN`.

`stepArc` lifts a step that changes plane along the sum of both normals, which clears the corner
instead of dragging the actor through it, and reads as a hop when the planes stand apart.

## Framing

`SurfaceCamera` keeps the world's up axis and never rolls — pillars stay vertical on screen no
matter which plane is walked on. Two things change instead:

- **Where it looks from.** The camera always moves to the free side of the current plane, so a
  ceiling is watched from below. The framing is built from the plane's own right axis, which is
  what keeps the controls honest: the right-hand input always moves the actor right across the
  screen, on every plane.
- **How the sprite stands.** `spriteBasis` turns the billboard's axes in the image plane until its
  up points the way the plane's normal points on screen. Under a ceiling that is half a turn, which
  hangs the sprite upside down. The turn is interpolated as an angle rather than as a vector, so
  turning fully over stays defined.

Changing planes slerps the orientation and the sprite's turn over a short blend. Nothing else
animates — the world itself never moves.

`BillboardRenderer.setBasis` carries those axes into the quad's world transform, so each sprite can
stand on its own plane. The ordinary field camera passes its own right and up axes and gets the
behaviour it always had.
