import React, {useState, useEffect} from 'react'
import * as RiIcons from 'react-icons/ri';
import { Link, withRouter } from 'react-router-dom';
import "firebase/database";
import '../styles/Navbar.css';
import '../styles/App.css';
import '../styles/Responsive.css';

function NavbarComponent() {
    const [button, setButton] = useState(true);
    const [click, setClick] = useState(false);

    const handleClick = () => setClick(!click);
    const closeMobileMenu = () => setClick(false);

    const showButton = () => {
        if (window.innerWidth <= 960) {
            setButton(false);
        } else {
            setButton(true);
        }
    };

    useEffect(() => {
        showButton();
    }, []);

    window.addEventListener('resize', showButton);

    return (
        <>
            <div className='navbar'>
                <div className='title-position'>
                    <Link to='/' className='title'>
                        <b>Dialogue Manager</b>
                    </Link>
                </div>
                <div onClick={handleClick} className='menu-icon-position'>
                    <Link to='#' className='menu-bars'>
                        {click ? <RiIcons.RiCloseLine/> : <RiIcons.RiMenuFoldLine/>}
                    </Link>
                </div>
                <ul className={click ? 'res-menu active' : 'res-menu'}>
                    <li className={click ? 'res-item active' : 'res-item'}>
                        <Link to='/selector' onClick={closeMobileMenu}>
                            <button className='button text-field-height'>
                                <b>Database</b>
                            </button>
                        </Link>
                    </li>
                    <li className={click ? 'res-item active' : 'res-item'}>
                        <Link to='/login' onClick={closeMobileMenu}>
                            <button className='button text-field-height'>
                                <b>Login</b>
                            </button>
                        </Link>
                    </li>
                    <li className={click ? 'res-item active' : 'res-item'}>
                        <Link to='/register' onClick={closeMobileMenu}>
                            <button className='button text-field-height'>
                                <b>Register</b>
                            </button>
                        </Link>
                    </li>
                </ul>
            </div>
        </>
    );
}

export default withRouter(NavbarComponent);