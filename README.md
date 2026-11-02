# Pass Studio

An Android app for making your own Google Wallet passes for any card with a barcode, with themes that change on
their own for the seasons and holidays.

- **Passes:** scan a card's barcode (or type it), name the card, and add it to Google Wallet.
- **Themes:** 23 built-in themes (holidays, seasons, everyday scenes), or make your own from any color and art.
- **Seasons & holidays:** passes set to change on their own switch themes in Google Wallet as the year goes by.
  Pick which theme each holiday and season gets, or turn holidays off.
- **Details and links:** a Member ID line, extra lines, a Nearby locations link (Google Maps) and website links.
- **Full-art pass:** Pass Studio shows each pass like Google Wallet does (logo, card name, your name, barcode,
  details), with the theme's art covering the whole page. The screen goes to full brightness while it's up, and it
  works with no signal. Open it from the **quick settings tile**, a **home-screen shortcut**, or just by opening the
  app (it goes straight to your starred pass).
- **Bring back passes:** after reinstalling or moving to a new phone, Settings brings back the passes already in
  Google Wallet, still linked so their themes keep changing.
- **Logos:** a wide transparent PNG at the top of the pass, or a round logo. Pick a PNG on the phone and it's
  sized for Google Wallet and uploaded to this repo's [`logos/`](logos) folder, or paste any public https link.

## How it works

The app signs passes with your own Google Cloud service account key, which stays on your phone (encrypted with
Android's keystore, left out of backups). Adding a pass opens Google Wallet's save screen. A few times a day (and
whenever the app opens) it checks each saved pass and updates it in Google Wallet when the season or holiday changes.

Theme art for Google Wallet loads from this repo's [`art/`](art) folder, so **the repo needs to be public**. (Settings
in the app has the link if you'd rather host the art somewhere else.)

## One-time Google setup

1. **Google Pay & Wallet Console** (pay.google.com/business/console): make an issuer account for the Google Wallet
   API and copy the **Issuer ID**. Under **Set up test accounts**, add the Google account on your phone. New issuer
   accounts start in demo mode, where only test accounts can save passes, which is all a personal app needs.
2. **Google Cloud console** (console.cloud.google.com): make a project, turn on the **Google Wallet API**, and make a
   **service account**. Under its **Keys**, add a **JSON** key; the file downloads.
3. Back in the Wallet console, **Users** > Invite a user > the service account's email (`client_email` in the file)
   as a **Developer**.
4. In the app: **Settings** > type the Issuer ID, **Add key file** (pick the JSON), then **Check connection**.

## Logo uploads

Google Wallet only loads pictures from public links, so picked logos go to this repo's `logos/` folder. On GitHub,
make a fine-grained personal access token (Settings > Developer settings > Personal access tokens > Fine-grained
tokens) for only this repo, with **Contents: Read and write**. In the app: **Settings** > Logo uploads > paste it >
**Save token** > **Check GitHub**. Uploads don't start a new APK build.

## Building

The APK builds on GitHub: every push to `main` runs [`build-apk.yml`](.github/workflows/build-apk.yml) and puts the
APK on the Releases page.

To sign it, add one repository secret, `KEYSTORE_PASSWORD` (Settings > Secrets and variables > Actions > New
repository secret): the password for [`signing/release.p12`](signing), Pass Studio's own signing key. The key file is
locked with that password, so it's safe in the repo; keep the password private. Signed builds install over each other
and keep your passes and Google key.
