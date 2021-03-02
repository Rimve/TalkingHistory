import React, {Component} from 'react';
import GraphComponent from "../components/GraphComponent";

export default class Graph extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <>
                <GraphComponent />
            </>
        );
    }
}