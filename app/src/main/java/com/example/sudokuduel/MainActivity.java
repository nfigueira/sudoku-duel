package com.example.sudokuduel;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private static final String TAG = "MainActivity";
    private EditText gameCode;
    private ListenerRegistration reg;
    private boolean waitingForGame;
    private String gameID;

    @Override
    protected void onDestroy() {
        if (waitingForGame) {
            reg.remove();
            final DocumentReference docRef = db.collection("private_games").document(gameID);
            docRef.delete();
        }
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        waitingForGame = false;
        gameCode = (EditText) findViewById(R.id.gameID);
    }

    public void playSingleGame(View view) {
        Intent singleGameIntent = new Intent(this, SingleGame.class);
        singleGameIntent.putExtra("gameID", "");
        startActivity(singleGameIntent);
    }

    private String createGameID() {
        String result = "";
        for (int i = 0; i < 5; i++) {
            result += (char) ('A' + Math.random() * 26);
        }
        return result;
    }

    private void startTwoPlayerGame() {
        Intent twoPlayerGameIntent = new Intent(this, SingleGame.class);
        twoPlayerGameIntent.putExtra("gameID", gameID);
        startActivity(twoPlayerGameIntent);
    }

    public void createPrivateGame(View view) {
        // Create the puzzle
        Sudoku sudoku = new Sudoku(3, 3);

        waitingForGame = true;

        // Get instance of database
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        // Create the arguments to create the game.
        Map<String, Object> data = new HashMap<>();
        data.put("done", false);
        data.put("player1", true);
        data.put("player2", false);
        data.put("puzzle", sudoku.getPuzzleFlat());
        data.put("solution", sudoku.getSolutionFlat());

        gameID = createGameID();
        final DocumentReference docRef = db.collection("private_games").document(gameID);

        // TODO: figure out how to check if the document exists and only create if it doesn't
        // Create the game in firestore
        docRef.set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "New private game successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing new private game", e);
                    }
                });

        // Create a listener to wait for opponent
        reg = docRef.addSnapshotListener(MainActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Map<String, Object> data = snapshot.getData();
                if ((boolean) data.get("player2")) {
                    startTwoPlayerGame();
                } else {
                    // TODO: something bad
                }
            } else {
                Log.d(TAG, "Current data: null");
            }
            }
        });

        // Create a dialog to tell the user the game ID
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Code: " + gameID + "\n" + "Waiting for opponent...");
        builder1.setCancelable(false);

        builder1.setNeutralButton(
            "Cancel",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                    reg.remove();
                    docRef
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Private game successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting private game", e);
                            }
                        });
                }
            });

        AlertDialog alert11 = builder1.create();
        alert11.show();



        /*
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

         */
    }

    public void joinPrivateGame(View view) {

        // Get instance of database
        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        // Find game id from edit text
        final String gameID = gameCode.getText().toString();
        if (gameID.length() != 5) {
            // TODO: Add message saying invalid code
            return;
        }
        final DocumentReference docRef = db.collection("private_games").document(gameID);

        // Set the player2 attribute in firestore
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> data = document.getData();
                        if ((boolean) data.get("player1") && !(boolean) data.get("player2")) {
                            Map<String, Object> newData = new HashMap<>();
                            newData.put("player2", true);
                            docRef.set(newData, SetOptions.merge());
                            startTwoPlayerGame();
                        }
                    } else {
                        // TODO: add text to dialog saying game not found
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        /*
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

         */
    }

    public void createOpenGame(View view) {
    }


    // TODO: Tables: Open games (with id), private games (without id)
    //  for games with friends: create game with id,
}
