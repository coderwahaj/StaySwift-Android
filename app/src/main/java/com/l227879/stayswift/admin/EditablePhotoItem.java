package com.l227879.stayswift.admin;

import android.net.Uri;

public class EditablePhotoItem {
    public enum Type { EXISTING_URL, NEW_URI }

    public Type type;
    public String url;
    public Uri uri;

    public EditablePhotoItem(String url) {
        this.type = Type.EXISTING_URL;
        this.url = url;
    }

    public EditablePhotoItem(Uri uri) {
        this.type = Type.NEW_URI;
        this.uri = uri;
    }
}