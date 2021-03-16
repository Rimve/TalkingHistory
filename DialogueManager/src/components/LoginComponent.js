import React, {Component} from 'react';
import '../styles/Footer.css';
import firebase from 'firebase';
import {getAuthService, getCurrentUser} from "../services/DatabaseService";
import {withRouter} from 'react-router-dom';
import 'firebaseui/dist/firebaseui.css';
import '../styles/Login.css';

class LoginComponent extends Component {

    constructor(props) {
        super(props);
    }

    login() {
        // Initialize the FirebaseUI Widget using Firebase
        const ui = getAuthService();
        ui.start('#firebaseui-auth-container', {
            callbacks: {
                signInSuccessWithAuthResult: function (authResult, redirectUrl) {
                    return false;
                }
            },
            signInOptions: [
                firebase.auth.EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD
            ],
        });
    }

    componentDidMount() {
        this.login()
    }

    render() {
        return (
            <div>
                <div id='firebaseui-auth-container' />
            </div>
        );
    }
}

export default withRouter(LoginComponent)