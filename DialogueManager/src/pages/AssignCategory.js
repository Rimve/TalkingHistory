import React, {Component} from 'react';
import {getCurrentUserRole} from "../services/FirebaseService";
import {Redirect} from "react-router-dom";
import {ROLES} from "../data/Roles";
import PageLoadingComponent from "../components/PageLoadingComponent";
import '../styles/Responsive.css';
import ManageUncategorizedComponent from "../components/ManageUncategorizedComponent";

export default class AssignCategory extends Component {
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
            return ((this.state.role !== ROLES.USER)
                ? <ManageUncategorizedComponent />
                : <Redirect to='/' />)
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
                {this.showComponent()}
            </>
        )
    }
}