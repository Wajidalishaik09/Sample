# Publishing Namaz 100 on Google Play — step by step

## 1. Create your developer account (1–3 days)
1. Go to play.google.com/console and sign in with the Google account you want to own the app.
2. Choose **Yourself** (personal account), pay the **$25 one-time fee**, and complete **identity verification** (government ID, and phone/address checks).
   For the **developer name** shown on Google Play, enter **MRS**. Upload `brand/mrs-logo-1024.png` as your developer page icon.
   (A personal account must still be verified with your own legal name; to publish as a registered company, choose an **Organization** account instead, which needs a D-U-N-S number.)
3. Because you sell an in-app purchase, open **Settings → Payments profile** and create a merchant/payments profile (bank account for payouts).

## 2. Add the upload key to GitHub (5 minutes, one time)
You received `play-upload-key.zip` privately in the chat. Unzip it and open `GITHUB-SECRETS.txt`.
In GitHub: **Repo → Settings → Secrets and variables → Actions → New repository secret**, and add the 4 secrets exactly as written in that file.
Keep `namaz100-upload.keystore` and the password somewhere private (e.g. your own Google Drive). Never commit them.

## 3. Build the signed bundle (automatic)
1. GitHub → **Actions → "Build Android app for Google Play" → Run workflow**.
2. When it finishes (about 5 minutes), open the run and download the artifact **namaz100-play-1.2.N**.
3. Inside: `app-release.aab` (upload this to Play) and `app-release.apk` (to test on your phone).

## 4. Create the app in Play Console
1. **Create app** → name `Namaz 100: Salah Tracker`, language English (US), **App**, **Free**.
2. **Store listing:** copy texts from `LISTING.md`, upload `icon-512.png`, `feature-graphic.png` and `screenshot-1..6.png`.
3. **App content:** answer everything using the "App content answers" section in `LISTING.md`.
   Privacy policy URL: the public link of the privacy policy page.
4. **In-app product:** first upload a bundle to a testing track (step 5), because Play only allows products once it has seen a bundle that uses billing. Then go to **Monetize with Play → Products → One-time products → Create**, ID **`challenge_100`**, price ₹199 / $3.99 (details in `LISTING.md`), and **Activate** it.
   To test buying without paying: **Settings → License testing** → add your Gmail and testers' Gmails. Their purchases are free test purchases.
5. When asked about **Play App Signing**, accept the default (Google manages the app signing key; your upload key only proves uploads are from you).

## 5. Closed testing — required for new personal accounts
New personal developer accounts must run a **closed test with at least 12 testers who stay opted in for 14 days in a row** before Google lets you apply for production.
1. **Testing → Closed testing → Create track** → upload the `.aab` → add testers' Gmail addresses (friends, family, classmates).
2. Testers accept the invite link and install the app from Play. For a paid app, testers in the testing track can install without paying.
3. Ask testers to open the app a few times and send feedback.
4. After 14 days: **Dashboard → Apply for production access**, answer the questions about your testing, then **Production → Create release** → upload the latest `.aab` → **Send for review** (review usually takes a few days).

## 6. Updates later
Push changes (or run the workflow), download the new `.aab` (its version code goes up automatically), and upload it as a new release.

## Important notes
- The app you sideloaded from the chat (signed with a different key) must be **uninstalled** before installing the Play version. **Export a backup first**, then import it in the Play version.
- The privacy policy contains `[your support email]`; replace it with a real support email before publishing.
- If you lose the upload key, Play Console lets you request an upload-key reset (Settings → App signing).
