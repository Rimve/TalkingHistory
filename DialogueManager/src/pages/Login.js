import React, {Component} from 'react';
import LoginComponent from "../components/LoginComponent";

export default class Login extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <>
                <div>
                    <LoginComponent />
                </div>
            </>
        );
    }
}