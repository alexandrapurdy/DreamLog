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


import java.util.LinkedList;
import java.util.List;


import edu.vassar.cmpu203.dreamlog.databinding.FragmentViewLogBinding;
import edu.vassar.cmpu203.dreamlog.databinding.DreamItemBinding;
import edu.vassar.cmpu203.dreamlog.model.Dream;


/**
 * fragment for viewing the dream log with filtering capabilities
 */
public class ViewLogFragment extends Fragment implements ViewLogUI {


    private FragmentViewLogBinding binding;
    private ViewLogListener viewLogListener;
    private final DreamAdapter dreamAdapter = new DreamAdapter();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = FragmentViewLogBinding.inflate(inflater);
        return this.binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // set up RecyclerView
        this.binding.dreamsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this.getContext()));
        this.binding.dreamsRecyclerView.setAdapter(this.dreamAdapter);


        // filter button
        this.binding.filterButton.setOnClickListener(v -> {
            String filterText = this.binding.filterText.getText().toString();
            if (ViewLogFragment.this.viewLogListener != null) {
                ViewLogFragment.this.viewLogListener.onFilter(filterText);
            }
        });


        // clear filter button
        this.binding.clearFilterButton.setOnClickListener(v -> {
            this.binding.filterText.setText("");
            if (ViewLogFragment.this.viewLogListener != null) {
                ViewLogFragment.this.viewLogListener.onClearFilter();
            }
        });


        // back button
        this.binding.backButton.setOnClickListener(v -> {
            if (ViewLogFragment.this.viewLogListener != null) {
                ViewLogFragment.this.viewLogListener.onBackToMenu();
            }
        });


        // notify listener that view is ready
        if (this.viewLogListener != null) {
            this.viewLogListener.onViewLogReady(this);
        }
    }


    @Override
    public void setListener(ViewLogListener viewLogListener) {
        this.viewLogListener = viewLogListener;
    }


    @Override
    public void updateDreamDisplay(List<Dream> dreams) {
        this.dreamAdapter.updateData(dreams);
        if (dreams.isEmpty()) {
            this.binding.emptyMessage.setVisibility(View.VISIBLE);
            this.binding.dreamsRecyclerView.setVisibility(View.GONE);
        } else {
            this.binding.emptyMessage.setVisibility(View.GONE);
            this.binding.dreamsRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * adapter for displaying dreams in a RecyclerView
     */
    private class DreamAdapter extends RecyclerView.Adapter<DreamAdapter.ViewHolder> {


        private List<Dream> dreams = new LinkedList<>();


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            DreamItemBinding binding = DreamItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(dreams.get(position), position);
        }


        @Override
        public int getItemCount() {
            return dreams.size();
        }


        public void updateData(@NonNull List<Dream> dreams) {
            this.dreams = dreams;
            this.notifyDataSetChanged();
        }


        /**
         * ViewHolder for individual dream items
         */
        private class ViewHolder extends RecyclerView.ViewHolder {


            private final DreamItemBinding binding;


            public ViewHolder(@NonNull DreamItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }


            public void bind(Dream dream, int position) {
                this.binding.dreamTitle.setText(dream.title());
                this.binding.dreamSummary.setText(dream.sum());
                this.binding.dreamTheme.setText("Themes: " + dream.themesAsText());
                this.binding.dreamCharacters.setText("Characters: " + dream.charactersAsText());
                this.binding.dreamLocation.setText("Locations: " + dream.locationsAsText());


                // set click listener for the entire card
                this.binding.getRoot().setOnClickListener(v -> {
                    if (ViewLogFragment.this.viewLogListener != null) {
                        ViewLogFragment.this.viewLogListener.onDreamClicked(dream, position);
                    }
                });
            }
        }
    }
}