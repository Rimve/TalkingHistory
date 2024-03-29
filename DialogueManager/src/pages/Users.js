import React, {Component} from 'react';
import UsersComponent from "../components/UsersComponent";
import {getCurrentUserRole} from "../services/FirebaseService";
import {Redirect} from "react-router-dom";
import {ROLES} from "../data/Roles";
import PageLoadingComponent from "../components/PageLoadingComponent";
import '../styles/Responsive.css';

export default class Users extends Component {
    constructor(props) {
        super(props);

        this.state = {role: null}
    }

    showComponent() {
        getCurrentUserRole().once("value")
            .then((data) => {
                this.setState({role: data.val()})
            })

        if (this.state.role) {
            return ((this.state.role === ROLES.ADMIN) ? <UsersComponent /> : <Redirect to='/'/>)
        }
        else {
            return (
                <div className='body-loading' id='body'>
                    <PageLoadingComponent />
                </div>
            )
        }
    }

    render() {
        return (
            <>
                <div className="table-responsive table-container">
                    {this.showComponent()}
                </div>
            </>
        )
    }
}