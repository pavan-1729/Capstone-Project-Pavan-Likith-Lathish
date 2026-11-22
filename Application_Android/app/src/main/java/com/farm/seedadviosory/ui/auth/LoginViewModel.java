package com.farm.seedadviosory.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.farm.seedadviosory.data.model.Farmer;
import com.farm.seedadviosory.data.repository.AuthRepository;

/**
 * ViewModel for the Login screen.
 */
public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<Farmer> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LoginViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Farmer> getLoginResult() { return loginResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void login(String mobileOrEmail, String password) {
        if (mobileOrEmail == null || mobileOrEmail.trim().isEmpty()) {
            errorMessage.setValue("Please enter mobile number or email");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Please enter password");
            return;
        }

        isLoading.setValue(true);
        // Simulate network delay
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Farmer farmer = authRepository.login(mobileOrEmail.trim(), password);
            isLoading.setValue(false);
            if (farmer != null) {
                loginResult.setValue(farmer);
            } else {
                errorMessage.setValue(null); // trigger observer
                errorMessage.setValue("Invalid mobile/email or password");
            }
        }, 800);
    }

    public void loginAsDemo() {
        isLoading.setValue(true);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Farmer farmer = authRepository.loginAsDemoFarmer();
            isLoading.setValue(false);
            loginResult.setValue(farmer);
        }, 500);
    }
}
