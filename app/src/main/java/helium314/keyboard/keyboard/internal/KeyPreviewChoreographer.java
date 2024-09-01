/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package helium314.keyboard.keyboard.internal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import helium314.keyboard.keyboard.Key;
import helium314.keyboard.latin.R;
import helium314.keyboard.latin.common.ColorType;
import helium314.keyboard.latin.common.Colors;
import helium314.keyboard.latin.common.CoordinateUtils;
import helium314.keyboard.latin.settings.Settings;
import helium314.keyboard.latin.utils.ViewLayoutUtils;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * This class controls pop up key previews. This class decides:
 * - what kind of key previews should be shown.
 * - where key previews should be placed.
 * - how key previews should be shown and dismissed.
 */
public final class KeyPreviewChoreographer {
    // Free {@link KeyPreviewView} pool that can be used for key preview.
    private final ArrayDeque<KeyPreviewView> mFreeKeyPreviewViews = new ArrayDeque<>();
    private final ArrayDeque<View> mFreeFlickKeyPreviewViews = new ArrayDeque<>();
    // Map from {@link Key} to {@link KeyPreviewView} that is currently being displayed as key
    // preview.
    private final HashMap<Key,KeyPreviewView> mShowingKeyPreviewViews = new HashMap<>();
    private final HashMap<Key,View> mShowingFlickKeyPreviewViews = new HashMap<>();

    private final KeyPreviewDrawParams mParams;

    public KeyPreviewChoreographer(final KeyPreviewDrawParams params) {
        mParams = params;
    }

    public View getKeyPreviewView(final Key key, final ViewGroup placerView) {
        if (key.isFlick()) {
            View flickKeyPreviewView = mShowingFlickKeyPreviewViews.remove(key);
            if (flickKeyPreviewView != null) {
                return flickKeyPreviewView;
            }
            flickKeyPreviewView = mFreeFlickKeyPreviewViews.poll();
            if (flickKeyPreviewView != null) {
                return flickKeyPreviewView;
            }
            final Context context = placerView.getContext();
            flickKeyPreviewView = new View(context, null /* attrs */);
            flickKeyPreviewView.setBackgroundResource(mParams.mPreviewBackgroundResId);
            placerView.addView(flickKeyPreviewView, ViewLayoutUtils.newLayoutParam(placerView, 0, 0));
            return flickKeyPreviewView;
        } else {
            KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.remove(key);
            if (keyPreviewView != null) {
                return keyPreviewView;
            }
            keyPreviewView = mFreeKeyPreviewViews.poll();
            if (keyPreviewView != null) {
                return keyPreviewView;
            }
            final Context context = placerView.getContext();
            keyPreviewView = new KeyPreviewView(context, null /* attrs */);
            keyPreviewView.setBackgroundResource(mParams.mPreviewBackgroundResId);
            placerView.addView(keyPreviewView, ViewLayoutUtils.newLayoutParam(placerView, 0, 0));
            return keyPreviewView;
        }
    }

    public boolean isShowingKeyPreview(final Key key) {
        if (key.isFlick()) {
            return mShowingFlickKeyPreviewViews.containsKey(key);
        } else {
            return mShowingKeyPreviewViews.containsKey(key);
        }
    }

    public void dismissKeyPreview(final Key key) {
        if (key == null) {
            return;
        }
        if (key.isFlick()) {
            final View flickKeyPreviewView = mShowingFlickKeyPreviewViews.get(key);
            if (flickKeyPreviewView == null) {
                return;
            }
            // Dismiss preview
            mShowingFlickKeyPreviewViews.remove(key);
            flickKeyPreviewView.setTag(null);
            flickKeyPreviewView.setVisibility(View.INVISIBLE);
            mFreeFlickKeyPreviewViews.add(flickKeyPreviewView);
        } else {
            final KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.get(key);
            if (keyPreviewView == null) {
                return;
            }
            // Dismiss preview
            mShowingKeyPreviewViews.remove(key);
            keyPreviewView.setTag(null);
            keyPreviewView.setVisibility(View.INVISIBLE);
            mFreeKeyPreviewViews.add(keyPreviewView);
        }
    }

    public void placeAndShowKeyPreview(final Key key, final KeyboardIconsSet iconsSet,
            final KeyDrawParams drawParams, final int fullKeyboardViewWidth, final int[] keyboardOrigin,
            final ViewGroup placerView) {
        if (key.isFlick()) {
            final View view = createAndPlaceFlickKeyPreview(key, placerView, keyboardOrigin, drawParams);
            showFlickKeyPreview(key, view);
        } else {
            final KeyPreviewView keyPreviewView = (KeyPreviewView) getKeyPreviewView(key, placerView);
            placeKeyPreview(key, keyPreviewView, iconsSet, drawParams, fullKeyboardViewWidth, keyboardOrigin);
            showKeyPreview(key, keyPreviewView);
        }
    }

