# Third-party materials and source availability

The MIT grant covers original project contributions and Socket Loader code.
It does not change the licenses or ownership of upstream components.

## Distributed components

| Component | Version | License | Upstream source |
|---|---|---|---|
| Fabric API | 0.119.4+1.21.4 | Apache-2.0 | https://github.com/FabricMC/fabric |
| FastItems | 1.1.0 | CC0-1.0 | https://github.com/Noryea/fast-items |
| Iris | 1.8.8 | LGPL-3.0-only | https://github.com/IrisShaders/Iris |
| Lithium | 0.15.3 | LGPL-3.0-only | https://github.com/CaffeineMC/lithium-fabric |
| Sodium | 0.6.13 | Polyform-Shield-1.0.0 | https://github.com/CaffeineMC/sodium |
| MakeUp UltraFast | 9.5g | See third-party/MakeUp-LICENSE.txt | https://modrinth.com/shader/makeup-ultra-fast |

The original license notices remain inside distributed dependency JARs.
Pinned binary hashes are recorded in loader-source/bundle-lock.json.
Dependencies are downloaded during packaging; their compiled files are excluded
from Git. Java and Minecraft binaries/resources are downloaded at runtime.

## Artwork, sounds and fonts

The menu sounds are Genshin Impact effects owned by HoYoverse; see
PAIMON-MENU-SOUNDS.md for the archive source. The menu character illustration
was supplied by the project owner and converted to a transparent cutout.
These assets and existing cosmetic textures/fonts are not covered by MIT.
This repository does not assert ownership of third-party game assets.
