import React, {Component} from 'react';
import SelectorComponent from "../components/SelectorComponent";

export default class Selector extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <>
                <SelectorComponent />
            </>
        );
    }
}