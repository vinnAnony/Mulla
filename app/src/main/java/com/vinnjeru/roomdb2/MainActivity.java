package com.vinnjeru.roomdb2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton buttonAddTask;
    private RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        buttonAddTask = findViewById(R.id.floating_button_add);
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTransaction.class);
                startActivity(intent);
            }
        });


        getTransactions();
    }

    private void getTransactions() {
        class GetTransaction extends AsyncTask<Void, Void, List<Transaction>> {

            @Override
            protected List<Transaction> doInBackground(Void... voids) {
                List<Transaction> transactionList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .transactionDao()
                        .getAll();
                return transactionList;
            }

            @Override
            protected void onPostExecute(List<Transaction> transactions) {
                super.onPostExecute(transactions);
                if (transactions.size() == 0) {
                    findViewById(R.id.tvNoT).setVisibility(View.VISIBLE);
                }
                TransactionAdapter adapter = new TransactionAdapter(MainActivity.this, transactions);
                recyclerView.setAdapter(adapter);
            }
        }

        GetTransaction gt = new GetTransaction();
        gt.execute();
    }

}
