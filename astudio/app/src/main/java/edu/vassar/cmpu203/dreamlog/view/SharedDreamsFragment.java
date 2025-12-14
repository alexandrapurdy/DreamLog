package edu.vassar.cmpu203.dreamlog.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.vassar.cmpu203.dreamlog.databinding.FragmentSharedDreamsBinding;
import edu.vassar.cmpu203.dreamlog.databinding.SharedDreamItemBinding;
import edu.vassar.cmpu203.dreamlog.model.SharedDream;

/**
 * Fragment that lists dreams shared by the current user and friends.
 */
public class SharedDreamsFragment extends Fragment implements SharedDreamsUI {

    private FragmentSharedDreamsBinding binding;
    private SharedDreamsListener listener;
    private final SharedDreamAdapter adapter = new SharedDreamAdapter(dream -> {
        if (listener != null) {
            listener.onSharedDreamSelected(dream);
        }
    });
    private final List<SharedDream> allDreams = new ArrayList<>();
    private String activeFilter = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = FragmentSharedDreamsBinding.inflate(inflater, container, false);
        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.binding.sharedDreamsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        this.binding.sharedDreamsRecycler.setAdapter(this.adapter);

        this.binding.loadSharedDreamsButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLoadSharedDreams(binding.usernameFilter.getText().toString());
            }
        });

        this.binding.applySharedFilterButton.setOnClickListener(v -> {
            this.activeFilter = binding.sharedDreamFilter.getText().toString();
            applyFilter();
        });

        this.binding.clearSharedFilterButton.setOnClickListener(v -> {
            this.activeFilter = "";
            binding.sharedDreamFilter.setText("");
            applyFilter();
        });

        this.binding.backToMenuButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBackToMenu();
            }
        });

        if (listener != null) {
            listener.onLoadSharedDreams(this.binding.usernameFilter.getText().toString());
        }
    }

    @Override
    public void setListener(SharedDreamsListener listener) {
        this.listener = listener;
    }

    @Override
    public void showLoading(boolean loading) {
        if (binding == null) {
            return;
        }
        binding.sharedProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.loadSharedDreamsButton.setEnabled(!loading);
    }

    @Override
    public void showError(@NonNull String message) {
        if (binding == null) {
            return;
        }
        binding.sharedEmpty.setText(message);
        binding.sharedEmpty.setVisibility(message.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void updateDreamDisplay(@NonNull List<SharedDream> dreams) {
        allDreams.clear();
        allDreams.addAll(dreams);
        applyFilter();
    }

    private void applyFilter() {
        if (binding == null) {
            return;
        }

        String normalizedFilter = activeFilter == null ? "" : activeFilter.trim().toLowerCase();
        List<SharedDream> filteredDreams = new ArrayList<>();
        for (SharedDream sharedDream : allDreams) {
            if (matchesFilter(sharedDream, normalizedFilter)) {
                filteredDreams.add(sharedDream);
            }
        }

        adapter.updateData(filteredDreams);
        boolean empty = filteredDreams.isEmpty();
        binding.sharedEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            int messageId = normalizedFilter.isEmpty()
                    ? edu.vassar.cmpu203.dreamlog.R.string.no_shared_dreams
                    : edu.vassar.cmpu203.dreamlog.R.string.no_filtered_shared_dreams;
            binding.sharedEmpty.setText(getString(messageId));
        } else {
            binding.sharedEmpty.setText("");
        }
    }

    private boolean matchesFilter(@NonNull SharedDream sharedDream, @NonNull String normalizedFilter) {
        if (normalizedFilter.isEmpty()) {
            return true;
        }

        if (sharedDream.ownerUsername().toLowerCase().contains(normalizedFilter)) {
            return true;
        }

        if (sharedDream.dream().title().toLowerCase().contains(normalizedFilter)
                || sharedDream.dream().sum().toLowerCase().contains(normalizedFilter)) {
            return true;
        }

        if (sharedDream.dream().themes().stream().anyMatch(theme -> theme.toLowerCase().contains(normalizedFilter))) {
            return true;
        }

        if (sharedDream.dream().chara().stream().anyMatch(character -> character.toLowerCase().contains(normalizedFilter))) {
            return true;
        }

        return sharedDream.dream().loc().stream().anyMatch(location -> location.toLowerCase().contains(normalizedFilter));
    }

    private static class SharedDreamAdapter extends RecyclerView.Adapter<SharedDreamAdapter.ViewHolder> {

        private List<SharedDream> dreams = new LinkedList<>();
        private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        private final OnSharedDreamClickListener clickListener;

        interface OnSharedDreamClickListener {
            void onSharedDreamClicked(@NonNull SharedDream sharedDream);
        }

        SharedDreamAdapter(@NonNull OnSharedDreamClickListener clickListener) {
            this.clickListener = clickListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            SharedDreamItemBinding binding = SharedDreamItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding, dateFormat);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(dreams.get(position), clickListener);
        }

        @Override
        public int getItemCount() {
            return dreams.size();
        }

        public void updateData(@NonNull List<SharedDream> newDreams) {
            this.dreams = newDreams;
            notifyDataSetChanged();
        }

        private static class ViewHolder extends RecyclerView.ViewHolder {

            private final SharedDreamItemBinding binding;
            private final DateFormat dateFormat;

            ViewHolder(@NonNull SharedDreamItemBinding binding, @NonNull DateFormat dateFormat) {
                super(binding.getRoot());
                this.binding = binding;
                this.dateFormat = dateFormat;
            }

            void bind(@NonNull SharedDream sharedDream, @NonNull OnSharedDreamClickListener clickListener) {
                this.binding.ownerUsername.setText(sharedDream.ownerUsername());
                this.binding.sharedTitle.setText(sharedDream.dream().title());
                this.binding.sharedSummary.setText(sharedDream.dream().sum());
                String meta = String.format("Themes: %s\nCharacters: %s\nShared: %s",
                        sharedDream.dream().themesAsText(),
                        sharedDream.dream().charactersAsText(),
                        dateFormat.format(sharedDream.timestamp()));
                this.binding.sharedMeta.setText(meta);
                this.binding.getRoot().setOnClickListener(v -> clickListener.onSharedDreamClicked(sharedDream));
            }
        }
    }
}