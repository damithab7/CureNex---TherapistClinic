package lk.damithab.curenex.model;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SignInViewModel extends ViewModel {
    // MutableLiveData allows fragments to observe changes
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();

    public void setEmail(String email) {
        userEmail.setValue(email);
    }

    public String getEmail() {
        return userEmail.getValue();
    }
}