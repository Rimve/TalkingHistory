import React from 'react';
import * as FaIcons from 'react-icons/fa';
import * as BiIcons from 'react-icons/bi';
import '../styles/App.css';
import {logoutUser} from "../services/FirebaseService";

export const MenuData = [
    {
        title: 'Characters',
        path: '/management',
        icon: <FaIcons.FaDatabase />,
        onClick: () => temp()
    },
    {
        title: 'Categories',
        path: '/categories',
        icon: <FaIcons.FaObjectGroup />,
        onClick: () => temp()
    },
    {
        title: 'Users',
        path: '/users',
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