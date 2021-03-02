import React, {Component} from 'react';
import '../styles/Footer.css';
import {Route, Switch} from "react-router-dom";
import Home from "../pages/Home";
import Selector from "../pages/Selector";
import Graph from "../pages/Graph";

export default class BodyComponent extends Component {
    render() {
        return (
            <div className='max-height-container'>
                <Switch>
                    <Route path='/' exact component={Home} />
                    <Route path='/selector' component={Selector} />
                    <Route path='/graph' component={Graph} />
                </Switch>
            </div>
        );
    }
}