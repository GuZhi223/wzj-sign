package com.wzj.sign;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.wzj.sign.databinding.DialogAccountEditBinding;

public class AccountBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_UIN = "uin";
    private static final String ARG_OPENID = "openid";

    private DialogAccountEditBinding binding;
    private OnAccountSaveListener saveListener;

    public interface OnAccountSaveListener {
        void onAccountSaved(Account account);
    }

    public static AccountBottomSheet newInstance() {
        return new AccountBottomSheet();
    }

    public static AccountBottomSheet newInstance(Account account) {
        AccountBottomSheet fragment = new AccountBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_UIN, account.getUin());
        args.putString(ARG_OPENID, account.getOpenid());
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnAccountSaveListener(OnAccountSaveListener listener) {
        this.saveListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogAccountEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        boolean isEditMode = args != null && args.containsKey(ARG_UIN);

        if (isEditMode) {
            binding.tvDialogTitle.setText("编辑账号");
            binding.etUin.setText(args.getString(ARG_UIN, ""));
            binding.etOpenid.setText(args.getString(ARG_OPENID, ""));
        }

        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnSave.setOnClickListener(v -> {
            String uin = binding.etUin.getText() != null ? binding.etUin.getText().toString().trim() : "";
            String openidInput = binding.etOpenid.getText() != null ? binding.etOpenid.getText().toString().trim() : "";

            if (uin.isEmpty()) {
                binding.etUin.setError("请输入备注");
                return;
            }
            if (openidInput.isEmpty()) {
                binding.etOpenid.setError("请输入OpenID或粘贴微助教网址");
                return;
            }

            String openid = extractOpenid(openidInput);
            if (openid == null) {
                binding.etOpenid.setError("无法识别OpenID，请检查输入");
                return;
            }

            if (saveListener != null) {
                saveListener.onAccountSaved(new Account(uin, openid, "", ""));
            }
            dismiss();
        });
    }

    private String extractOpenid(String input) {
        if (input == null || input.isEmpty()) return null;

        if (input.startsWith("http")) {
            try {
                Uri uri = Uri.parse(input);
                String openid = uri.getQueryParameter("openid");
                if (openid != null && !openid.isEmpty()) {
                    return openid;
                }
            } catch (Exception ignored) {
            }
            return null;
        }

        if (input.matches("[a-f0-9]{32}")) {
            return input;
        }

        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
