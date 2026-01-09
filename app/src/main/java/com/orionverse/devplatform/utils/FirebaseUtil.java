package com.orionverse.devplatform.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtil {
    private static FirebaseAuth auth;
    private static FirebaseFirestore firestore;
    private static FirebaseStorage storage;

    // Firebase Auth
    public static FirebaseAuth getAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        return auth;
    }

    // Firestore
    public static FirebaseFirestore getFirestore() {
        if (firestore == null) {
            firestore = FirebaseFirestore.getInstance();
        }
        return firestore;
    }

    // Storage
    public static FirebaseStorage getStorage() {
        if (storage == null) {
            storage = FirebaseStorage.getInstance();
        }
        return storage;
    }

    // Collection References
    public static CollectionReference getUsersCollection() {
        return getFirestore().collection("users");
    }

    public static CollectionReference getPostsCollection() {
        return getFirestore().collection("posts");
    }

    public static CollectionReference getApplicationsCollection() {
        return getFirestore().collection("applications");
    }

    public static CollectionReference getRatingsCollection() {
        return getFirestore().collection("ratings");
    }

    // Storage References
    public static StorageReference getProfileImagesRef() {
        return getStorage().getReference().child("profile_images");
    }

    // Current User ID
    public static String getCurrentUserId() {
        return getAuth().getCurrentUser() != null ? 
               getAuth().getCurrentUser().getUid() : null;
    }

    // Check if user is logged in
    public static boolean isUserLoggedIn() {
        return getAuth().getCurrentUser() != null;
    }
}
