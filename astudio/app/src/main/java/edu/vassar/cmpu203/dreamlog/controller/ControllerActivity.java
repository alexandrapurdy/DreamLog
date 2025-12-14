package edu.vassar.cmpu203.dreamlog.controller;


import android.os.Bundle;
import android.util.Log;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


import java.util.ArrayList;
import java.util.List;


import edu.vassar.cmpu203.dreamlog.R;
import edu.vassar.cmpu203.dreamlog.model.Dream;
import edu.vassar.cmpu203.dreamlog.model.DreamAnalyzer;
import edu.vassar.cmpu203.dreamlog.model.DreamLog;
import edu.vassar.cmpu203.dreamlog.model.SharedDream;
import edu.vassar.cmpu203.dreamlog.persistence.CloudDreamLogStore;
import edu.vassar.cmpu203.dreamlog.persistence.DreamSharingService;
import edu.vassar.cmpu203.dreamlog.persistence.UserDreamStorage;
import edu.vassar.cmpu203.dreamlog.view.AuthFragment;
import edu.vassar.cmpu203.dreamlog.view.AuthUI;
import edu.vassar.cmpu203.dreamlog.view.DreamDetailFragment;
import edu.vassar.cmpu203.dreamlog.view.DreamDetailUI;
import edu.vassar.cmpu203.dreamlog.view.EditDreamFragment;
import edu.vassar.cmpu203.dreamlog.view.EditDreamUI;
import edu.vassar.cmpu203.dreamlog.view.InputDreamsFragment;
import edu.vassar.cmpu203.dreamlog.view.InputDreamsUI;
import edu.vassar.cmpu203.dreamlog.view.MainUI;
import edu.vassar.cmpu203.dreamlog.view.MenuFragment;
import edu.vassar.cmpu203.dreamlog.view.MenuUI;
import edu.vassar.cmpu203.dreamlog.view.SharedDreamDetailFragment;
import edu.vassar.cmpu203.dreamlog.view.SharedDreamDetailUI;
import edu.vassar.cmpu203.dreamlog.view.SharedDreamsFragment;
import edu.vassar.cmpu203.dreamlog.view.SharedDreamsUI;
import edu.vassar.cmpu203.dreamlog.view.ViewLogFragment;
import edu.vassar.cmpu203.dreamlog.view.ViewLogUI;


/**
 * Controls the app's execution.
 * Implements the UI listener interfaces
 */
