import React, {useEffect, useState} from 'react';
import { BrowserRouter as Router } from "react-router-dom";
import './styles/App.css';
import {initialize} from "./services/DatabaseService";
import HeaderComponent from "./components/HeaderComponent";
import FooterComponent from "./components/FooterComponent";
import BodyComponent from "./components/BodyComponent";
import firebase from "firebase";

function App() {
    const [isAuthenticated, setAuthenticated] = useState(null);

    initialize();

    useEffect(() => {
        onLoad();
    }, []);

    async function onLoad() {
        try {
            firebase.auth().onAuthStateChanged((user) => {
                if (user) {
                    setAuthenticated(true);
                } else {
                    setAuthenticated(false);
                }
            });
        } catch (e) {
            alert(e);
        }
    }

    return (
        <div className="App">
            <Router>
                <HeaderComponent authStatus={isAuthenticated} />
                <BodyComponent authStatus={isAuthenticated} />
                <FooterComponent authStatus={isAuthenticated} />
            </Router>
        </div>
    );
}

export default App;
