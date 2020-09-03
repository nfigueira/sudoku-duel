package com.example.sudokuduel;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SingleGame extends AppCompatActivity {
    private static final String TAG = "SingleGame";
    private Button currentBox;
    private int currentOuterBox;
    private int currentInnerBox;
    private boolean notes;
    private Sudoku sudoku;
    private boolean twoPlayerGame;
    private FirebaseFirestore db;
    private ListenerRegistration reg;
    private String gameID;

    @Override
    protected void onDestroy() {
        final DocumentReference docRef = db.collection("private_games").document(gameID);
        docRef.delete();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Create layout here so it can be generalized to different sizes
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_game);

        Intent intent = getIntent();
        gameID = intent.getStringExtra("gameID");
        if (gameID.equals("")) {
            sudoku = new Sudoku(3, 3);
            twoPlayerGame = false;
            setup();
        } else {
            twoPlayerGame = true;
            db = FirebaseFirestore.getInstance();
            final DocumentReference docRef = db.collection("private_games").document(gameID);

            // Listen for opposing win or disconnect
            reg = docRef.addSnapshotListener(SingleGame.this, new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Map<String, Object> data = snapshot.getData();
                        if ((boolean) data.get("done")) {
                            lose();
                        } else {
                            Log.d(TAG, "hiiii");
                        }
                    } else if (snapshot != null){
                        Log.d(TAG, "Current data: null");
                        win(true);
                    }
                }
            });

            Log.d(TAG, "hello");
            // Get sudoku data
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> data = document.getData();
                            Log.d(TAG, "hi");
                            setSudoku((List<Long>) data.get("puzzle"), (List<Long>) data.get("solution"));
                            setup();
                        } else {
                            // TODO: problem
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }
    }

    private void setup() {
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                String id = "box" + i + j;
                int value = sudoku.getPuzzle(i, j);
                Button box = findViewById(getResources().
                        getIdentifier(id, "id", getPackageName()));
                if (value != 0) {
                    box.setText(Integer.toString(value));
                } else {
                    box.setTextColor(Color.rgb(45,140,237));
                    box.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            currentBox.setBackgroundResource(R.drawable.inner_border);
                            currentBox = (Button) view;
                            currentBox.setBackgroundColor(Color.LTGRAY);
                            // TODO: Change IDs when creating layout above so string manipulation isn't necessary
                            String id = view.getResources().getResourceEntryName(currentBox.getId());
                            currentOuterBox = id.charAt(3) - '0';
                            currentInnerBox = id.charAt(4) - '0';
                        }
                    });
                    if (currentBox == null) {
                        currentBox = box;
                        currentOuterBox = i;
                        currentInnerBox = j;
                        currentBox.setBackgroundColor(Color.LTGRAY);
                    }
                }
            }
        }

        notes = false;
    }

    private void setSudoku(List<Long> puzzle, List<Long> solution) {
        sudoku = new Sudoku(puzzle, solution);
    }

    public void assignNumber(View view) {
        String number = ((Button) view).getText().toString();
        if (notes) {
            sudoku.setPuzzle(currentOuterBox, currentInnerBox, 0);
            int num = Integer.parseInt(number);
            String currentText = currentBox.getText().toString();
            StringBuilder newText = new StringBuilder();

            boolean used = false;
            for (int i = 0; i < currentText.length(); i += 3) {
                if (!used && currentText.charAt(i) - '0' > num) {
                    newText.append(number);
                    newText.append("  ");
                    used = true;
                }
                if (currentText.charAt(i) - '0' != num) {
                    newText.append(currentText.charAt(i));
                    newText.append("  ");;
                } else {
                    used = true;
                }
            }
            if (!used) {
                newText.append(number);
                newText.append("  ");
            }
            if (newText.length() > 0) {
                newText.setLength(newText.length() - 2);
            }
            currentBox.setText(newText.toString());
            currentBox.setTextSize(9);
        } else {
            // TODO: Make sure sizes work for all screen sizes
            currentBox.setText(number);
            currentBox.setTextSize(25);
            if (sudoku.setPuzzle(currentOuterBox, currentInnerBox, Integer.parseInt(number))) {
                win(false);
            }
        }
    }

    public void erase(View view) {
        currentBox.setText("");
        sudoku.setPuzzle(currentOuterBox, currentInnerBox, 0);
    }

    public void toggle_notes(View view) {
        notes = !notes;
    }

    private void lose() {
        // can only happen in a two player game
        reg.remove();

        // delete the game from the database
        final DocumentReference docRef = db.collection("private_games").document(gameID);
        docRef.delete();

        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("You lost! You suck!");
        builder1.setCancelable(true);

        builder1.setNeutralButton(
                "Return to Menu",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        returnToMenu();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void win(boolean disconnect) {
        // if it's a two player game, update database to let loser know
        if (twoPlayerGame) {
            reg.remove();
            if (!disconnect) {
                final DocumentReference docRef = db.collection("private_games").document(gameID);

                Map<String, Object> newData = new HashMap<>();
                newData.put("done", true);
                docRef.set(newData, SetOptions.merge());
            }
        }

        // TODO: Make dialog fancier
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Great job!");
        builder1.setCancelable(true);

        builder1.setNeutralButton(
                "Return to Menu",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        returnToMenu();
                    }
                });
        /* taking this out because i don't want it for 2 player games
        builder1.setNegativeButton(
                "Play Again",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        startNewGame();
                    }
                });

         */

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void returnToMenu() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
    }

    private void startNewGame() {
        Intent game = new Intent(this, SingleGame.class);
        startActivity(game);
    }
}
