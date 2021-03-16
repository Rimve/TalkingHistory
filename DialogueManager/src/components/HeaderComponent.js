import React, {Component} from 'react';
import '../styles/Header.css';
import NavbarComponent from "./NavbarComponent";

export default class HeaderComponent extends Component {

    constructor(props) {
        super(props);
    }

    loadPage() {
        if (this.props.authStatus != null) {
            return (
                <div className='header'>
                    <NavbarComponent authStatus={this.props.authStatus}/>
                </div>
            )
        }
    }

    render() {
        return (
            <>
                {this.loadPage()}
            </>
        )
    }
}