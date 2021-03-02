import React, {Component} from 'react';
import '../styles/Footer.css';

export default class FooterComponent extends Component {
    render() {
        return (
            <div className='footer'>
                <div className='footer-relative'>
                    <span className='color-accent'>Dialogue manager for android application</span>
                    <strong className='color-accent font-italic'> TalkingHistory</strong>
                </div>
            </div>
        );
    }
}