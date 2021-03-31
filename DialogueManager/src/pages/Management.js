import React, {Component} from 'react';
import ManagementComponent from "../components/ManagementComponent";

export default class Management extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <>
                <ManagementComponent />
            </>
        );
    }
}