import React from 'react'
import {getNodeRef} from "../services/DatabaseService";
import '../styles/CharacterSelection.css';
import {Link, withRouter} from "react-router-dom";

class SelectorComponent extends React.Component{
    constructor(props){
        super(props);

        this.state = {
            charList: [],
            loaded: false
        };
    }

    componentDidMount() {
        let names = [];
        getNodeRef().on('value', (snapshot) => {
            let results = snapshot.val();
            for(let index in results){
                const name = {
                    "name" : index,
                    "size" : results[index].length
                };
                names.push(name);
            }
            this.setState({
                charList: names,
                loaded: true
            });
        });
    }

    render() {
        if (this.state.loaded) {
            const {charList} = this.state;
            return (
                <div className="character-list">
                    <ul className="list-group">
                        {
                            charList.map((data, index) => {
                                return (
                                    <Link to={{pathname: '/graph', state: {name: data.name}}} key={index}
                                          className="character-item d-flex justify-content-between align-items-center">
                                        {data.name}
                                        <span className="badge item-badge badge-pill">{data.size}</span>
                                    </Link>
                                )
                            })
                        }
                    </ul>
                </div>
            );
        }
        else return null;
    }
}

export default withRouter(SelectorComponent);