package use_case.login;

/**
 * Input Boundary for actions which are related to loggin in.
 */
public interface LoginInputBoundary {

    /**
     * Executes the signup use case.
     * @param loginInputData the input data
     */
    void LoginUser(LoginInputData loginInputData);

    /**
     * Executes the switch to Signup view use case.
     */
    void switchToSignUpView();
}
