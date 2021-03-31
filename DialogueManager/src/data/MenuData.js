import React from 'react';
import * as FaIcons from 'react-icons/fa';
import * as BiIcons from 'react-icons/bi';
import '../styles/App.css';
import {logoutUser} from "../services/DatabaseService";

export const MenuData = [
    {
        title: 'Database',
        path: '/management',
        icon: <FaIcons.FaDatabase />,
        onClick: () => temp()
    },
    {
        title: 'Profile',
        path: '/profile',
        icon: <FaIcons.FaUserCircle />,
        onClick: () => temp()
    },
    {
        title: 'Logout',
        path: '/',
        icon: <BiIcons.BiLogOut />,
        onClick: () => logoutUser()
    }
];

function temp() {

}