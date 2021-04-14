import React, { Component } from 'react';
import {Form, FormControl} from "react-bootstrap";
import * as FaIcons from 'react-icons/fa';
import '../styles/Register.css';
import {createUser} from "../services/DatabaseService";
import AlertMassage from "./AlertMessage";

export default class RegisterComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            input: {},
            errors: {},
            showSuccessAlert: false,
            showErrorAlert: false
        };

        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    showAlertCallback = (data) => {
        this.setState({showSuccessAlert: data});
        this.setState({showErrorAlert: data});
    };

    showSuccessAlert() {
        return (
            <AlertMassage severity={"success"}
                          message={"Registration successful!"}
                          show={this.state.showSuccessAlert}
                          showAlert={this.showAlertCallback} />
        )
    }

    showErrorAlert() {
        return (
            <AlertMassage severity={"error"}
                          message={"Make sure you filled all of the fields correctly and try again"}
                          show={this.state.showErrorAlert}
                          showAlert={this.showAlertCallback} />
        )
    }

    handleChange(event) {
        let input = this.state.input;
        input[event.target.name] = event.target.value;

        this.setState({input});
    }

    handleSubmit(event) {
        event.preventDefault();

        if(this.validate()) {
            let input = {};
            input["password"] = "";
            input["passwordRpt"] = "";
            input["email"] = "";
            this.setState({input: input, showSuccessAlert: true});

            createUser(this.state.input["email"], this.state.input["password"])
        }
        else {
            this.setState({showErrorAlert: true});
        }
    }

    validate() {
        let input = this.state.input;
        let errors = {};
        let isValid = true;

        if (!input["password"]) {
            isValid = false;
            errors["password"] = "Please enter your password";
        }

        if (typeof input["password"] !== "undefined") {
            if (input["password"].length < 8) {
                isValid = false;
                errors["password"] = "Password must be at least 8 characters long";
            }
        }

        if (!input["passwordRpt"]) {
            isValid = false;
            errors["passwordRpt"] = "Please re-enter your password here";
        }

        if (typeof input["passwordRpt"] !== "undefined") {
            if (input["passwordRpt"] !== input["password"]) {
                isValid = false;
                errors["passwordRpt"] = "Password does not match";
            }
        }

        if (!input["email"]) {
            isValid = false;
            errors["email"] = "Please enter your email address";
        }

        if (typeof input["email"] !== "undefined") {
            const expression = /(?!.*\.{2})^([a-z\d!#$%&'*+\-\/=?^_`{|}~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+(\.[a-z\d!#$%&'*+\-\/=?^_`{|}~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]+)*|"((([\t]*\r\n)?[\t]+)?([\x01-\x08\x0b\x0c\x0e-\x1f\x7f\x21\x23-\x5b\x5d-\x7e\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|\\[\x01-\x09\x0b\x0c\x0d-\x7f\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]))*(([\t]*\r\n)?[\t]+)?")@(([a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|[a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF][a-z\d\-._~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]*[a-z\d\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])\.)+([a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]|[a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF][a-z\d\-._~\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF]*[a-z\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])\.?$/i;

            var pattern = new RegExp(expression);
            if (!pattern.test(input["email"])) {
                isValid = false;
                errors["email"] = "Please enter a valid email address";
            }
        }

        this.setState({
            errors: errors
        });

        return isValid;
    }

    render () {
        return (
            <div className='register-container margin-top container-shadow'>
                <Form className='form-register' onSubmit={this.handleSubmit}>
                    <FaIcons.FaUserCircle className='icon-user align-center' />

                    <label className="text align-start"><b>Email</b></label>
                    <FormControl
                        type="email"
                        name="email"
                        value={this.state.input.email}
                        onChange={this.handleChange}
                        placeholder="Enter your email address"
                        className='text-field-register align-center'
                        id="email"/>
                    <div className="text-danger align-warning">{this.state.errors.email}</div>

                    <label className="text align-start"><b>Password</b></label>
                    <FormControl
                        type="password"
                        name="password"
                        value={this.state.input.password}
                        onChange={this.handleChange}
                        placeholder="Enter your password"
                        className='text-field-register align-center'
                        id="password"/>
                    <div className="text-danger align-warning">{this.state.errors.password}</div>

                    <label className="text align-start"><b>Repeat password</b></label>
                    <FormControl
                        type="password"
                        name="passwordRpt"
                        value={this.state.input.passwordRpt}
                        onChange={this.handleChange}
                        placeholder="Repeat your password"
                        className='text-field-register align-center'
                        id="passwordRpt"/>
                    <div className="text-danger align-warning">{this.state.errors.passwordRpt}</div>

                    <button className='button-register container-shadow align-center'>
                        <b>Register</b>
                    </button>
                </Form>
                {this.state.showSuccessAlert ? this.showSuccessAlert() : null}
                {this.state.showErrorAlert ? this.showErrorAlert() : null}
            </div>
        );
    }
}