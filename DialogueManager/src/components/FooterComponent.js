import React, {Component} from 'react';
import '../styles/Footer.css';
import NavbarComponent from "./NavbarComponent";

export default class FooterComponent extends Component {
    constructor(props) {
        super(props);
    }

    loadPage() {
        if (this.props.authStatus != null) {
            return (
                <div className='footer'>
                    <div className='footer-relative'>
                        <span className='color-accent'>Dialogue manager for android application</span>
                        <strong className='color-accent font-italic'> TalkingHistory</strong>
                    </div>
                </div>
            )
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