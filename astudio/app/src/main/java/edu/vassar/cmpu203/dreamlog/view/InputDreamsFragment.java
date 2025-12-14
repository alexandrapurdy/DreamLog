package edu.vassar.cmpu203.dreamlog.view;


import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.google.android.material.snackbar.Snackbar;


import edu.vassar.cmpu203.dreamlog.R;
import edu.vassar.cmpu203.dreamlog.databinding.FragmentInputDreamsBinding;
/**
 * Implements the InputDreamsUI interface
 */
public class InputDreamsFragment extends Fragment implements InputDreamsUI {


    private FragmentInputDreamsBinding binding;
    private InputDreamsListener inputDreamsListener;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        this.binding = FragmentInputDreamsBinding.inflate(inflater);
        return this.binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // attach click listener to add button
        this.binding.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // get dream's title
                final Editable titleEditable = InputDreamsFragment.this.binding.dreamTitleText.getText();
                final String titleStr = titleEditable != null ? titleEditable.toString() : "";


                // get dream's summary
                final Editable sumEditable = InputDreamsFragment.this.binding.summaryText.getText();
                final String sumStr = sumEditable != null ? sumEditable.toString() : "";


                // get dream's theme
                final Editable themeEditable = InputDreamsFragment.this.binding.themeText.getText();
                final String themeStr = themeEditable != null ? themeEditable.toString() : "";


                // get dream's characters
                final Editable charaEditable = InputDreamsFragment.this.binding.characterText.getText();
                final String charaStr = charaEditable != null ? charaEditable.toString() : "";


                // get dream's locations
                final Editable locEditable = InputDreamsFragment.this.binding.locationText.getText();
                final String locStr = locEditable != null ? locEditable.toString() : "";


                // check that we have all required info
                if (titleStr.isBlank() || sumStr.isBlank() || themeStr.isBlank() ||
                        charaStr.isBlank() || locStr.isBlank()) {
                    Snackbar.make(v, R.string.missing_item_field_error, Snackbar.LENGTH_LONG).show();
                    return;
                }


                if (InputDreamsFragment.this.inputDreamsListener != null) {
                    // notify listener
                    InputDreamsFragment.this.inputDreamsListener.onAddDream(titleStr, sumStr, themeStr,
                            charaStr, locStr);
                }


                // clear input
                if (titleEditable != null) titleEditable.clear();
                if (sumEditable != null) sumEditable.clear();
                if (themeEditable != null) themeEditable.clear();
                if (charaEditable != null) charaEditable.clear();
                if (locEditable != null) locEditable.clear();
            }
        });


        // attach click listener to cancel/done button
        this.binding.doneButton.setOnClickListener((v) -> {
            if (InputDreamsFragment.this.inputDreamsListener != null)
                InputDreamsFragment.this.inputDreamsListener.onDoneAddingDream();
        });
    }


    @Override
    public void setListener(final InputDreamsListener inputDreamsListener) {
        this.inputDreamsListener = inputDreamsListener;
    }
}
