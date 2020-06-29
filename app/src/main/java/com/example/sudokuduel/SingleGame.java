package com.example.sudokuduel;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SingleGame extends AppCompatActivity {
    private Button currentBox;
    private int currentOuterBox;
    private int currentInnerBox;
    private boolean notes;
    private Sudoku sudoku;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: Create layout here so it can be generalized to different sizes
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_game);
        sudoku = new Sudoku(3, 3);

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
                win();
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

    private void win() {
        // TODO: Make dialog fancier
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Great job!");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Return to Menu",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        returnToMenu();
                    }
                });

        builder1.setNegativeButton(
                "Play Again",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        startNewGame();
                    }
                });

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
