package edu.vassar.cmpu203.dreamlog.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import java.text.DateFormat;

import edu.vassar.cmpu203.dreamlog.R;
import edu.vassar.cmpu203.dreamlog.databinding.FragmentSharedDreamDetailBinding;
import edu.vassar.cmpu203.dreamlog.model.SharedDream;

public class SharedDreamDetailFragment extends Fragment implements SharedDreamDetailUI {

    private FragmentSharedDreamDetailBinding binding;
    private SharedDreamDetailListener listener;
    private SharedDream sharedDream;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = FragmentSharedDreamDetailBinding.inflate(inflater, container, false);
        return this.binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.binding.backToSharedListButton.setOnClickListener(v -> {
            if (this.listener != null) {
                this.listener.onBackToSharedList();
            }
        });

        this.binding.unshareButton.setOnClickListener(v -> {
            if (this.listener != null && this.sharedDream != null) {
                this.listener.onUnshareDream(this.sharedDream.dream());
            }
        });

        if (this.sharedDream != null) {
            displaySharedDream(this.sharedDream);
        }
    }

    @Override
    public void setListener(SharedDreamDetailListener listener) {
        this.listener = listener;
    }

    public void setSharedDream(@NonNull SharedDream sharedDream) {
        this.sharedDream = sharedDream;
    }

    @Override
    public void displaySharedDream(@NonNull SharedDream sharedDream) {
        this.sharedDream = sharedDream;
        if (this.binding == null) {
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        boolean isOwner = false;
        if (auth.getCurrentUser() != null) {
            isOwner = sharedDream.ownerId().equals(auth.getCurrentUser().getUid());
        }
        this.binding.unshareButton.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        this.binding.sharedOwner.setText(getString(R.string.shared_from, sharedDream.ownerUsername()));
        this.binding.sharedTimestamp.setText(getString(R.string.shared_on, dateFormat.format(sharedDream.timestamp())));
        this.binding.dreamTitle.setText(sharedDream.dream().title());
        this.binding.dreamSummary.setText(sharedDream.dream().sum());
        this.binding.dreamThemes.setText(sharedDream.dream().themesAsText());
        this.binding.dreamCharacters.setText(sharedDream.dream().charactersAsText());
        this.binding.dreamLocations.setText(sharedDream.dream().locationsAsText());
    }
}