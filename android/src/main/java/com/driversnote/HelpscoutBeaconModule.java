package com.driversnote;

import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.Callback;

import java.util.*;
import com.helpscout.beacon.Beacon;
import com.helpscout.beacon.ui.BeaconActivity;
import com.helpscout.beacon.model.BeaconScreens;
import com.helpscout.beacon.model.SuggestedArticle;

public class HelpscoutBeaconModule extends ReactContextBaseJavaModule {
    private static final String TAG = "com.driversnote.helpscoutbeacon";
    private final ReactApplicationContext reactContext;
    private Beacon beacon;

    public HelpscoutBeaconModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "HelpscoutBeacon";
    }

    @ReactMethod
    public void init(String beaconID) {
        if (TextUtils.isEmpty(beaconID)) {
            Log.w(TAG, "[init] Missing argument: beaconID");
            return;
        }
        beacon = new Beacon.Builder()
            .withBeaconId(beaconID)
            .build();
    }

    @ReactMethod
    public void identify(String email) {
        if (beacon == null || TextUtils.isEmpty(email)) {
            if (beacon == null) {
                Log.w(TAG, "[login] Not initialized - did you forget to call 'init'?");
            }
            if (TextUtils.isEmpty(email)) {
                Log.w(TAG, "[login] Missing argument: email");
            }
            return;
        }
        Beacon.identify(email);
    }

    /**
     * Use email and name field to identify
     *
     * @param email
     * @param name
     */
    @ReactMethod
    public void identifyWithEmailAndName(String email, String name) {
        if (beacon == null || TextUtils.isEmpty(email) || TextUtils.isEmpty(name)) {
            if (beacon == null) {
                Log.w(TAG, "[loginWithEmailAndName] Not initialized - did you forget to call 'init'?");
            }
            if (TextUtils.isEmpty(email)) {
                Log.w(TAG, "[loginWithEmailAndName] Missing argument: email");
            }
            if (TextUtils.isEmpty(name)) {
                Log.w(TAG, "[loginWithEmailAndName] Missing argument: name");
            }
            return;
        }
        Beacon.identify(email, name);
    }

    @ReactMethod
    public void logout() {
        if (beacon == null) {
            Log.w(TAG, "[logout] Not initialized - did you forget to call 'init'?");
            return;
        }
        Beacon.logout();
    }

    /**
     * Used to add an user attribute represented as a key / value pair.
     * There is a limit of 30 attributes per user.
     *
     * @param key String Maximum size is 80 characters
     * @param value String Maximum size is 200 characters
     */
    @ReactMethod
    public void addAttributeWithKey(String key, String value) {
        if (beacon == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            if (beacon == null) {
                Log.w(TAG, "[addAttributeWithKey] Not initialized - did you forget to call 'init'?");
            }
            if (TextUtils.isEmpty(key)) {
                Log.w(TAG, "[addAttributeWithKey] Missing argument: key");
            }
            if (TextUtils.isEmpty(value)) {
                Log.w(TAG, "[addAttributeWithKey] Missing argument: value");
            }
            return;
        }
        Beacon.addAttributeWithKey(key, value);
    }

    @ReactMethod
    public void open(String signature) {
        if (beacon == null) {
            Log.w(TAG, "[open] Not initialized - did you forget to call 'init'?");
            return;
        }

        if (TextUtils.isEmpty(signature)) {
            BeaconActivity.open(this.reactContext);
        } else {
            BeaconActivity.openInSecureMode(this.reactContext, signature);
        }
    }

    @ReactMethod
    public void openArticle(String articleID, String signature) {
        if (beacon == null || TextUtils.isEmpty(articleID) || TextUtils.isEmpty(signature)) {
            if (beacon == null) {
                Log.w(TAG, "[openArticle] Not initialized - did you forget to call 'init'?");
            }
            if (TextUtils.isEmpty(articleID)) {
                Log.w(TAG, "[openArticle] Missing argument: articleID");
            }
            if (TextUtils.isEmpty(signature)) {
                Log.w(TAG, "[openArticle] Missing argument: signature");
            }
            return;
        }

        if (TextUtils.isEmpty(signature)) {
            BeaconActivity.open(this.reactContext, BeaconScreens.ARTICLE_SCREEN, new ArrayList<>(Arrays.asList(articleID)));
        } else {
            BeaconActivity.openInSecureMode(this.reactContext, signature, BeaconScreens.ARTICLE_SCREEN, new ArrayList<>(Arrays.asList(articleID)));
        }
    }

    @ReactMethod
    public void navigate(String path) {
        if (beacon == null) {
            Log.w(TAG, "[navigate] Not initialized - did you forget to call 'init'?");
            return;
        }
        switch (path) {
            case "/ask/message/":
                BeaconActivity.open(this.reactContext, BeaconScreens.CONTACT_FORM_SCREEN, new ArrayList<String>());
                break;
            case "/ask/chat/":
                BeaconActivity.open(this.reactContext, BeaconScreens.CHAT, new ArrayList<String>());
                break;
            case "/answers/":
                BeaconActivity.open(this.reactContext, BeaconScreens.PREVIOUS_MESSAGES, new ArrayList<String>());
                break;
            default:
                Log.w(TAG, "[navigate] Path '" + (path != null ? path : "null") + "' not supported");
                break;
        }
    }

    @ReactMethod
    public void suggestArticles(ReadableArray articleIds) {
        if (beacon == null || articleIds == null) {
            if (beacon == null) {
                Log.w(TAG, "[suggestArticles] Not initialized - did you forget to call 'init'?");
            }
            if (articleIds == null) {
                Log.w(TAG, "[suggestArticles] Missing argument: articleIds");
            }
            return;
        }
        if (articleIds != null && articleIds.size() == 0) {
            Log.w(TAG, "[suggestArticles] Don't pass an empty articleIds");
            return;
        }
        if (articleIds != null && articleIds.size() >= 0 && articleIds.getType(0) != ReadableType.String) {
            Log.w(TAG, "[suggestArticles] Argument 'articleIds' must contain String values");
            return;
        }

        // There is a limit of 5 article ids. If the list is larger than 5 the additional articles will be ignored.
        List<SuggestedArticle> suggestedArticles = new ArrayList();

        for (int j = 0; j < articleIds.size(); j++) {
            if (!articleIds.isNull(j) && articleIds.getType(j) == ReadableType.String) {
                String id = articleIds.getString(j);
                suggestedArticles.add(new SuggestedArticle.SuggestedArticleWithId(id));
            }
        }

        Beacon.setOverrideSuggestedArticlesOrLinks(suggestedArticles);
    }

    @ReactMethod
    public void resetSuggestions() {
        if (beacon == null) {
            Log.w(TAG, "[resetSuggestions] Not initialized - did you forget to call 'init'?");
            return;
        }
        Beacon.setOverrideSuggestedArticlesOrLinks(new ArrayList());
    }
}
