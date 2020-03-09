package com.vinnjeru.roomdb2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionsViewHolder> {

    private Context mCtx;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context mCtx, List<Transaction> transactionList) {
        this.mCtx = mCtx;
        this.transactionList = transactionList;
    }


    @Override
    public TransactionsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mCtx).inflate(R.layout.person_card, parent, false);
        return new TransactionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionsViewHolder holder, int position) {
        Transaction t = transactionList.get(position);
        holder.textViewName.setText(t.getName());
        //holder.textViewAmount.setText("Ksh."+t.getAmount());
        holder.textViewAmount.setText(t.getAmount());
        holder.textViewFinishBy.setText(t.getFinishBy());

        if (t.isFinished()) {
            holder.textViewStatus.setText("Paid");
        }
        else {
            holder.textViewStatus.setBackgroundColor(Color.parseColor("#FFF53535"));
            holder.textViewStatus.setText("Not Paid");
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    class TransactionsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewStatus, textViewName, textViewAmount, textViewFinishBy;

        public TransactionsViewHolder(View itemView) {
            super(itemView);

            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewFinishBy = itemView.findViewById(R.id.textViewFinishBy);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Transaction transaction = transactionList.get(getAdapterPosition());

            Intent intent = new Intent(mCtx, Update.class);
            intent.putExtra("transaction", transaction);

            mCtx.startActivity(intent);
        }
    }
}
