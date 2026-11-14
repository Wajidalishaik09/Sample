# iPhone (Apple) users — what is needed

The `.apk` app is **Android only**. iPhones cannot install it. To reach iPhone users there are two routes.

## Route 1 — Native iPhone app on the App Store (recommended for selling)
**What you need**
- **Apple Developer Program:** $99 per year (local price varies), enrolled at developer.apple.com/programs. Individual name, or Organization "MRS" (needs a D-U-N-S number).
- **A Mac with Xcode** — or a cloud Mac build (GitHub Actions macOS runner) that we set up for you.
- **App Store Connect** (appstoreconnect.apple.com):
  - **Business → Agreements**: accept the **Paid Apps Agreement**, add **bank account** and **tax forms**.
  - Join the **App Store Small Business Program** → Apple keeps **15%** (instead of 30%) while you earn under $1M a year.
- **In-App Purchase** (required by Apple for unlocking digital content — no external payment links):
  App Store Connect → your app → **In-App Purchases → +** → type **Non-Consumable**, Product ID `challenge_100`, price tier ≈ ₹199 / $3.99.
- **TestFlight** for testing (no 14-day rule like Google), then **Submit for Review** (usually 1–3 days).

**What MRS (us) would build**
- An iOS app (Swift + WKWebView) that bundles the same Namaz 100 tracker, with:
  StoreKit 2 for the ₹199 unlock and "Restore purchase", local notifications for prayer reminders,
  camera-only Masjid photo, location for prayer times and Qibla, save/share images.
- A GitHub Actions workflow that builds on a cloud Mac and uploads to TestFlight using an
  **App Store Connect API key** saved as GitHub secrets.

**Apple review note:** Apple rejects apps that are "just a website in a wrapper" (guideline 4.2).
Namaz 100 works offline with native reminders, camera, compass and in-app purchase, which helps — but approval is never guaranteed.

## Route 2 — iPhone users use the web version (no App Store)
- Host the web version publicly (e.g. Netlify) → iPhone users open it in Safari → **Share → Add to Home Screen**.
- Payments: selling outside the App Store is allowed on your own website (e.g. a Razorpay/Stripe payment link),
  but unlocking needs a small server or unlock codes, and there is no App Store visibility.
- Good for a quick start; weaker for sales.

## Recommendation
1. Launch on **Google Play first** (cheaper, most users in India/Pakistan use Android).
2. When sales start, join the Apple Developer Program and let us build the iOS app (Route 1).
