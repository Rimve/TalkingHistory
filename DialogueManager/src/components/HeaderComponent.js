import React, {Component} from 'react';
import '../styles/Header.css';
import NavbarComponent from "./NavbarComponent";

export default class HeaderComponent extends Component {
    render() {
        return (
            <div className='header'>
                <NavbarComponent />
            </div>
        )
    }
}