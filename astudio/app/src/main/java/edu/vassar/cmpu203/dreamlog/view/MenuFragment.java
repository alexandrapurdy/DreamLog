package edu.vassar.cmpu203.dreamlog.view;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import edu.vassar.cmpu203.dreamlog.databinding.FragmentMenuBinding;


/**
 * Fragment for the main menu screen.
 */
public class MenuFragment extends Fragment implements MenuUI {


    private FragmentMenuBinding binding;
    private MenuListener menuListener;


    /**
     * called when it's time to create the view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.binding = FragmentMenuBinding.inflate(inflater);
        return this.binding.getRoot();
    }


    /**
     * called after the layout has been inflated
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // input dream button
        this.binding.inputDreamButton.setOnClickListener(v -> {
            if (MenuFragment.this.menuListener != null) {
                MenuFragment.this.menuListener.onInputDream();
            }
        });


        // view dream log button
        this.binding.dreamLogButton.setOnClickListener(v -> {
            if (MenuFragment.this.menuListener != null) {
                MenuFragment.this.menuListener.onViewLog();
            }
        });

        // shared dreams button
        this.binding.sharedDreamsButton.setOnClickListener(v -> {
            if (MenuFragment.this.menuListener != null) {
                MenuFragment.this.menuListener.onViewSharedDreams();
            }
        });

        // sign out button
        this.binding.signOutButton.setOnClickListener(v -> {
            if (MenuFragment.this.menuListener != null) {
                MenuFragment.this.menuListener.onSignOut();
            }
        });

        if (this.menuListener != null) {
            this.menuListener.onMenuReady(this);
        }
    }


    /**
     * sets the listener object to be notified of events
     */
    @Override
    public void setListener(MenuListener menuListener) {
        this.menuListener = menuListener;
    }

    @Override
    public void showUsername(@NonNull String username) {
        if (this.binding != null) {
            this.binding.userStatus.setText(getString(edu.vassar.cmpu203.dreamlog.R.string.signed_in_as, username));
            this.binding.signOutButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showSignedOut() {
        if (this.binding != null) {
            this.binding.userStatus.setText(getString(edu.vassar.cmpu203.dreamlog.R.string.sign_in_required));
            this.binding.signOutButton.setVisibility(View.GONE);
        }
    }
}