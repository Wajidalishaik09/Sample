package com.namaz100.app;

/**
 * In-app purchase of the 100-Day Challenge. The Google Play build provides PlayStore
 * (android/src-play); builds without it (sideloaded APK) have everything unlocked.
 */
interface Store {
    /** Ask Play for the price and whether the challenge is already owned; reports via MainActivity.sendStoreState. */
    void refresh();
    /** Open Google Play's purchase screen. */
    void buy();
}
