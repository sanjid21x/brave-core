# Scope and verification

| Feature | State |
|---|---|
| Native jlibtorrent AAR dependency | Configured, not resolved here |
| Magnet and .torrent import | Lab code present; untested on device |
| Actual engine session / native download calls | Lab code present; untested on device |
| Live speed, progress, seed/peer counts, ETA | Lab code reads engine; host ETA tested |
| Pause/resume in session; stop; pause on completion | Lab code present; untested |
| Foreground service + notification | Lab code present; untested |
| Private staging and single-file SAF export | Lab code present; untested |
| Transfer merge contract and input validation | Host tests passed |
| Basic redacted exception logging | Lab code present |
| Actual Brave fork and app identity | Not done; bootstrap script provided |
| Unified browser download provider and magnet hook | Design only |
| Queue, file selection, persistent fast resume | Not done |
| Configurable speed/peer limits, Wi-Fi-only, seeding policy | Not done; lab uses fixed 50 connections and 64 KiB/s upload cap |
| Media detection, conversion, HTTP acceleration, full dashboard, browser UI, Shields | Not part of this torrent lab |
| APK or Android build | Not produced/verified |