public class ControllerActivity extends AppCompatActivity
        implements InputDreamsUI.InputDreamsListener, MenuUI.MenuListener, ViewLogUI.ViewLogListener,
        DreamDetailUI.DreamDetailListener, EditDreamUI.EditDreamListener, AuthUI.AuthListener,
        SharedDreamsUI.SharedDreamsListener, SharedDreamDetailUI.SharedDreamDetailListener {


    private DreamLog dreamLog = new DreamLog();
    private String currentFilter = "";
    private Dream selectedDream;
    private int selectedDreamIndex = -1;
    private final DreamAnalyzer dreamAnalyzer = new DreamAnalyzer();
    private DreamDetailUI activeDreamDetailUI;


    private FirebaseAuth firebaseAuth;
    private DreamSharingService dreamSharingService;
    private UserDreamStorage userDreamStorage;
    private CloudDreamLogStore cloudDreamLogStore;


    private String cachedUsername = "";
    private String pendingUsername = "";


    /**
     * An enumeration to keep track of what stage in the workflow we are at
     */
    private enum DreamLogState {
        AUTHENTICATING("auth"),
        MENU("menu"),
        ADDING_DREAMS("adding"),
        VIEWING_DREAMS("viewing"),
        VIEWING_DETAIL("detail"),
        EDITING_DREAM("editing"),
        ANALYZING_DREAMS("analyzing"),
        VIEWING_SHARED("shared"),
        VIEWING_SHARED_DETAIL("shared detail");


        final String name;


        DreamLogState(String name) {
            this.name = name;
        }


        @NonNull
        @Override
        public String toString() {
            return this.name;
        }
    }


    private DreamLogState state = DreamLogState.AUTHENTICATING;
    private MainUI mainUI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        this.firebaseAuth = FirebaseAuth.getInstance();
        this.dreamSharingService = new DreamSharingService();
        this.userDreamStorage = new UserDreamStorage(this);
        this.cloudDreamLogStore = new CloudDreamLogStore();


        mainUI = new MainUI(this);
        setContentView(mainUI.getRootView());


        if (this.firebaseAuth.getCurrentUser() == null) {
            showAuth();
        } else {
            loadDreamLogForCurrentUser();
            showMenu();
        }
    }


    /** shows the main menu */
    private void showMenu() {
        this.state = DreamLogState.MENU;

        this.selectedDream = null;
        this.selectedDreamIndex = -1;
        this.activeDreamDetailUI = null;


        MenuFragment menuFragment = new MenuFragment();
        menuFragment.setListener(this);
        mainUI.displayFragment(menuFragment);
    }


    private void updateMenuStatus(@NonNull MenuUI menuUI) {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user != null) {
            menuUI.showUsername(getCachedUsername(user));
        } else {
            menuUI.showSignedOut();
        }
    }


    private void loadDreamLogForCurrentUser() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user == null) {
            this.dreamLog = new DreamLog();
            return;
        }


        String username = getCachedUsername(user);

        DreamLog stored = this.userDreamStorage.loadDreamLog(username);
        this.dreamLog = stored != null ? stored : new DreamLog();
        this.selectedDream = null;
        this.selectedDreamIndex = -1;


        this.cloudDreamLogStore.loadDreamLog(username, new CloudDreamLogStore.LoadCallback() {
            @Override
            public void onLoaded(@Nullable DreamLog dreamLogLoaded) {
                if (dreamLogLoaded == null) return;


                runOnUiThread(() -> {
                    if (state != DreamLogState.MENU && state != DreamLogState.AUTHENTICATING) {
                        Log.i("DreamLog", "Skipping cloud overwrite (state=" + state + ")");
                        return;
                    }


                    dreamLog = dreamLogLoaded;
                    selectedDream = null;
                    selectedDreamIndex = -1;
                    userDreamStorage.saveDreamLog(username, dreamLogLoaded);
                    refreshActiveDreamViews();
                });
            }


            @Override
            public void onError(@NonNull String message) {
                Log.w("DreamLog", "Unable to load cloud dream log: " + message);
            }
        });
    }


    private void persistDreamLogForCurrentUser() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user != null) {
            String username = getCachedUsername(user);
            this.userDreamStorage.saveDreamLog(username, this.dreamLog);
            this.cloudDreamLogStore.saveDreamLog(username, this.dreamLog, new CloudDreamLogStore.SaveCallback() {
                @Override
                public void onSaved() { /* no-op */ }


                @Override
                public void onError(@NonNull String message) {
                    Log.w("DreamLog", "Unable to save dream log to cloud: " + message);
                }
            });
        }
    }


    /** displays authentication screen */
    private void showAuth() {
        this.state = DreamLogState.AUTHENTICATING;
        AuthFragment authFragment = new AuthFragment();
        authFragment.setListener(this);
        mainUI.displayFragment(authFragment);
    }


    @Override
    public void onMenuReady(MenuUI ui) {
        updateMenuStatus(ui);
    }


    @Override
    public void onInputDream() {
        this.state = DreamLogState.ADDING_DREAMS;

        this.selectedDream = null;
        this.selectedDreamIndex = -1;
        this.activeDreamDetailUI = null;


        InputDreamsFragment inputDreamsFragment = new InputDreamsFragment();
        inputDreamsFragment.setListener(this);
        mainUI.displayFragment(inputDreamsFragment);
    }


    @Override
    public void onViewLog() {
        this.state = DreamLogState.VIEWING_DREAMS;
        ViewLogFragment viewLogFragment = new ViewLogFragment();
        viewLogFragment.setListener(this);
        mainUI.displayFragment(viewLogFragment);
    }


    @Override
    public void onViewSharedDreams() {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user == null) {
            mainUI.showSnackbar(getString(R.string.sign_in_required));
            showAuth();
            return;
        }
        this.state = DreamLogState.VIEWING_SHARED;
        SharedDreamsFragment fragment = new SharedDreamsFragment();
        fragment.setListener(this);
        mainUI.displayFragment(fragment);
    }

    @Override
    public void onSignOut() {
        persistDreamLogForCurrentUser();
        this.firebaseAuth.signOut();
        this.cachedUsername = "";
        this.pendingUsername = "";
        this.dreamLog = new DreamLog();
        this.selectedDream = null;
        this.selectedDreamIndex = -1;
        this.activeDreamDetailUI = null;
        showAuth();
    }


    @Override
    public void onAddDream(String title, String sum, String theme, String chara, String loc) {
        if (this.state != DreamLogState.ADDING_DREAMS) {
            return;
        }
        Dream newDream = Dream.fromTextFields(title, sum, theme, chara, loc);
        int index = dreamLog.addDream(newDream);
        this.selectedDream = newDream;
        this.selectedDreamIndex = index;
        persistDreamLogForCurrentUser();
        mainUI.showSnackbar(getString(R.string.dream_added_message));


        this.state = DreamLogState.VIEWING_DETAIL;
        DreamDetailFragment detailFragment = new DreamDetailFragment();
        detailFragment.setListener(this);
        mainUI.displayFragment(detailFragment);
    }


    @Override
    public void onDoneAddingDream() {
        if (this.state != DreamLogState.ADDING_DREAMS) {
            return;
        }

        onBackToMenu();
    }


    @Override
    public void onViewLogReady(ViewLogUI ui) {
        updateViewLogDisplay(ui);
    }


    @Override
    public void onFilter(String filterText) {
        this.currentFilter = filterText.toLowerCase();
        Fragment fragment = mainUI.getCurrentFragment();
        if (fragment instanceof ViewLogFragment) {
            updateViewLogDisplay((ViewLogFragment) fragment);
        }
    }


    @Override
    public void onClearFilter() {
        this.currentFilter = "";
        Fragment fragment = mainUI.getCurrentFragment();
        if (fragment instanceof ViewLogFragment) {
            updateViewLogDisplay((ViewLogFragment) fragment);
        }
    }


    @Override
    public void onLoadSharedDreams(String usernameFilter) {
        Fragment current = mainUI.getCurrentFragment();
        if (current instanceof SharedDreamsFragment sharedDreamsFragment) {
            sharedDreamsFragment.showLoading(true);
            sharedDreamsFragment.showError("");
        }


        String trimmedFilter = usernameFilter == null ? "" : usernameFilter.trim();


        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user == null) {
            mainUI.showSnackbar(getString(R.string.sign_in_required));
            showAuth();
            return;
        }


        this.dreamSharingService.loadSharedDreams(trimmedFilter, new DreamSharingService.LoadListener() {
            @Override
            public void onDreamsLoaded(@NonNull List<SharedDream> dreams) {
                runOnUiThread(() -> {
                    Fragment fragment = mainUI.getCurrentFragment();
                    if (fragment instanceof SharedDreamsFragment sharedFragment) {
                        sharedFragment.showLoading(false);
                        sharedFragment.showError("");
                        sharedFragment.updateDreamDisplay(dreams);
                    }
                });
            }


            @Override
            public void onLoadFailed(@NonNull String message) {
                runOnUiThread(() -> {
                    Fragment fragment = mainUI.getCurrentFragment();
                    if (fragment instanceof SharedDreamsFragment sharedFragment) {
                        sharedFragment.showLoading(false);
                        sharedFragment.showError(message);
                    }
                });
            }
        });
    }


    @Override
    public void onBackToMenu() {
        showMenu();
    }


    @Override
    public void onSharedDreamSelected(@NonNull SharedDream sharedDream) {
        this.state = DreamLogState.VIEWING_SHARED_DETAIL;


        SharedDreamDetailFragment fragment = new SharedDreamDetailFragment();
        fragment.setListener(this);
        fragment.setSharedDream(sharedDream);
        mainUI.displayFragment(fragment);
    }


    @Override
    public void onDreamClicked(Dream dream, int position) {
        this.state = DreamLogState.VIEWING_DETAIL;
        this.activeDreamDetailUI = null;


        // Find the actual index in the original dream log
        this.selectedDreamIndex = findDreamIndex(dream);
        this.selectedDream = dream;


        DreamDetailFragment detailFragment = new DreamDetailFragment();
        detailFragment.setListener(this);
        mainUI.displayFragment(detailFragment);
    }


    @Override
    public void onDreamDetailReady(DreamDetailUI ui) {
        if (this.selectedDream != null && this.selectedDreamIndex >= 0) {
            String baseAnalysis = dreamAnalyzer.generateAnalysis(this.selectedDream);
            this.activeDreamDetailUI = ui;
            ui.displayDream(this.selectedDream, this.selectedDreamIndex,
                    baseAnalysis + "\n\n> Gemini Insights\nLoading Gemini analysis...");


            dreamAnalyzer.generateAiAnalysis(this.selectedDream, aiText -> runOnUiThread(() -> {
                if (this.state == DreamLogState.VIEWING_DETAIL && ui == this.activeDreamDetailUI) {
                    String combined = baseAnalysis + "\n" + aiText;
                    ui.updateAnalysis(combined);
                }
            }));
        }
    }


    @Override
    public void onEditDream(Dream dream, int dreamIndex) {
        this.state = DreamLogState.EDITING_DREAM;
        this.activeDreamDetailUI = null;
        this.selectedDream = dream;
        this.selectedDreamIndex = dreamIndex;


        EditDreamFragment editFragment = new EditDreamFragment();
        editFragment.setListener(this);
        mainUI.displayFragment(editFragment);
    }


    @Override
    public void onDeleteDream(int dreamIndex) {
        boolean deleted = dreamLog.deleteDream(dreamIndex);
        if (!deleted) {
            Log.w("DreamLog", "Attempted to delete dream at invalid index: " + dreamIndex);
            return;
        }
        persistDreamLogForCurrentUser();
        this.selectedDream = null;
        this.selectedDreamIndex = -1;
        this.activeDreamDetailUI = null;
        mainUI.showSnackbar(getString(R.string.dream_deleted_message));


        // return to the dream log view
        this.state = DreamLogState.VIEWING_DREAMS;
        ViewLogFragment viewLogFragment = new ViewLogFragment();
        viewLogFragment.setListener(this);
        mainUI.displayFragment(viewLogFragment);
    }


    @Override
    public void onShareDream(Dream dream) {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user == null) {
            mainUI.showSnackbar(getString(R.string.sign_in_required));
            showAuth();
            return;
        }


        this.dreamSharingService.shareDream(user, dream, new DreamSharingService.ShareListener() {
            @Override
            public void onShareSucceeded() {
                runOnUiThread(() -> mainUI.showSnackbar(getString(R.string.share_success)));
                onBackToSharedList();
            }


            @Override
            public void onShareFailed(@NonNull String message) {
                runOnUiThread(() -> mainUI.showSnackbar(getString(R.string.share_failure, message)));
            }
        });
    }


    @Override
    public void onUnshareDream(@NonNull Dream dream) {
        FirebaseUser user = this.firebaseAuth.getCurrentUser();
        if (user == null) {
            mainUI.showSnackbar(getString(R.string.sign_in_required));
            showAuth();
            return;
        }


        this.dreamSharingService.unshareDream(user, dream, new DreamSharingService.UnshareListener() {
            @Override
            public void onUnshareSucceeded() {
                runOnUiThread(() -> mainUI.showSnackbar(getString(R.string.unshare_success)));
                onBackToSharedList();
            }


            @Override
            public void onUnshareFailed(@NonNull String message) {
                runOnUiThread(() -> mainUI.showSnackbar(getString(R.string.unshare_failure, message)));
            }
        });
    }


    @Override
    public void onEditDreamReady(EditDreamUI ui) {
        if (this.selectedDream != null && this.selectedDreamIndex >= 0) {
            ui.displayDreamForEdit(this.selectedDream, this.selectedDreamIndex);
        }
    }


    @Override
    public void onBackToLog() {
        this.state = DreamLogState.VIEWING_DREAMS;
        this.activeDreamDetailUI = null;
        ViewLogFragment viewLogFragment = new ViewLogFragment();
        viewLogFragment.setListener(this);
        mainUI.displayFragment(viewLogFragment);
    }


    @Override
    public void onSaveEdit(String title, String sum, String theme, String chara, String loc, int dreamIndex) {
        Dream updatedDream = Dream.fromTextFields(title, sum, theme, chara, loc);
        dreamLog.updateDream(dreamIndex, updatedDream);
        persistDreamLogForCurrentUser();
        mainUI.showSnackbar(getString(R.string.dream_updated_message));


        this.selectedDream = updatedDream;
        this.selectedDreamIndex = dreamIndex;


        this.state = DreamLogState.VIEWING_DETAIL;
        DreamDetailFragment detailFragment = new DreamDetailFragment();
        detailFragment.setListener(this);
        mainUI.displayFragment(detailFragment);
    }


    @Override
    public void onCancelEdit() {
        this.state = DreamLogState.VIEWING_DETAIL;
        DreamDetailFragment detailFragment = new DreamDetailFragment();
        detailFragment.setListener(this);
        mainUI.displayFragment(detailFragment);
    }


    @Override
    public void onBackToSharedList() {
        this.state = DreamLogState.VIEWING_SHARED;
        SharedDreamsFragment fragment = new SharedDreamsFragment();
        fragment.setListener(this);
        mainUI.displayFragment(fragment);
    }


    @Override
    public void onSignIn(@NonNull String email, @NonNull String password, @NonNull String username) {
        this.pendingUsername = username;
        authenticate(email, password, false);
    }


    @Override
    public void onRegister(@NonNull String email, @NonNull String password, @NonNull String username) {
        this.pendingUsername = username;
        authenticate(email, password, true);
    }


    private void authenticate(@NonNull String email, @NonNull String password, boolean registering) {
        Fragment current = mainUI.getCurrentFragment();
        if (current instanceof AuthFragment authFragment) {
            authFragment.showLoading(true);
            authFragment.showError("");
        }


        if (registering) {
            this.firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> handleAuthResult(
                            task.isSuccessful(),
                            task.getException() == null ? "" : task.getException().getMessage()
                    ));
        } else {
            this.firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> handleAuthResult(
                            task.isSuccessful(),
                            task.getException() == null ? "" : task.getException().getMessage()
                    ));
        }
    }


    private void handleAuthResult(boolean success, String message) {
        runOnUiThread(() -> {
            Fragment current = mainUI.getCurrentFragment();
            if (current instanceof AuthFragment authFragment) {
                authFragment.showLoading(false);
                if (!success) {
                    authFragment.showError(getString(R.string.auth_error, message));
                    return;
                }
            }


            if (success) {
                loadDreamLogForCurrentUser();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    ensureDisplayNameSet(currentUser);
                    this.cachedUsername = deriveUsername(currentUser);
                }
                mainUI.showSnackbar(getString(
                        R.string.signed_in_as,
                        currentUser != null ? getCachedUsername(currentUser) : ""
                ));
                this.pendingUsername = "";
                showMenu();
            }
        });
    }


    /** updates the view log display with filtered dreams */
    private void updateViewLogDisplay(ViewLogUI ui) {
        List<Dream> filteredDreams = dreamLog.filterDreams(currentFilter);
        ui.updateDreamDisplay(new ArrayList<>(filteredDreams));
    }


    /** finds the actual index of a dream in the dream log */
    private int findDreamIndex(Dream dream) {
        return dreamLog.findDreamIndex(dream);
    }


    private void refreshActiveDreamViews() {
        Fragment fragment = mainUI.getCurrentFragment();
        if (fragment instanceof ViewLogFragment viewLogFragment) {
            updateViewLogDisplay(viewLogFragment);
        } else if (fragment instanceof DreamDetailFragment detailFragment) {
            onDreamDetailReady(detailFragment);
        }
    }


    @NonNull
    private String getCachedUsername(@NonNull FirebaseUser user) {
        if (this.cachedUsername.isEmpty()) {
            this.cachedUsername = deriveUsername(user);
        }
        return this.cachedUsername;
    }


    @NonNull
    private String deriveUsername(@NonNull FirebaseUser user) {
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            return user.getDisplayName();
        }
        if (!this.pendingUsername.isEmpty()) {
            return this.pendingUsername;
        }
        if (user.getEmail() != null && user.getEmail().contains("@")) {
            return user.getEmail().substring(0, user.getEmail().indexOf('@'));
        }
        return user.getUid();
    }


    private void ensureDisplayNameSet(@NonNull FirebaseUser user) {
        if (user.getDisplayName() == null || user.getDisplayName().isEmpty()) {
            String username = deriveUsername(user);
            UserProfileChangeRequest changeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build();
            user.updateProfile(changeRequest);
        }
    }

}