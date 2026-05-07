package com.wzj.sign;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wzj.sign.databinding.FragmentLogBinding;
import com.wzj.sign.log.LogEntry;
import com.wzj.sign.log.SignLogger;

public class LogFragment extends Fragment implements SignLogger.OnLogListener {

    private FragmentLogBinding binding;
    private LogAdapter adapter;
    private SignLogger logger;
    private boolean isAtBottom = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        logger = SignLogger.getInstance(requireContext());
        adapter = new LogAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.rvLog.setLayoutManager(layoutManager);
        binding.rvLog.setAdapter(adapter);

        binding.rvLog.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isAtBottom = !recyclerView.canScrollVertically(1);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                isAtBottom = !recyclerView.canScrollVertically(1);
            }
        });

        adapter.setEntries(logger.getEntries());
        if (adapter.getItemCount() > 0) {
            binding.rvLog.scrollToPosition(adapter.getItemCount() - 1);
        }

        logger.addListener(this);

        binding.btnClearLog.setOnClickListener(v -> {
            logger.clear();
            adapter.clear();
        });

        binding.btnExportLog.setOnClickListener(v -> {
            String path = logger.exportToFile();
            if (path != null) {
                Toast.makeText(requireContext(), "日志已导出: " + path, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(requireContext(), "日志导出失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onNewLog(LogEntry entry) {
        if (adapter == null || binding == null) return;
        adapter.addEntry(entry);
        if (isAtBottom) {
            binding.rvLog.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    @Override
    public void onDestroyView() {
        if (logger != null) {
            logger.removeListener(this);
        }
        binding = null;
        adapter = null;
        super.onDestroyView();
    }
}
