import React, {Component} from 'react';
import * as BiIcons from 'react-icons/bi';
import '../styles/App.css';

export default class NotFound extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (
            <>
                <div>
                    <BiIcons.BiErrorCircle className='not-found-icon' />
                    <h1 style={{color: '#c30000'}}>Page Not Found</h1>
                </div>
            </>
        );
    }
}