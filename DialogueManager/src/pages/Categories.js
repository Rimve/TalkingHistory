import React, {Component} from 'react';
import {getCurrentUserRole} from "../services/FirebaseService";
import {Redirect} from "react-router-dom";
import {ROLES} from "../data/Roles";
import PageLoadingComponent from "../components/PageLoadingComponent";
import CategorizedWordsComponent from "../components/CategorizedWordsComponent";
import '../styles/Responsive.css';
import SelectCategoryComponent from "../components/SelectCategoryComponent";

export default class Categories extends Component {
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
            return ((this.state.role !== ROLES.USER) ? <SelectCategoryComponent /> : <Redirect to='/' />)
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
                <div>
                    {this.showComponent()}
                </div>
            </>
        )
    }
}