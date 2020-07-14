package com.example.sudokuduel;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void playSingleGame(View view) {
        Intent singleGameIntent = new Intent(this, SingleGame.class);
        startActivity(singleGameIntent);
    }

    public void createPrivateGame(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String playerID = ""; // TODO: generate this
        String gameID = ""; // TODO: generate this too
        // TODO:do i wanna do this  here or not

        Map<String, Object> game = new HashMap<>();
        game.put("done", "");
        game.put("player1", playerID);
        game.put("player2", "");
        game.put("gameID", gameID);

        DocumentReference docRef;
        db.collection("private_games").add(game)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        docRef = documentReference; // TODO: need to generate this outside or something
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());
                    // TODO: means it's been created, so go there
                } else {
                    Log.d(TAG, "Current data: null");
                    // TODO: This means we've won? or cancelled
                }
            }
        });
    }

    public void joinPrivateGame(View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // TODO: ask for a code
        Map<String, Object> game_player_2 = new HashMap<>();
        game_player_2.put("player2", something something);

        db.collection("private_games").document(find the document)
                .set(game_player_2, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        // TODO: Start the game
    }

    public void createOpenGame(View view) {
    }


    // TODO: Tables: Open games (with id), private games (without id)
    //  for games with friends: create game with id,
}
