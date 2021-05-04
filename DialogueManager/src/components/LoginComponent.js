import React, {Component} from 'react';
import '../styles/Footer.css';
import {withRouter} from 'react-router-dom';
import * as FaIcons from 'react-icons/fa';
import {Form, FormControl} from "react-bootstrap";
import 'firebaseui/dist/firebaseui.css';
import '../styles/Login.css';
import {firebaseAuthRef} from "../services/FirebaseService";
import AlertMassage from "./AlertMessage";

class LoginComponent extends Component {

    constructor(props) {
        super(props);

        this.state = {
            email: "",
            password: "",
            showAlert: false
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleChange = this.handleChange.bind(this);
    }

    showAlertCallback = (data) => {
        this.setState({showAlert: data});
    }

    showErrorAlert() {
        return (
            <AlertMassage message={"Login failed, please check your credentials"}
                          severity={"error"}
                          show={this.state.showAlert}
                          showAlert={this.showAlertCallback} />
        )
    }

    handleChange(event) {
        this.setState( {
            [event.target.name]: event.target.value
        });
    }

    handleSubmit(event) {
        event.preventDefault();
        const { email, password } = this.state;

        firebaseAuthRef().signInWithEmailAndPassword(email, password)
            .catch(() => {
                this.setState({showAlert: true})
            });
    }

    render() {
        return (
            <div className='div-login container-shadow margin-top'>
                <Form className='form-login' onSubmit={this.handleSubmit}>
                    <FaIcons.FaUserCircle className='icon-profile align-center' />
                    <FormControl name='email' type='email' onChange={this.handleChange} placeholder='Email' className='text-field-login align-center' />
                    <FormControl name='password' type='password' onChange={this.handleChange} placeholder='Password' className='text-field-login align-center' />
                    <button className='button-login container-shadow align-center' type='submit' >
                        <b>Login</b>
                    </button>
                </Form>
                {this.state.showAlert ? this.showErrorAlert() : null}
            </div>
        );
    }
}

export default withRouter(LoginComponent)