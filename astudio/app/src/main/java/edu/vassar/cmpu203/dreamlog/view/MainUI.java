package edu.vassar.cmpu203.dreamlog.view;


import android.view.LayoutInflater;
import android.view.View;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.material.snackbar.Snackbar;


import edu.vassar.cmpu203.dreamlog.databinding.ActivityMainBinding;




public class MainUI {
    private final ActivityMainBinding binding;
    private final FragmentManager fmanager;


    public MainUI (@NonNull FragmentActivity factivity) {
        this.binding = ActivityMainBinding.inflate(LayoutInflater.from(factivity));
        this.fmanager = factivity.getSupportFragmentManager();


        EdgeToEdge.enable(factivity);
        ViewCompat.setOnApplyWindowInsetsListener(this.binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    public void displayFragment(@NonNull Fragment frag) {
        FragmentTransaction ftrans = this.fmanager.beginTransaction();
        ftrans.replace(this.binding.fragmentContainerView.getId(), frag);
        ftrans.commit();
    }


    /**
     * Returns the fragment currently hosted in the container.
     *
     * @return the active fragment or null if none is attached
     */
    @Nullable
    public Fragment getCurrentFragment() {
        return this.fmanager.findFragmentById(this.binding.fragmentContainerView.getId());
    }


    @NonNull
    public View getRootView() { return this.binding.getRoot(); }


    public void showSnackbar(@NonNull CharSequence message) {
        Snackbar.make(this.binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }


    public int getFragmentContainerId() {
        return this.binding.fragmentContainerView.getId();
    }
}