    private void placeKeyPreview(final Key key, final KeyPreviewView keyPreviewView,
            final KeyboardIconsSet iconsSet, final KeyDrawParams drawParams,
            final int fullKeyboardViewWidth, final int[] originCoords) {
        keyPreviewView.setPreviewVisual(key, iconsSet, drawParams);
        keyPreviewView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mParams.setGeometry(keyPreviewView);
        final int previewWidth = keyPreviewView.getMeasuredWidth();
        final int previewHeight = keyPreviewView.getMeasuredHeight();
        final int keyDrawWidth = key.getDrawWidth();
        // The key preview is horizontally aligned with the center of the visible part of the
        // parent key. If it doesn't fit in this {@link KeyboardView}, it is moved inward to fit and
        // the left/right background is used if such background is specified.
        final int keyPreviewPosition;
        int previewX = key.getDrawX() - (previewWidth - keyDrawWidth) / 2 + CoordinateUtils.x(originCoords);
        if (previewX < 0) {
            previewX = 0;
            keyPreviewPosition = KeyPreviewView.POSITION_LEFT;
        } else if (previewX > fullKeyboardViewWidth - previewWidth) {
            previewX = fullKeyboardViewWidth - previewWidth;
            keyPreviewPosition = KeyPreviewView.POSITION_RIGHT;
        } else {
            keyPreviewPosition = KeyPreviewView.POSITION_MIDDLE;
        }
        final boolean hasPopupKeys = (key.getPopupKeys() != null);
        keyPreviewView.setPreviewBackground(hasPopupKeys, keyPreviewPosition);
        final Colors colors = Settings.getInstance().getCurrent().mColors;
        colors.setBackground(keyPreviewView, ColorType.KEY_PREVIEW);

        // The key preview is placed vertically above the top edge of the parent key with an
        // arbitrary offset.
        final int previewY = key.getY() - previewHeight + key.getHeight() - mParams.mPreviewOffset
                + CoordinateUtils.y(originCoords);

        ViewLayoutUtils.placeViewAt(keyPreviewView, previewX, previewY, previewWidth, previewHeight);
        keyPreviewView.setPivotX(previewWidth / 2.0f);
        keyPreviewView.setPivotY(previewHeight);
    }

    View createAndPlaceFlickKeyPreview(final Key key, final ViewGroup placerView, final int [] keyboardOrigin, final KeyDrawParams drawParams) {
        Key.Direction direction = key.getFlickDirection();
        LayoutInflater li = LayoutInflater.from(placerView.getContext());
        View v = li.inflate(R.layout.keyboard_preview_flickkey, null);
        switch (direction) {
            case CENTER -> {
                if (key.getFlickLeft() != null) {
                    ((TextView)v.findViewById(R.id.textLeft)).setText(key.getFlickLeft().mLabel);
                    ((TextView)v.findViewById(R.id.textLeft)).setTextColor(drawParams.mPreviewTextColor);
                }
                if (key.getFlickUp() != null){
                    ((TextView)v.findViewById(R.id.textTop)).setText(key.getFlickUp().mLabel);
                    ((TextView)v.findViewById(R.id.textTop)).setTextColor(drawParams.mPreviewTextColor);
                }
                if (key.getFlickRight() != null) {
                    ((TextView)v.findViewById(R.id.textRight)).setText(key.getFlickRight().mLabel);
                    ((TextView)v.findViewById(R.id.textRight)).setTextColor(drawParams.mPreviewTextColor);
                }
                if (key.getFlickDown() != null) {
                    ((TextView)v.findViewById(R.id.textBottom)).setText(key.getFlickDown().mLabel);
                    ((TextView)v.findViewById(R.id.textBottom)).setTextColor(drawParams.mPreviewTextColor);
                }
                if (key.getFlickCenter() != null){
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickCenter().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
            }
            case LEFT -> {
                if (key.getFlickLeft() != null) {
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickLeft().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
                else if (key.getFlickCenter() != null){
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickCenter().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
            }
            case UP -> {
                if (key.getFlickUp() != null) {
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickUp().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);}
                else if (key.getFlickCenter() != null){
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickCenter().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
            }
            case RIGHT -> {
                if (key.getFlickRight() != null) {
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickRight().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
                else if (key.getFlickCenter() != null){
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickCenter().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
            }
            case DOWN -> {
                if (key.getFlickDown() != null) {
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickDown().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
                else if (key.getFlickCenter() != null){
                    ((TextView)v.findViewById(R.id.textCenter)).setText(key.getFlickCenter().mLabel);
                    ((TextView)v.findViewById(R.id.textCenter)).setTextColor(drawParams.mPreviewTextColor);
                }
            }
        }

        v.measure(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mParams.setGeometry(v);
        int x = key.getDrawX() - (v.getMeasuredWidth() - key.getDrawWidth()) / 2
                + CoordinateUtils.x(keyboardOrigin);
        int y =
                key.getY() + CoordinateUtils.y(keyboardOrigin) - mParams.getVisibleHeight() - key.getHeight() / 2;
        placerView.addView(v, ViewLayoutUtils.newLayoutParam(placerView, 0, 0));
        ViewLayoutUtils.placeViewAt(
                v, x, y, v.getMeasuredWidth(), mParams.getVisibleHeight());
        v.setPivotX(v.getMeasuredWidth() / 2.0f);
        v.setPivotY(mParams.getVisibleHeight());

        return v;
    }

    void showKeyPreview(final Key key, final KeyPreviewView keyPreviewView) {
        keyPreviewView.setVisibility(View.VISIBLE);
        mShowingKeyPreviewViews.put(key, keyPreviewView);
    }

    void showFlickKeyPreview(final Key key, final View flickKeyPreviewView) {
        flickKeyPreviewView.setVisibility(View.VISIBLE);
        mShowingFlickKeyPreviewViews.put(key, flickKeyPreviewView);
    }

}
