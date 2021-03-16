import React, {Component} from 'react';
import RegisterComponent from "../components/RegisterComponent";

export default class Register extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <>
                <div>
                    <RegisterComponent />
                </div>
            </>
        );
    }
}