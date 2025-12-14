package edu.vassar.cmpu203.dreamlog.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;


import edu.vassar.cmpu203.dreamlog.R;
import edu.vassar.cmpu203.dreamlog.databinding.FragmentDreamDetailBinding;
import edu.vassar.cmpu203.dreamlog.model.Dream;


/**
 * Fragment for viewing detailed information about a dream including analysis
 */
public class DreamDetailFragment extends Fragment implements DreamDetailUI {


    private FragmentDreamDetailBinding binding;
    private DreamDetailListener dreamDetailListener;
    private Dream dream;
    private int dreamIndex;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = FragmentDreamDetailBinding.inflate(inflater);
        return this.binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (this.dreamDetailListener != null) {
            this.dreamDetailListener.onDreamDetailReady(this);
        }


        // Edit button
        this.binding.editButton.setOnClickListener(v -> {
            if (this.dreamDetailListener != null) {
                this.dreamDetailListener.onEditDream(this.dream, this.dreamIndex);
            }
        });


        // Delete button
        this.binding.deleteButton.setOnClickListener(v -> {
            if (this.dreamDetailListener != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete_dream_title)
                        .setMessage(R.string.delete_dream_message)
                        .setPositiveButton(R.string.delete, (dialog, which) ->
                                this.dreamDetailListener.onDeleteDream(this.dreamIndex))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });


        // Share button
        this.binding.shareButton.setOnClickListener(v -> {
            if (this.dreamDetailListener != null && this.dream != null) {
                this.dreamDetailListener.onShareDream(this.dream);
            }
        });

        // Back button
        this.binding.backButton.setOnClickListener(v -> {
            if (this.dreamDetailListener != null) {
                this.dreamDetailListener.onBackToLog();
            }
        });
    }


    @Override
    public void setListener(DreamDetailListener dreamDetailListener) {
        this.dreamDetailListener = dreamDetailListener;
    }

    @Override
    public void displayDream(Dream dream, int dreamIndex, String analysis) {
        this.dream = dream;
        this.dreamIndex = dreamIndex;
        this.binding.dreamTitle.setText(dream.title());
        this.binding.dreamSummary.setText(dream.sum());
        this.binding.dreamTheme.setText(dream.themesAsText());
        this.binding.dreamCharacters.setText(dream.charactersAsText());
        this.binding.dreamLocation.setText(dream.locationsAsText());
        this.binding.analysisText.setText(analysis);
    }

    @Override

    public void updateAnalysis(String analysis) {
        this.binding.analysisText.setText(analysis);
    }
}