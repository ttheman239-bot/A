# AvatarTalk

Canvas-drawn 2D rigged humanoid that lip-syncs to Android's built-in
TextToSpeech. Entirely on-device. No network. No downloaded models.
APK size ~33 KB.

## Architecture

```
anim/
  Pose.java            — head yaw/pitch/roll, torso lean/breathe, both
                         shoulders pitch/yaw, elbows, wrist pitch/roll,
                         15 finger angles per hand (5 fingers × 3 joints),
                         mouth open/wide, eyelids, brows.
  GestureLibrary.java  — target poses: Wave, Point, Thumbs up, Clap,
                         Hands up, Arms crossed, Hands on hips, Count 1-5,
                         Relax. Each is Pose + duration + hold + release.
  PosePlayer.java      — ease-out cubic tween from current to target,
                         hold, ease-in-out release. Idle layer always
                         on: 4 s breathing, head sway, 2.5-5.5 s blinks.
speech/
  SpeechController.java — wraps android.speech.tts.TextToSpeech, picks
                          a language from default / th-TH / en-US / en,
                          reports start/end via UtteranceProgressListener.
  MouthDriver.java      — 5-12 Hz amplitude oscillator with mixed
                          closed-consonant / mid-vowel / wide-"ah"
                          phases, smoothed toward target while speaking.
view/
  AvatarView.java       — Choreographer-driven custom View. Canvas path
                          + line drawing with forward kinematics for
                          every joint, every frame.
ui/
  MainActivity.java     — layout wiring, TTS lifecycle, gesture buttons,
                          finger-count buttons, manual mouth seekbar.
```

## Running it

Install from Releases, open. No permissions prompted.

- Idle breathing + blinks start immediately.
- Type a sentence → tap **Speak** → TTS speaks and the mouth syncs.
- Tap any gesture button (Wave, Point, Thumbs up, Clap, Hands up,
  Arms crossed, Hands on hips, Relax) to run that pose.
- Tap **1 / 2 / 3 / 4 / 5** to raise the right-hand index → pinky → thumb.
- **Mute mouth** freezes lip-sync during speech.
- **Manual mouth open** seekbar overrides the mouth when not speaking.

## Why no "real" digital human?

Running wav2lip / MuseTalk / DH_live on-device needs a GPU, 50 MB+ of
model weights and an inference runtime. This repo's build environment
also can't reach Google Maven, so AndroidX / Compose / ML Kit are off
the table.

Trade-off: no photorealism. Upside: works on any Android 5.0+ device,
offline, in 33 KB, with full control over every finger — the kind of
avatar that fits a sideload-friendly APK.

## Build

Same toolchain as `../build.sh` (uses Ubuntu's packaged Android SDK).

```
sudo apt-get install -y android-sdk-platform-23 libandroid-23-java \
    aapt apksigner zipalign dalvik-exchange ecj
./build-avatar.sh
```

Output: `dist/AvatarTalk-debug.apk`.
