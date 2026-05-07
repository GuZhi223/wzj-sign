package com.wzj.sign;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wzj.sign.databinding.HomeAccountItemBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeAccountAdapter extends RecyclerView.Adapter<HomeAccountAdapter.ViewHolder> {

    private final List<Account> accounts = new ArrayList<>();
    private OnDeleteListener deleteListener;
    private OnItemClickListener itemClickListener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public interface OnItemClickListener {
        void onItemClick(int position, Account account);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts.clear();
        this.accounts.addAll(accounts);
        notifyDataSetChanged();
    }

    public void addAccount(Account account) {
        accounts.add(account);
        notifyItemInserted(accounts.size() - 1);
    }

    public void updateAccount(int position, Account account) {
        if (position >= 0 && position < accounts.size()) {
            accounts.set(position, account);
            notifyItemChanged(position);
        }
    }

    public void removeAccount(int position) {
        if (position >= 0 && position < accounts.size()) {
            accounts.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, accounts.size());
        }
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public int getAccountCount() {
        return accounts.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HomeAccountItemBinding binding = HomeAccountItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Account account = accounts.get(position);
        holder.bind(account);
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final HomeAccountItemBinding binding;

        ViewHolder(HomeAccountItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Account account) {
            String uin = account.getUin();
            if (uin == null || uin.isEmpty()) {
                binding.tvUin.setText("未填写备注");
            } else {
                binding.tvUin.setText(uin);
            }

            boolean hasOpenid = account.getOpenid() != null && !account.getOpenid().isEmpty();
            binding.tvAccountStatus.setText(hasOpenid ? "就绪" : "信息不完整");

            binding.getRoot().setOnClickListener(v -> {
                if (itemClickListener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        itemClickListener.onItemClick(pos, accounts.get(pos));
                    }
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        deleteListener.onDelete(pos);
                    }
                }
            });
        }
    }
}
