import React, {Component} from 'react';
import '../styles/Footer.css';
import {Route, Switch, Redirect} from "react-router-dom";
import Home from "../pages/Home";
import Graph from "../pages/Graph";
import Login from "../pages/Login";
import Register from "../pages/Register";
import PageLoadingComponent from "./PageLoadingComponent";
import NotFound from "../pages/NotFound";
import Management from "../pages/Management";

export default class BodyComponent extends Component {

    constructor(props) {
        super(props);
    }

    loadPage() {
        if (this.props.authStatus == null) {
            return (
                <div className='body-loading' id='body'>
                    <PageLoadingComponent />
                </div>
            );
        }
        else {
            return (
                <div className='body' id='body'>
                    <Switch>
                        <Route path='/' exact component={Home}/>
                        <Route path='/management' render={() =>
                            this.props.authStatus ? <Management/> : <Redirect to='/'/>}
                        />
                        <Route path='/graph' render={() =>
                            this.props.authStatus ? <Graph/> : <Redirect to='/'/>}
                        />
                        <Route path='/login' render={() =>
                            this.props.authStatus ? <Redirect to='/'/> : <Login/>}
                        />
                        <Route path='/register' render={() =>
                            this.props.authStatus ? <Redirect to='/'/> : <Register/>}
                        />
                        <Route path='*' component={NotFound} />
                    </Switch>
                </div>
            );
        }
    }

    render() {
        return (
            <>
                {this.loadPage()}
            </>
        );
    }
}