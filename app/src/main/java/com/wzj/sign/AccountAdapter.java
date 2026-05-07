package com.wzj.sign;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wzj.sign.databinding.AccountItemBinding;

import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private List<Account> accounts = new ArrayList<>();
    private OnAccountDeleteListener deleteListener;
    private OnAccountChangeListener changeListener;

    public interface OnAccountDeleteListener {
        void onAccountDelete(int position);
    }

    public interface OnAccountChangeListener {
        void onAccountChange(int position, Account account);
    }

    public void setOnAccountDeleteListener(OnAccountDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnAccountChangeListener(OnAccountChangeListener listener) {
        this.changeListener = listener;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        accounts.add(account);
        notifyItemInserted(accounts.size() - 1);
    }

    public void removeAccount(int position) {
        if (position >= 0 && position < accounts.size()) {
            accounts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, accounts.size());
        }
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AccountItemBinding binding = AccountItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new AccountViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.bind(account, position);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        private final AccountItemBinding binding;

        AccountViewHolder(AccountItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Account account, int position) {
            binding.etUin.setText(account.getUin());
            binding.etOpenid.setText(account.getOpenid());
            binding.etLongitude.setText(account.getLongitude());
            binding.etLatitude.setText(account.getLatitude());

            binding.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onAccountDelete(position);
                }
            });

            View.OnFocusChangeListener focusChangeListener = (v, hasFocus) -> {
                if (!hasFocus) {
                    Account updatedAccount = new Account(
                            binding.etUin.getText().toString().trim(),
                            binding.etOpenid.getText().toString().trim(),
                            binding.etLongitude.getText().toString().trim(),
                            binding.etLatitude.getText().toString().trim()
                    );
                    int pos = getAdapterPosition();
                    if (pos >= 0 && pos < accounts.size()) {
                        accounts.set(pos, updatedAccount);
                    }
                    if (changeListener != null) {
                        changeListener.onAccountChange(pos, updatedAccount);
                    }
                }
            };

            binding.etUin.setOnFocusChangeListener(focusChangeListener);
            binding.etOpenid.setOnFocusChangeListener(focusChangeListener);
            binding.etLongitude.setOnFocusChangeListener(focusChangeListener);
            binding.etLatitude.setOnFocusChangeListener(focusChangeListener);
        }
    }
}