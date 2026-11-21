# Corresponding source for locally built dependencies

Archives are exact `git archive` snapshots at pins listed in `bitchord/PROVENANCE.md`. Their contained licenses apply. Maven artifacts live at `../maven`. The application and host changes are source directories in this repository, not prebuilt opaque APKs.

For NewPipeExtractor, unpack its archive, apply `NewPipeExtractor-build.patch`, and replace the patch's local repository URL with the absolute path to this repository's `third_party/maven`. Build `:extractor:jar` using JDK 21 and the included wrapper. The patch changes compiler tooling and dependency lookup only, retaining Java 11 source/target. Copy the jar to the corresponding Maven coordinate (POM already checked in). NewPipe's protobuf compiler and regular dependencies resolve from Maven Central.

For nanojson, compile `src/main/java` with `javac -source 11 -target 11`, then `jar --create` from the classes directory. There are no runtime dependencies. Use the exact e9d656d archive, not current main.

For InnerTubeX, unpack its archive and use JDK 21 + Android SDK 37, then run its wrapper: `allTests ktlintCheck apiCheck assemble publishToMavenLocal`. Copy the resulting `com/github/MetrolistGroup/innertubex*` publications from Maven Local into `third_party/maven`. No functional source patch was used. Source publication coordinates differ from JitPack's group, so the host consumes the real source publication group. Javadoc-only jars are not needed to compile the application and are omitted.

For the bundled audio decoder, unpack librempeg's exact source or let `bitchord/native/ffmpeg/build.sh` fetch its pinned commit; set ANDROID_NDK to an installed NDK and JOBS to a reasonable parallelism. That script rebuilds the JNI decoder from the supplied native/ffmpeg/ffmpeg_jni.cc and the pinned GPLv3 codec configuration. CMake builds the other native audio libraries from `bitchord/native` plus Oboe/libusb at the hashes in CMakeLists.txt. Source archives for these CMake dependencies are supplied too; use FetchContent source directory overrides to rebuild offline.
