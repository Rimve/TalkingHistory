import React from 'react';
import { BrowserRouter as Router } from "react-router-dom";
import './styles/App.css';
import {initialize} from "./services/DatabaseService";
import HeaderComponent from "./components/HeaderComponent";
import FooterComponent from "./components/FooterComponent";
import BodyComponent from "./components/BodyComponent";

function App() {
    initialize();
  return (
    <div className="App">
        <Router>
            <HeaderComponent />
            <BodyComponent />
            <FooterComponent />
        </Router>
    </div>
  );
}

export default App;
