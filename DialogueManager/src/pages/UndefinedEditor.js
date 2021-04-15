import React, {Component} from 'react';
import {getCurrentUserRole} from "../services/DatabaseService";
import {Redirect} from "react-router-dom";
import {ROLES} from "../data/Roles";
import PageLoadingComponent from "../components/PageLoadingComponent";
import '../styles/Responsive.css';
import CharUncategorizedListComponent from "../components/CharUncategorizedListComponent";

export default class UndefinedEditor extends Component {
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
                ? <CharUncategorizedListComponent />
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