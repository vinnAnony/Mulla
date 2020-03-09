package com.vinnjeru.roomdb2;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class Update extends AppCompatActivity {

    private EditText editTextName, editTextAmount, editTextFinishBy;
    private CheckBox checkBoxFinished;
    private DatePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        editTextName = findViewById(R.id.editTextName);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextFinishBy = findViewById(R.id.editTextFinishBy);

        checkBoxFinished = findViewById(R.id.checkBoxFinished);


        final Transaction transaction = (Transaction) getIntent().getSerializableExtra("transaction");

        loadTransaction(transaction);

        findViewById(R.id.button_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_LONG).show();
                updateTransaction(transaction);
            }
        });

        findViewById(R.id.button_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Update.this);
                builder.setTitle("Are you sure?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteTransaction(transaction);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                AlertDialog ad = builder.create();
                ad.show();
            }
        });

        editTextFinishBy.setInputType(InputType.TYPE_NULL);
        editTextFinishBy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                picker = new DatePickerDialog(Update.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                editTextFinishBy.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                            }
                        }, year, month, day);
                picker.show();
            }
        });

    }


    private void loadTransaction(Transaction transaction) {
        editTextName.setText(transaction.getName());
        editTextAmount.setText(transaction.getAmount());
        editTextFinishBy.setText(transaction.getFinishBy());
        checkBoxFinished.setChecked(transaction.isFinished());
    }

    private void updateTransaction(final Transaction transaction) {
        final String sName = editTextName.getText().toString().trim();
        final String sAmount = editTextAmount.getText().toString().trim();
        final String sFinishBy = editTextFinishBy.getText().toString().trim();

        if (sName.isEmpty()) {
            editTextName.setError("Task required");
            editTextName.requestFocus();
            return;
        }

        if (sAmount.isEmpty()) {
            editTextAmount.setError("Desc required");
            editTextAmount.requestFocus();
            return;
        }

        if (sFinishBy.isEmpty()) {
            editTextFinishBy.setError("Finish by required");
            editTextFinishBy.requestFocus();
            return;
        }

        class UpdateTransaction extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                transaction.setName(sName);
                transaction.setAmount(sAmount);
                transaction.setFinishBy(sFinishBy);
                transaction.setFinished(checkBoxFinished.isChecked());
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .transactionDao()
                        .update(transaction);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_LONG).show();
                finishAffinity();
                startActivity(new Intent(Update.this, MainActivity.class));
            }
        }

        UpdateTransaction ut = new UpdateTransaction();
        ut.execute();
    }

    private void deleteTransaction(final Transaction transaction) {
        class DeleteTransaction extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase()
                        .transactionDao()
                        .delete(transaction);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                }
                finishAffinity();
                startActivity(new Intent(Update.this, MainActivity.class));
            }
        }

        DeleteTransaction dt = new DeleteTransaction();
        dt.execute();

    }
}
