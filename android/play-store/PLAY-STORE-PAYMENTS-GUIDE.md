# Google Play: from zero to "Unlock for ₹199" — step by step

Your app's public link (works once the app is live):
**https://play.google.com/store/apps/details?id=com.namaz100.app**
This is the link you share. People install from it, and the ₹199 payment happens inside the app through Google Play.

---

## Part A — Accounts (do once)

### A1. Google Play developer account (1–3 days)
1. Go to **play.google.com/console/signup** with the Gmail that will own the app.
2. Choose **Yourself** (personal). Developer name: **MRS**.
3. Pay **$25** (one time).
4. Verify your identity (government ID) and contact details.
5. Verify you own an Android phone: install the **Google Play Console** app on your phone and sign in when asked.

### A2. Payments profile = where your money goes (needed before you can sell)
1. Play Console → **Settings → Payments profile → Create payments profile**.
2. Enter your **legal name, address** and **tax info** (in India: PAN).
3. Open the payments settings → **Add payment method → bank account** (account in the same name).
4. Google sends a small test deposit (a few days); enter the exact amount to verify.
Payouts: around the **15th of every month** for the previous month's sales (minus Google's 15%).

---

## Part B — Build the signed app bundle (5 minutes)
1. Unzip `play-upload-key.zip` (sent in chat) and open `GITHUB-SECRETS.txt`.
2. GitHub → your repo → **Settings → Secrets and variables → Actions → New repository secret** → add the 4 secrets.
3. GitHub → **Actions → "Build Android app for Google Play" → Run workflow**.
4. When it is green, open the run → **Artifacts** → download `namaz100-play-…` → unzip → **`app-release.aab`**.

---

## Part C — Create the app in Play Console
1. **Home → Create app** → Name `Namaz 100: Salah Tracker` · Language English (US) · **App** · **Free** → accept declarations → Create.
2. **Grow → Store presence → Main store listing**: paste texts from `LISTING.md`; upload `icon-512.png`, `feature-graphic.png`, `screenshot-1…6.png`.
3. **Policy → App content**: answer every item using "App content answers" in `LISTING.md`.
   - Privacy policy URL: open your privacy-policy page → **Share → anyone with the link** → paste that link. (Replace `[your support email]` first.)

---

## Part D — Closed test + the ₹199 product
1. **Test and release → Testing → Closed testing → Create track** (or use "Alpha").
2. **Countries/regions**: add India (and any others).
3. **Testers**: create an email list with **at least 12 Gmail addresses** (friends, family, classmates).
4. **Create new release** → upload **`app-release.aab`** → accept **Play App Signing** (Google keeps the app-signing key; your upload key only proves uploads are yours) → release notes → **Review release → Start rollout to Closed testing**.
5. Copy the **"Join on Android"/"Join on the web" link** on the Testers tab and send it to your testers. They must open it, tap **Become a tester**, then install from Play.
6. **Monetize with Play → Products → One-time products** (older consoles: *In-app products*) → **Create product**:
   - Product ID: **`challenge_100`** (exactly — the app asks Play for this ID)
   - Name: `100-Day Namaz Challenge` · Description from `LISTING.md`
   - Price: **₹199** (Play converts other countries; check USA $3.99) → **Save → Activate**.
7. **Settings (all apps) → License testing** → add your Gmail and your testers' Gmails → "License response: RESPOND_NORMALLY".
   License testers see the real Google payment screen with a **test card** — no money is charged.
8. Install from the tester link → tap **Unlock for ₹199** → you will see Google's payment sheet. 🎉

---

## Part E — Go live (after 14 days)
New personal accounts need **12+ testers opted in for 14 days in a row**.
1. **Dashboard → Apply for production** → answer questions about your test (what testers did, what you fixed).
2. When approved: **Production → Countries/regions** → add countries → **Create release** (promote the tested release) → **Review → Send for review**.
3. Review takes a few days. When approved, your link is live:
   **https://play.google.com/store/apps/details?id=com.namaz100.app**
4. Share it on WhatsApp groups, masjid groups, Instagram, and in the app's share card.

## Updates later
Push changes or run the workflow → download the new `.aab` (version code increases automatically) → **Production → Create release → upload → Send for review**.
