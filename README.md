# Pass Studio

An Android app for your own passes for any card with a barcode, shown full screen with art that covers the whole
page and changes on its own for the seasons and holidays. No accounts or setup: everything stays on the phone.

- **Passes:** scan a card's barcode (or type it), name the card, add your name and a logo.
- **The pass page:** logo, card name, your name and the barcode, with the theme's art covering the whole page. The
  screen goes to full brightness while it's up so the barcode scans, and it works with no signal. Scroll down for
  the details (Member ID and other lines, Nearby locations on Google Maps, website links).
- **Themes:** 23 built-in themes (holidays, seasons, everyday scenes), or make your own from any color and art.
  Tap the palette on a pass to pick one, or leave it on **Automatic** to have it change with the seasons and
  holidays. Pick which theme each holiday and season gets, or turn holidays off.
- **Logos:** pick a picture on the phone (a see-through PNG works best). It's trimmed, centered in the circle at the
  top of the pass, and kept on the phone.
- **Quick access:** star a pass, then open it from the **quick settings tile**, a **home-screen shortcut**, the
  phone's **side button** (Settings > Advanced features > Side button > Double press > Open app > Pass Studio), or
  just by opening the app.
- **Backups:** passes and logos go along with the phone's own backups and moves to a new phone.

## Building

The APK builds on GitHub: every push to `main` runs [`build-apk.yml`](.github/workflows/build-apk.yml) and puts the
APK on the Releases page.

To sign it, add one repository secret, `KEYSTORE_PASSWORD` (Settings > Secrets and variables > Actions > New
repository secret): the password for [`signing/release.p12`](signing), Pass Studio's own signing key. The key file is
locked with that password, so it's safe in the repo; keep the password private. Signed builds install over each other
and keep your passes.
