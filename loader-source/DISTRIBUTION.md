# Socket Loader: portable distribution 1.2.0

Delivery artifact: dist/Socket loader.exe.
Only the portable EXE is required by recipients. Target: Windows 10/11 x64.

The embedded bundle contains the current core (1.2.0),
Fabric API, Iris/Sodium, Lithium, FastItems and MakeUp UltraFast 9.5g.
Manifest SHA-256 checks protect the bundle; downloads are verified before
replacement. User configs and worlds are not bundled.

First launch downloads Microsoft OpenJDK 21.0.12.1 to APPDATA/.socketclient/runtime,
then Minecraft 1.21.4, Fabric 0.18.4, libraries, native binaries and assets.
The Java archive is pinned to its official URL and SHA-256.
Required mods and the core are restored from the EXE before launching.
Default automatic core updates are off; distribution baseline 1.2.0 prevents
an older GitHub release replacing the bundled custom core.

Validation: node distribution-tests.cjs with a separate APPDATA;
full real-network installation in test-output/distribution-check;
node distribution-launch-check.cjs for the freshly installed game.
Do not run checks against the real user's APPDATA.

Build: npm run dist. Electron 44.4.3 is downloaded during normal builds; SOCKET_ELECTRON_DIST can point to a local cache.
The distribution EXE contains Electron; recipients need no Node.js or Java.
Microsoft account authentication is not implemented in the existing nickname launcher.
