# Pass Studio

An Android app for making your own Google Wallet passes for any card with a barcode, with themes that change on
their own for the seasons and holidays.

- **Passes:** scan a card's barcode (or type it), name the card, and add it to Google Wallet.
- **Themes:** 23 built-in themes (holidays, seasons, everyday scenes), or make your own from any color and art.
- **Seasons & holidays:** passes set to change on their own switch themes in Google Wallet as the year goes by.
  Pick which theme each holiday and season gets, or turn holidays off.
- **Details and links:** a Member ID line, extra lines, a Nearby locations link (Google Maps) and website links.
- **Logos:** a wide transparent PNG at the top of the pass, or a round logo, by public https link.

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

## Building

The APK builds on GitHub: every push to `main` runs [`build-apk.yml`](.github/workflows/build-apk.yml) and puts the
APK on the Releases page. Add the four signing secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`,
`KEY_PASSWORD`; the same values as Rounds work) so each build installs over the last one.
