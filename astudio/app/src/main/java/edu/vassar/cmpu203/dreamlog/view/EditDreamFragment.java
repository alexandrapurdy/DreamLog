package edu.vassar.cmpu203.dreamlog.view;


import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.google.android.material.snackbar.Snackbar;


import edu.vassar.cmpu203.dreamlog.R;
import edu.vassar.cmpu203.dreamlog.databinding.FragmentEditDreamBinding;
import edu.vassar.cmpu203.dreamlog.model.Dream;


/**
 * fragment for editing an existing dream
 */
public class EditDreamFragment extends Fragment implements EditDreamUI {


    private FragmentEditDreamBinding binding;
    private EditDreamListener editDreamListener;
    private int dreamIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        this.binding = FragmentEditDreamBinding.inflate(inflater);
        return this.binding.getRoot();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (this.editDreamListener != null) {
            this.editDreamListener.onEditDreamReady(this);
        }


        // save button
        this.binding.saveButton.setOnClickListener(v -> {
            final Editable titleEditable = this.binding.dreamTitleText.getText();
            final String titleStr = titleEditable != null ? titleEditable.toString() : "";


            final Editable sumEditable = this.binding.summaryText.getText();
            final String sumStr = sumEditable != null ? sumEditable.toString() : "";


            final Editable themeEditable = this.binding.themeText.getText();
            final String themeStr = themeEditable != null ? themeEditable.toString() : "";


            final Editable charaEditable = this.binding.characterText.getText();
            final String charaStr = charaEditable != null ? charaEditable.toString() : "";


            final Editable locEditable = this.binding.locationText.getText();
            final String locStr = locEditable != null ? locEditable.toString() : "";


            // validate input
            if (titleStr.isBlank() || sumStr.isBlank() || themeStr.isBlank() ||
                    charaStr.isBlank() || locStr.isBlank()) {
                Snackbar.make(v, R.string.missing_item_field_error, Snackbar.LENGTH_LONG).show();
                return;
            }


            if (this.editDreamListener != null) {
                this.editDreamListener.onSaveEdit(titleStr, sumStr, themeStr, charaStr, locStr,
                        this.dreamIndex);
            }
        });


        // cancel button
        this.binding.cancelButton.setOnClickListener(v -> {
            if (this.editDreamListener != null) {
                this.editDreamListener.onCancelEdit();
            }
        });
    }


    @Override
    public void setListener(EditDreamListener editDreamListener) {
        this.editDreamListener = editDreamListener;
    }

    @Override
    public void displayDreamForEdit(Dream dream, int dreamIndex) {
        this.dreamIndex = dreamIndex;


        this.binding.dreamTitleText.setText(dream.title());
        this.binding.summaryText.setText(dream.sum());
        this.binding.themeText.setText(dream.themesAsText());
        this.binding.characterText.setText(dream.charactersAsText());
        this.binding.locationText.setText(dream.locationsAsText());
    }
}